package com.drivingschool.instructor.service;

import com.drivingschool.common.exception.StorageException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.instructor.api.InstructorBookingInfo;
import com.drivingschool.instructor.model.Instructor;
import com.drivingschool.instructor.model.InstructorAvailability;
import com.drivingschool.instructor.repository.InstructorAvailabilityRepository;
import com.drivingschool.instructor.repository.InstructorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InstructorLookupServiceTest {

    @TempDir
    Path tempDirectory;

    private InstructorRepository instructorRepository;
    private InstructorAvailabilityRepository availabilityRepository;
    private InstructorLookupService lookup;

    private final LocalDateTime base =
            LocalDateTime.of(2026, 11, 9, 9, 0);

    @BeforeEach
    void setUp() {
        ObjectMapper mapper =
                new ObjectMapper().registerModule(new JavaTimeModule());

        instructorRepository =
                new InstructorRepository(tempDirectory, mapper);

        availabilityRepository =
                new InstructorAvailabilityRepository(tempDirectory, mapper);

        lookup = new InstructorLookupService(
                instructorRepository, availabilityRepository);
    }

    private void addInstructor(String id, boolean active) {
        instructorRepository.save(new Instructor(
                id, "Kumar Silva", "kumar@example.com",
                "0771234567", 5, active));
    }

    private void addAvailability(int startHour, int endHour) {
        availabilityRepository.save(new InstructorAvailability(
                availabilityRepository.nextId("T"),
                "I001",
                base.plusHours(startHour),
                base.plusHours(endHour)));
    }

    @Test
    void shouldFindExistingInstructor() {
        addInstructor("I001", true);

        Optional<InstructorBookingInfo> result =
                lookup.findById("I001");

        assertTrue(result.isPresent());
        assertEquals("I001", result.get().id());
        assertEquals("Kumar Silva", result.get().name());
        assertTrue(result.get().active());
    }

    @Test
    void shouldReturnEmptyForMissingInstructor() {
        assertTrue(lookup.findById("I999").isEmpty());
    }

    @Test
    void shouldReturnInactiveInstructorInfo() {
        addInstructor("I001", false);

        assertFalse(lookup.findById("I001").orElseThrow().active());
    }

    @Test
    void shouldRejectInvalidInstructorIds() {
        for (String id : new String[]{null, "", "I000", "I1000", "i001", " I001"}) {
            assertThrows(ValidationException.class,
                    () -> lookup.findById(id));
        }
    }

    @Test
    void shouldReturnFalseWithoutAvailability() {
        addInstructor("I001", true);

        assertFalse(lookup.isWorkingAvailable(
                "I001", base, base.plusHours(1)));
    }

    @Test
    void shouldAllowExactAndContainedCoverage() {
        addInstructor("I001", true);
        addAvailability(0, 4);

        assertTrue(lookup.isWorkingAvailable(
                "I001", base, base.plusHours(4)));

        assertTrue(lookup.isWorkingAvailable(
                "I001", base.plusHours(1), base.plusHours(3)));
    }

    @Test
    void shouldAllowAdjacentCoverage() {
        addInstructor("I001", true);
        addAvailability(0, 2);
        addAvailability(2, 4);

        assertTrue(lookup.isWorkingAvailable(
                "I001", base, base.plusHours(4)));
    }

    @Test
    void shouldAllowOverlappingCoverage() {
        addInstructor("I001", true);
        addAvailability(0, 3);
        addAvailability(2, 5);

        assertTrue(lookup.isWorkingAvailable(
                "I001", base, base.plusHours(5)));
    }

    @Test
    void shouldRejectCoverageWithGap() {
        addInstructor("I001", true);
        addAvailability(0, 2);
        addAvailability(3, 5);

        assertFalse(lookup.isWorkingAvailable(
                "I001", base, base.plusHours(5)));
    }

    @Test
    void shouldRejectInsufficientCoverage() {
        addInstructor("I001", true);
        addAvailability(1, 3);

        assertFalse(lookup.isWorkingAvailable(
                "I001", base, base.plusHours(3)));

        assertFalse(lookup.isWorkingAvailable(
                "I001", base.plusHours(1), base.plusHours(4)));
    }

    @Test
    void shouldRejectMissingAndInactiveInstructor() {
        assertFalse(lookup.isWorkingAvailable(
                "I999", base, base.plusHours(1)));

        addInstructor("I001", false);
        addAvailability(0, 4);

        assertFalse(lookup.isWorkingAvailable(
                "I001", base, base.plusHours(2)));
    }

    @Test
    void shouldRejectInvalidIntervals() {
        addInstructor("I001", true);

        assertThrows(ValidationException.class,
                () -> lookup.isWorkingAvailable("I001", null, base));

        assertThrows(ValidationException.class,
                () -> lookup.isWorkingAvailable("I001", base, null));

        assertThrows(ValidationException.class,
                () -> lookup.isWorkingAvailable("I001", base, base));

        assertThrows(ValidationException.class,
                () -> lookup.isWorkingAvailable(
                        "I001", base.plusHours(2), base));

        assertThrows(ValidationException.class,
                () -> lookup.isWorkingAvailable(
                        "I999", null, null));
    }

    @Test
    void shouldPropagateCorruptedInstructorStorage() throws Exception {
        Files.writeString(
                tempDirectory.resolve("instructors.txt"),
                "invalid-json");

        assertThrows(StorageException.class,
                () -> lookup.findById("I001"));
    }

    @Test
    void shouldPropagateCorruptedAvailabilityStorage() throws Exception {
        addInstructor("I001", true);

        Files.writeString(
                tempDirectory.resolve("instructor-availability.txt"),
                "invalid-json");

        assertThrows(StorageException.class,
                () -> lookup.isWorkingAvailable(
                        "I001", base, base.plusHours(1)));
    }
}