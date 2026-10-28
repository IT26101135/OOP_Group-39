
package com.drivingschool.instructor.repository;

import com.drivingschool.instructor.model.Instructor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class InstructorRepositoryTest {

    @TempDir
    Path tempDirectory;

    @Test
    void shouldSaveAndReloadInstructor() throws Exception {

        InstructorRepository repository =
                new InstructorRepository(
                        tempDirectory,
                        new ObjectMapper()
                );

        String id = repository.nextId("I");

        Instructor instructor = new Instructor(
                id,
                "Kumar Silva",
                "kumar@example.com",
                "0771234567",
                5,
                false
        );

        repository.save(instructor);

        InstructorRepository reopened =
                new InstructorRepository(
                        tempDirectory,
                        new ObjectMapper()
                );

        Instructor restored =
                reopened.findByIdStrict(id).orElseThrow();

        assertEquals(id, restored.getId());
        assertEquals("Kumar Silva", restored.getName());
        assertEquals("kumar@example.com", restored.getEmail());
        assertEquals("0771234567", restored.getPhone());
        assertEquals(5, restored.getYearsOfExperience());
        assertFalse(restored.isActive());

        assertTrue(Files.exists(
                tempDirectory.resolve("instructors.txt")
        ));

        assertTrue(Files.exists(
                tempDirectory.resolve("instructors.txt.ids.txt")
        ));
    }

    @Test
    void shouldPersistInstructorUpdates() {

        InstructorRepository repository =
                new InstructorRepository(
                        tempDirectory,
                        new ObjectMapper()
                );

        String id = repository.nextId("I");

        Instructor instructor = new Instructor(
                id,
                "Kumar Silva",
                "kumar@example.com",
                "0771234567",
                5,
                true
        );

        repository.save(instructor);

        instructor.setName("Kumar Fernando");
        instructor.setEmail("kumar.new@example.com");
        instructor.setYearsOfExperience(8);
        instructor.setActive(false);

        repository.update(instructor);

        InstructorRepository reopened =
                new InstructorRepository(
                        tempDirectory,
                        new ObjectMapper()
                );

        Instructor updated =
                reopened.findByIdStrict(id).orElseThrow();

        assertEquals("Kumar Fernando", updated.getName());
        assertEquals("kumar.new@example.com", updated.getEmail());
        assertEquals(8, updated.getYearsOfExperience());
        assertFalse(updated.isActive());
        assertEquals("0771234567", updated.getPhone());
    }

    @Test
    void shouldHandleMissingInstructorId() {

        InstructorRepository repository =
                new InstructorRepository(
                        tempDirectory,
                        new ObjectMapper()
                );

        // No instructor with ID I999 exists.
        assertTrue(
                repository.findByIdStrict("I999").isEmpty()
        );

        Instructor missingInstructor = new Instructor(
                "I999",
                "Kumar Silva",
                "kumar@example.com",
                "0771234567",
                5,
                true
        );

        assertThrows(
                com.drivingschool.common.exception.RecordNotFoundException.class,
                () -> repository.update(missingInstructor)
        );
    }

    @Test
    void shouldAllocateSequentialInstructorIds() {

        InstructorRepository repository =
                new InstructorRepository(
                        tempDirectory,
                        new ObjectMapper()
                );

        String firstId = repository.nextId("I");
        String secondId = repository.nextId("I");

        assertEquals("I001", firstId);
        assertEquals("I002", secondId);

        Instructor first = new Instructor(
                firstId,
                "Kumar Silva",
                "kumar@example.com",
                "0771234567",
                5,
                true
        );

        repository.save(first);

        InstructorRepository reopened =
                new InstructorRepository(
                        tempDirectory,
                        new ObjectMapper()
                );

        String thirdId = reopened.nextId("I");

        assertEquals("I003", thirdId);
    }

}
