package com.drivingschool.instructor.repository;

import com.drivingschool.common.exception.StorageException;
import com.drivingschool.instructor.model.InstructorAvailability;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class InstructorAvailabilityRepositoryTest {

    @TempDir
    Path tempDirectory;

    private ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void shouldAllocateSequentialAvailabilityIds() {

        InstructorAvailabilityRepository repository =
                new InstructorAvailabilityRepository(
                        tempDirectory,
                        objectMapper());

        assertEquals("T001", repository.nextId("T"));
        assertEquals("T002", repository.nextId("T"));
    }

    @Test
    void shouldSaveAndReloadAvailability() {

        InstructorAvailabilityRepository repository =
                new InstructorAvailabilityRepository(
                        tempDirectory,
                        objectMapper());

        String id = repository.nextId("T");

        LocalDateTime start =
                LocalDateTime.of(2026, 11, 9, 9, 0);

        LocalDateTime end =
                LocalDateTime.of(2026, 11, 9, 12, 0);

        InstructorAvailability availability =
                new InstructorAvailability(
                        id, "I001", start, end);

        repository.save(availability);

        InstructorAvailabilityRepository reopened =
                new InstructorAvailabilityRepository(
                        tempDirectory,
                        objectMapper());

        InstructorAvailability restored =
                reopened.findByIdStrict(id).orElseThrow();

        assertEquals(id, restored.getId());
        assertEquals("I001", restored.getInstructorId());
        assertEquals(start, restored.getStartTime());
        assertEquals(end, restored.getEndTime());

        assertTrue(Files.exists(
                tempDirectory.resolve("instructor-availability.txt")));

        assertTrue(Files.exists(
                tempDirectory.resolve("instructor-availability.txt.ids.txt")));
    }

    @Test
    void shouldRejectCorruptedAvailabilityStorage() throws Exception {

        Files.writeString(
                tempDirectory.resolve("instructor-availability.txt"),
                "invalid-json"
        );

        InstructorAvailabilityRepository repository =
                new InstructorAvailabilityRepository(
                        tempDirectory,
                        objectMapper());

        assertThrows(
                StorageException.class,
                repository::readAllStrict
        );
    }
}