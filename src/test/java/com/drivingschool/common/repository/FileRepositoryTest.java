package com.drivingschool.common.repository;

import com.drivingschool.common.exception.*;
import com.drivingschool.common.model.Identifiable;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class FileRepositoryTest {

    @TempDir
    Path directory;

    static class TestRepository extends FileRepository<TestRecord> {

        TestRepository(Path directory) {
            super(
                    directory,
                    "test-records.txt",
                    TestRecord.class,
                    new ObjectMapper()
            );
        }
    }

    public static class TestRecord implements Identifiable {

        private String id;
        private String name;

        // Jackson needs a way to construct the object.
        public TestRecord() {
        }

        public TestRecord(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    void createsMissingDirectoryAndFiles() {
        Path nested = directory.resolve("new-data");
        TestRepository repository = new TestRepository(nested);

        assertTrue(repository.readAll().isEmpty());
        assertTrue(Files.exists(nested.resolve("test-records.txt")));
        assertTrue(Files.exists(
                nested.resolve("test-records.txt.ids.txt")
        ));
    }

    @Test
    void supportsCrudAndPersistsAcrossInstances() throws Exception {
        TestRepository repository = new TestRepository(directory);

        String id = repository.nextId("S");
        repository.save(new TestRecord(id, "Sample student"));

        assertEquals(
                1,
                Files.readAllLines(
                        directory.resolve("test-records.txt")
                ).size()
        );

        TestRepository reopened = new TestRepository(directory);

        assertEquals(
                "Sample student",
                reopened.findById(id).orElseThrow().getName()
        );

        reopened.update(new TestRecord(id, "Updated student"));

        assertEquals(
                "Updated student",
                repository.findById(id).orElseThrow().getName()
        );

        reopened.delete(id);
        assertTrue(repository.findById(id).isEmpty());
    }

    @Test
    void rejectsDuplicateAndMissingRecords() {
        TestRepository repository = new TestRepository(directory);

        repository.save(
                new TestRecord(repository.nextId("S"), "One")
        );

        assertThrows(
                DuplicateRecordException.class,
                () -> repository.save(new TestRecord("S001", "Two"))
        );

        assertThrows(
                RecordNotFoundException.class,
                () -> repository.update(
                        new TestRecord("S002", "Missing")
                )
        );

        assertThrows(
                RecordNotFoundException.class,
                () -> repository.delete("S002")
        );
    }

    @Test
    void doesNotReuseDeletedOrReservedIds() {
        TestRepository repository = new TestRepository(directory);

        String first = repository.nextId("S");
        repository.save(new TestRecord(first, "One"));
        repository.delete(first);

        assertEquals(
                "S002",
                new TestRepository(directory).nextId("S")
        );

        // S002 was reserved without saving a record.
        assertEquals(
                "S003",
                new TestRepository(directory).nextId("S")
        );
    }

    @Test
    void skipsCorruptLinesButPreventsDestructiveRewrite()
            throws Exception {

        TestRepository repository = new TestRepository(directory);

        repository.save(
                new TestRecord(repository.nextId("S"), "One")
        );

        Path file = directory.resolve("test-records.txt");

        Files.writeString(
                file,
                "broken-json\n",
                StandardOpenOption.APPEND
        );

        String before = Files.readString(file);

        assertEquals(1, repository.readAll().size());

        assertThrows(
                StorageException.class,
                () -> repository.delete("S001")
        );

        assertThrows(
                StorageException.class,
                () -> repository.nextId("S")
        );

        assertEquals(before, Files.readString(file));
    }

    @Test
    void missingCounterDoesNotResetIssuedIds() throws Exception {
        TestRepository repository = new TestRepository(directory);

        repository.save(
                new TestRecord(repository.nextId("S"), "One")
        );

        repository.delete("S001");

        Files.delete(
                directory.resolve("test-records.txt.ids.txt")
        );

        TestRepository reopened = new TestRepository(directory);

        assertThrows(
                StorageException.class,
                () -> reopened.nextId("S")
        );
    }

    @Test
    void concurrentInstancesReserveDifferentIds() throws Exception {
        TestRepository first = new TestRepository(directory);
        TestRepository second = new TestRepository(directory);

        ExecutorService executor = Executors.newFixedThreadPool(4);

        try {
            List<Callable<String>> tasks = new ArrayList<>();

            for (int i = 0; i < 20; i++) {
                TestRepository repository =
                        i % 2 == 0 ? first : second;

                tasks.add(() -> repository.nextId("S"));
            }

            Set<String> ids = new HashSet<>();

            for (Future<String> result : executor.invokeAll(tasks)) {
                ids.add(result.get());
            }

            assertEquals(20, ids.size());
            assertEquals("S021", first.nextId("S"));

        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void invalidRecordDoesNotChangeStorage() throws Exception {
        TestRepository repository = new TestRepository(directory);

        assertThrows(
                ValidationException.class,
                () -> repository.save(
                        new TestRecord("S000", "Invalid")
                )
        );

        assertEquals(
                "",
                Files.readString(
                        directory.resolve("test-records.txt")
                )
        );
    }

    @Test
    void strictReadsReturnValidRecordsAndEmptyForMissingId() {
        TestRepository repository = new TestRepository(directory);

        repository.save(
                new TestRecord(repository.nextId("S"), "Sample student")
        );

        assertEquals(1, repository.readAllStrict().size());

        assertEquals(
                "Sample student",
                repository.findByIdStrict("S001")
                        .orElseThrow()
                        .getName()
        );

        assertTrue(repository.findByIdStrict("S002").isEmpty());
    }

    @Test
    void strictLookupRejectsCorruptionEvenWhenRequestedRecordExists()
            throws Exception {

        TestRepository repository = new TestRepository(directory);

        repository.save(
                new TestRecord(repository.nextId("S"), "Sample student")
        );

        Path file = directory.resolve("test-records.txt");

        Files.writeString(
                file,
                "broken-json\n",
                StandardOpenOption.APPEND
        );

        String before = Files.readString(file);

        // Ordinary reads can still return the valid record.
        assertEquals(1, repository.readAll().size());

        // Strict reads must reject the incomplete file.
        assertThrows(
                StorageException.class,
                () -> repository.readAllStrict()
        );

        assertThrows(
                StorageException.class,
                () -> repository.findByIdStrict("S001")
        );

        assertEquals(before, Files.readString(file));
    }
}