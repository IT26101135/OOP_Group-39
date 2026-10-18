package com.drivingschool.common.repository;

import com.drivingschool.common.exception.*;
import com.drivingschool.common.model.Identifiable;
import com.drivingschool.common.util.IdGenerator;
import com.fasterxml.jackson.databind.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

public abstract class FileRepository<T extends Identifiable> {

    // Shared by all repository instances in this running application.
    private static final Object STORAGE_LOCK = new Object();

    private static final Logger LOG =
            Logger.getLogger(FileRepository.class.getName());

    private final Path file;
    private final Path counterFile;
    private final ObjectMapper mapper;
    private final ObjectReader reader;
    private final ObjectWriter writer;

    protected FileRepository(
            Path dataDirectory,
            String fileName,
            Class<T> type,
            ObjectMapper objectMapper) {

        Objects.requireNonNull(dataDirectory, "dataDirectory");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(objectMapper, "objectMapper");

        if (fileName == null
                || !fileName.matches("[a-z][a-z0-9_-]*\\.txt")) {

            throw new ValidationException(
                    "Use a simple file name such as students.txt."
            );
        }

        file = dataDirectory.toAbsolutePath()
                .normalize()
                .resolve(fileName);

        counterFile = file.resolveSibling(fileName + ".ids.txt");

        // Copy configuration without changing Spring's shared ObjectMapper.
        mapper = objectMapper.copy()
                .disable(SerializationFeature.INDENT_OUTPUT)
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

        reader = mapper.readerFor(type);
        writer = mapper.writerFor(type);

        synchronized (STORAGE_LOCK) {
            try {
                Files.createDirectories(file.getParent());

                boolean created = false;

                if (!Files.exists(file)) {
                    if (Files.exists(counterFile)) {
                        throw new IOException(
                                "Records file is missing but its counter exists."
                        );
                    }

                    Files.createFile(file);
                    created = true;
                }

                // Only initialize counters for a newly created records file.
                // An existing file with missing counters needs investigation.
                if (!Files.exists(counterFile) && created) {
                    atomicWrite(counterFile, "{}\n");
                }

            } catch (IOException e) {
                throw new StorageException(
                        "Cannot initialize " + fileName,
                        e
                );
            }
        }
    }

    public List<T> readAll() {
        synchronized (STORAGE_LOCK) {
            return readRecords(false);
        }
    }

    public Optional<T> findById(String id) {
        validateId(id);

        synchronized (STORAGE_LOCK) {
            return readRecords(false).stream()
                    .filter(record -> id.equals(record.getId()))
                    .findFirst();
        }
    }

    /**
     * Reads all records, but fails if any non-blank record is invalid.
     * Use for safety checks that require complete information.
     */
    public List<T> readAllStrict() {
        synchronized (STORAGE_LOCK) {
            return readRecords(true);
        }
    }

    /**
     * Finds a record only after verifying that the entire file is readable
     * without invalid records.
     */
    public Optional<T> findByIdStrict(String id) {
        validateId(id);

        synchronized (STORAGE_LOCK) {
            return readRecords(true).stream()
                    .filter(record -> id.equals(record.getId()))
                    .findFirst();
        }
    }

    public void save(T record) {
        validateRecord(record);

        synchronized (STORAGE_LOCK) {
            List<T> records = readRecords(true);

            if (indexOf(records, record.getId()) >= 0) {
                throw new DuplicateRecordException(
                        "ID already exists: " + record.getId()
                );
            }

            // Advance counters before writing records.
            // A failed save may leave a harmless gap in numbering.
            Map<String, Integer> counters = readCounters();

            String prefix = record.getId().substring(0, 1);
            int number = Integer.parseInt(record.getId().substring(1));

            if (number > counters.getOrDefault(prefix, 0)) {
                counters.put(prefix, number);
                writeCounters(counters);
            }

            records.add(record);
            writeRecords(records);
        }
    }

    public void update(T record) {
        validateRecord(record);

        synchronized (STORAGE_LOCK) {
            List<T> records = readRecords(true);
            int index = indexOf(records, record.getId());

            if (index < 0) {
                throw new RecordNotFoundException(
                        "ID not found: " + record.getId()
                );
            }

            records.set(index, record);
            writeRecords(records);
        }
    }

    public void delete(String id) {
        validateId(id);

        synchronized (STORAGE_LOCK) {
            List<T> records = readRecords(true);
            int index = indexOf(records, id);

            if (index < 0) {
                throw new RecordNotFoundException(
                        "ID not found: " + id
                );
            }

            records.remove(index);
            writeRecords(records);
        }
    }

