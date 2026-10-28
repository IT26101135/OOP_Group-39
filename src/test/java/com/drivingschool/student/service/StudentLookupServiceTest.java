package com.drivingschool.student.service;

import com.drivingschool.auth.model.Admin;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.common.exception.StorageException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.student.api.StudentLookup;
import com.drivingschool.student.dto.StudentSummary;
import com.drivingschool.student.model.Student;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StudentLookupServiceTest {

    // Format-only fixture: these tests do not verify passwords.
    private static final String HASH =
            "$2a$10$" + "A".repeat(53);

    @TempDir
    Path tempDir;

    private UserRepository repository;
    private StudentLookup lookup;

    @BeforeEach
    void setUp() {
        repository = new UserRepository(
                tempDir.toString(),
                new ObjectMapper()
        );

        lookup = new StudentLookupService(repository);
    }

    private Student student(String name) {
        return new Student(
                "S001",
                name,
                "nimal@example.com",
                HASH,
                "0771234567"
        );
    }

    @Test
    void findsExistingStudentThroughInterface() {
        repository.save(student("Nimal Perera"));

        assertTrue(lookup.exists("S001"));

        StudentSummary summary =
                lookup.findById("S001").orElseThrow();

        assertEquals("S001", summary.getId());
        assertEquals("Nimal Perera", summary.getName());
    }

    @Test
    void missingStudentReturnsFalseAndEmpty() {
        assertFalse(lookup.exists("S999"));
        assertTrue(lookup.findById("S999").isEmpty());
    }

    @Test
    void rejectsInvalidStudentIds() {
        for (String invalid : List.of(
                "", " ", "S000", "S1000", "s001",
                "S01", " S001", "S001 ", "A001", "I001")) {

            assertThrows(
                    ValidationException.class,
                    () -> lookup.exists(invalid)
            );

            assertThrows(
                    ValidationException.class,
                    () -> lookup.findById(invalid)
            );
        }

        assertThrows(
                ValidationException.class,
                () -> lookup.exists(null)
        );

        assertThrows(
                ValidationException.class,
                () -> lookup.findById(null)
        );
    }

    @Test
    void administratorIsNotAvailableThroughStudentLookup() {
        repository.save(new Admin(
                "A001",
                "System Admin",
                "admin@example.com",
                HASH
        ));

        assertFalse(lookup.exists("S001"));

        assertThrows(
                ValidationException.class,
                () -> lookup.findById("A001")
        );
    }

    @Test
    void summaryContainsOnlyIdAndName() {
        repository.save(student("Nimal Perera"));

        StudentSummary summary =
                lookup.findById("S001").orElseThrow();

        JsonNode json = new ObjectMapper().valueToTree(summary);

        assertEquals(2, json.size());
        assertEquals("S001", json.get("id").asText());
        assertEquals("Nimal Perera", json.get("name").asText());

        assertFalse(json.has("passwordHash"));
        assertFalse(json.has("password"));
        assertFalse(json.has("email"));
        assertFalse(json.has("phone"));
    }

    @Test
    void newLookupReflectsUpdatesWithoutChangingPreviousSummary() {
        repository.save(student("Nimal Perera"));

        StudentSummary before =
                lookup.findById("S001").orElseThrow();

        repository.update(student("Nimal Silva"));

        StudentSummary after =
                lookup.findById("S001").orElseThrow();

        assertEquals("Nimal Perera", before.getName());
        assertEquals("Nimal Silva", after.getName());
    }

    @Test
    void deletedStudentIsNoLongerFound() {
        repository.save(student("Nimal Perera"));
        assertTrue(lookup.exists("S001"));

        repository.delete("S001");

        assertFalse(lookup.exists("S001"));
        assertTrue(lookup.findById("S001").isEmpty());
    }

    @Test
    void corruptedStoragePropagatesFailureEvenForMissingId()
            throws Exception {

        repository.save(student("Nimal Perera"));

        Path records = tempDir.resolve("users.txt");

        Files.writeString(
                records,
                "invalid-json\n",
                StandardOpenOption.APPEND
        );

        String before = Files.readString(records);

        assertThrows(
                StorageException.class,
                () -> lookup.exists("S001")
        );

        assertThrows(
                StorageException.class,
                () -> lookup.findById("S001")
        );

        assertThrows(
                StorageException.class,
                () -> lookup.exists("S999")
        );

        assertEquals(before, Files.readString(records));
    }
}