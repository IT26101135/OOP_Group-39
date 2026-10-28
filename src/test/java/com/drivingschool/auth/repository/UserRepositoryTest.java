package com.drivingschool.auth.repository;

import com.drivingschool.auth.model.Admin;
import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.util.PasswordHasher;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.common.exception.StorageException;
import com.drivingschool.student.model.Student;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private static final String TEST_PASSWORD = "DrivingSchool123!";

    private static String passwordHash;

    @TempDir
    Path tempDir;

    private ObjectMapper mapper;
    private UserRepository repository;

    @BeforeAll
    static void generateTestHash() {
        passwordHash = new PasswordHasher().hash(TEST_PASSWORD);
    }

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        repository = reopen();
    }

    private UserRepository reopen() {
        return new UserRepository(tempDir.toString(), mapper);
    }

    private Student student(String id) {
        return new Student(
                id,
                "Nimal Perera",
                "nimal@example.com",
                passwordHash,
                "0771234567"
        );
    }

    private Admin admin(String id) {
        return new Admin(
                id,
                "System Admin",
                "admin@example.com",
                passwordHash
        );
    }

    @Test
    void initializesMissingFilesAndReadsEmptyRepository() {
        assertTrue(Files.isRegularFile(tempDir.resolve("users.txt")));
        assertTrue(Files.isRegularFile(
                tempDir.resolve("users.txt.ids.txt")
        ));

        assertTrue(repository.readAllStrict().isEmpty());
        assertTrue(repository.findByIdStrict("S001").isEmpty());
    }

    @Test
    void savesBothUserTypesAndRestoresAllFieldsAfterReopening() {
        repository.save(student("S001"));
        repository.save(admin("A001"));

        UserRepository reloaded = reopen();

        assertEquals(2, reloaded.readAllStrict().size());

        Student actualStudent = assertInstanceOf(
                Student.class,
                reloaded.findByIdStrict("S001").orElseThrow()
        );

        assertEquals("S001", actualStudent.getId());
        assertEquals("Nimal Perera", actualStudent.getName());
        assertEquals("nimal@example.com", actualStudent.getEmail());
        assertEquals("0771234567", actualStudent.getPhone());
        assertEquals(passwordHash, actualStudent.getPasswordHash());
        assertEquals(Role.STUDENT, actualStudent.getRole());

        Admin actualAdmin = assertInstanceOf(
                Admin.class,
                reloaded.findByIdStrict("A001").orElseThrow()
        );

        assertEquals("A001", actualAdmin.getId());
        assertEquals("System Admin", actualAdmin.getName());
        assertEquals("admin@example.com", actualAdmin.getEmail());
        assertEquals(passwordHash, actualAdmin.getPasswordHash());
        assertEquals(Role.ADMIN, actualAdmin.getRole());

        assertTrue(new PasswordHasher().matches(
                TEST_PASSWORD,
                actualStudent.getPasswordHash()
        ));
    }

    @Test
    void writesOneJsonObjectPerLineWithTypeAndHash() throws IOException {
        repository.save(student("S001"));
        repository.save(admin("A001"));

        List<String> lines = Files.readAllLines(
                tempDir.resolve("users.txt"),
                StandardCharsets.UTF_8
        );

        assertEquals(2, lines.size());

        for (String line : lines) {
            JsonNode json = mapper.readTree(line);

            assertTrue(json.isObject());
            assertEquals(passwordHash, json.get("passwordHash").asText());
            assertFalse(json.has("password"));
            assertFalse(json.has("role"));
            assertFalse(json.has("dashboardPath"));
            assertFalse(line.contains(TEST_PASSWORD));

            String expectedType =
                    json.get("id").asText().startsWith("S")
                            ? "STUDENT"
                            : "ADMIN";

            assertEquals(expectedType, json.get("type").asText());
        }
    }

    @Test
    void updatePersistsStudentChangesAndPreservesAdmin() {
        repository.save(student("S001"));
        repository.save(admin("A001"));

        Student changed = new Student(
                "S001",
                "Nimal Silva",
                "nimal.silva@example.com",
                passwordHash,
                "+94771234567"
        );

        repository.update(changed);

        UserRepository reloaded = reopen();

        Student actual = assertInstanceOf(
                Student.class,
                reloaded.findByIdStrict("S001").orElseThrow()
        );

        assertEquals("S001", actual.getId());
        assertEquals("Nimal Silva", actual.getName());
        assertEquals("nimal.silva@example.com", actual.getEmail());
        assertEquals("+94771234567", actual.getPhone());
        assertEquals(passwordHash, actual.getPasswordHash());

        User unchangedAdmin =
                reloaded.findByIdStrict("A001").orElseThrow();

        assertEquals(Role.ADMIN, unchangedAdmin.getRole());
        assertEquals("System Admin", unchangedAdmin.getName());
        assertEquals(2, reloaded.readAllStrict().size());
    }

    @Test
    void deletePersistsAfterReopeningAndPreservesOtherAccount() {
        repository.save(student("S001"));
        repository.save(admin("A001"));

        repository.delete("S001");

        UserRepository reloaded = reopen();

        assertTrue(reloaded.findByIdStrict("S001").isEmpty());
        assertInstanceOf(
                Admin.class,
                reloaded.findByIdStrict("A001").orElseThrow()
        );
        assertEquals(1, reloaded.readAllStrict().size());
    }

    @Test
    void rejectsDuplicateIdsWithoutReplacingExistingAccount() {
        repository.save(student("S001"));

        Student duplicate = new Student(
                "S001",
                "Another Student",
                "another@example.com",
                passwordHash,
                "0777654321"
        );

        assertThrows(
                DuplicateRecordException.class,
                () -> repository.save(duplicate)
        );

        UserRepository reloaded = reopen();

        assertEquals(1, reloaded.readAllStrict().size());
        assertEquals(
                "Nimal Perera",
                reloaded.findByIdStrict("S001").orElseThrow().getName()
        );
    }

    @Test
    void missingUpdateAndDeleteThrowRecordNotFoundException() {
        assertThrows(
                RecordNotFoundException.class,
                () -> repository.update(student("S001"))
        );

        assertThrows(
                RecordNotFoundException.class,
                () -> repository.delete("S001")
        );

        assertTrue(reopen().readAllStrict().isEmpty());
    }

    @Test
    void allocatesIndependentPersistentStudentAndAdminIds() {
        assertEquals("S001", repository.nextId("S"));
        assertEquals("A001", repository.nextId("A"));

        UserRepository reloaded = reopen();

        assertEquals("S002", reloaded.nextId("S"));
        assertEquals("A002", reloaded.nextId("A"));
    }

    @Test
    void deletedStudentIdIsNotReusedAfterReopening() {
        String id = repository.nextId("S");
        repository.save(student(id));

        repository.delete(id);

        assertEquals("S002", reopen().nextId("S"));
    }

    @Test
    void unknownUserTypeBlocksStrictReadsAndWrites() throws IOException {
        repository.save(student("S001"));

        Path recordsFile = tempDir.resolve("users.txt");
        Path counterFile = tempDir.resolve("users.txt.ids.txt");

        Files.writeString(
                recordsFile,
                "{\"type\":\"INSTRUCTOR\",\"id\":\"I001\"}\n",
                StandardCharsets.UTF_8,
                StandardOpenOption.APPEND
        );

        String recordsBefore = Files.readString(
                recordsFile, StandardCharsets.UTF_8
        );
        String countersBefore = Files.readString(
                counterFile, StandardCharsets.UTF_8
        );

        assertThrows(StorageException.class, repository::readAllStrict);

        assertThrows(
                StorageException.class,
                () -> repository.findByIdStrict("S001")
        );

        assertThrows(
                StorageException.class,
                () -> repository.save(student("S002"))
        );

        assertThrows(
                StorageException.class,
                () -> repository.update(student("S001"))
        );

        assertThrows(
                StorageException.class,
                () -> repository.delete("S001")
        );

        assertThrows(
                StorageException.class,
                () -> repository.nextId("S")
        );

        assertEquals(recordsBefore, Files.readString(
                recordsFile, StandardCharsets.UTF_8
        ));
        assertEquals(countersBefore, Files.readString(
                counterFile, StandardCharsets.UTF_8
        ));
    }
}