    public String nextId(String prefix) {
        // Validate the prefix even when it has never been used.
        IdGenerator.nextId(prefix, 0);

        synchronized (STORAGE_LOCK) {
            List<T> records = readRecords(true);
            Map<String, Integer> counters = readCounters();

            int last = counters.getOrDefault(prefix, 0);

            // Also consider existing records, for example imported sample data.
            for (T record : records) {
                if (record.getId().startsWith(prefix)) {
                    int number = Integer.parseInt(
                            record.getId().substring(1)
                    );

                    last = Math.max(last, number);
                }
            }

            String id = IdGenerator.nextId(prefix, last);

            // Persist the reservation before returning the ID.
            counters.put(prefix, last + 1);
            writeCounters(counters);

            return id;
        }
    }

    private List<T> readRecords(boolean requireCleanFile) {
        List<T> records = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        boolean damaged = false;

        try (BufferedReader input = Files.newBufferedReader(
                file, StandardCharsets.UTF_8)) {

            String line;
            int lineNumber = 0;

            while ((line = input.readLine()) != null) {
                lineNumber++;

                if (line.isBlank()) {
                    continue;
                }

                try {
                    T record = reader.readValue(line);
                    validateRecord(record);

                    if (!ids.add(record.getId())) {
                        throw new ValidationException(
                                "Duplicate ID in storage."
                        );
                    }

                    records.add(record);

                } catch (IOException | ValidationException e) {
                    damaged = true;

                    // Never log record contents or password hashes.
                    LOG.warning(
                            "Skipped invalid record in "
                                    + file.getFileName()
                                    + " at line " + lineNumber
                    );
                }
            }

        } catch (IOException e) {
            throw new StorageException(
                    "Cannot read " + file.getFileName(),
                    e
            );
        }

        if (requireCleanFile && damaged) {
            throw new StorageException(
                    "Repair invalid records before changing "
                            + file.getFileName(),
                    new IOException("Invalid records detected.")
            );
        }

        return records;
    }

    private Map<String, Integer> readCounters() {
        try {
            String contents = Files.readString(
                    counterFile, StandardCharsets.UTF_8
            );

            JsonNode root = mapper.readTree(contents);

            if (root == null || !root.isObject()) {
                throw new IOException(
                        "Counter must be a JSON object."
                );
            }

            Map<String, Integer> counters = new HashMap<>();

            Iterator<Map.Entry<String, JsonNode>> fields =
                    root.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                JsonNode value = field.getValue();

                if (!field.getKey().matches("[A-Z]")
                        || !value.isIntegralNumber()
                        || !value.canConvertToInt()
                        || value.intValue() < 0
                        || value.intValue() > 999) {

                    throw new IOException("Invalid counter entry.");
                }

                counters.put(field.getKey(), value.intValue());
            }

            return counters;

        } catch (IOException e) {
            throw new StorageException(
                    "Cannot read ID counters; restore them from backup.",
                    e
            );
        }
    }

    private void writeCounters(Map<String, Integer> counters) {
        try {
            atomicWrite(
                    counterFile,
                    mapper.writeValueAsString(counters) + "\n"
            );

        } catch (IOException e) {
            throw new StorageException(
                    "Cannot save ID counters.",
                    e
            );
        }
    }

    private void writeRecords(List<T> records) {
        try {
            StringBuilder contents = new StringBuilder();

            for (T record : records) {
                contents.append(writer.writeValueAsString(record))
                        .append('\n');
            }

            atomicWrite(file, contents.toString());

        } catch (IOException e) {
            throw new StorageException(
                    "Cannot save " + file.getFileName(),
                    e
            );
        }
    }

    private void atomicWrite(Path target, String contents)
            throws IOException {

        Path temporary = Files.createTempFile(
                target.getParent(), "records-", ".tmp"
        );

        try {
            Files.writeString(
                    temporary, contents, StandardCharsets.UTF_8
            );

            Files.move(
                    temporary,
                    target,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } finally {
            try {
                Files.deleteIfExists(temporary);

            } catch (IOException e) {
                LOG.warning(
                        "Could not remove temporary storage file."
                );
            }
        }
    }

    private int indexOf(List<T> records, String id) {
        for (int i = 0; i < records.size(); i++) {
            if (id.equals(records.get(i).getId())) {
                return i;
            }
        }

        return -1;
    }

    private void validateRecord(T record) {
        if (record == null) {
            throw new ValidationException(
                    "Record cannot be null."
            );
        }

        validateId(record.getId());
    }

    private void validateId(String id) {
        if (id == null
                || !id.matches("[A-Z][0-9]{3}")
                || id.endsWith("000")) {

            throw new ValidationException(
                    "ID must contain a letter and a number from 001 to 999."
            );
        }
    }
}