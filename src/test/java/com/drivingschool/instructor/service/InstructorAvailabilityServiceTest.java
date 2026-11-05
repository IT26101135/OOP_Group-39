package com.drivingschool.instructor.service;

import com.drivingschool.auth.model.Admin;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.common.exception.StorageException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.instructor.model.Instructor;
import com.drivingschool.instructor.model.InstructorAvailability;
import com.drivingschool.instructor.repository.InstructorAvailabilityRepository;
import com.drivingschool.instructor.repository.InstructorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InstructorAvailabilityServiceTest {

    @TempDir
    Path tempDirectory;

    private InstructorRepository instructorRepository;
    private InstructorAvailabilityRepository availabilityRepository;
    private AuthSessionService authSessionService;
    private InstructorAvailabilityService service;
    private MockHttpServletRequest request;

    private final LocalDateTime start =
            LocalDateTime.of(2026, 11, 9, 9, 0);

    private final LocalDateTime end = start.plusHours(2);

    @BeforeEach
    void setUp() {
        ObjectMapper mapper =
                new ObjectMapper().registerModule(new JavaTimeModule());

        instructorRepository =
                new InstructorRepository(tempDirectory, mapper);

        availabilityRepository =
                new InstructorAvailabilityRepository(tempDirectory, mapper);

        authSessionService = mock(AuthSessionService.class);
        request = new MockHttpServletRequest();

        service = new InstructorAvailabilityService(
                instructorRepository,
                availabilityRepository,
                authSessionService);
    }

    private void addInstructor(boolean active) {
        instructorRepository.save(new Instructor(
                "I001",
                "Kumar Silva",
                "kumar@example.com",
                "0771234567",
                5,
                active));
    }

    @Test
    void shouldAllowAdminToCreateAvailability() {
        addInstructor(true);

        InstructorAvailability result =
                service.addAvailability(request, "I001", start, end);

        assertEquals("T001", result.getId());
        assertEquals("I001", result.getInstructorId());
        assertEquals(start, result.getStartTime());
        assertEquals(end, result.getEndTime());

        verify(authSessionService).requireAdmin(request);
    }

    @Test
    void shouldRejectUnauthorizedRequest() {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
                .when(authSessionService).requireAdmin(request);

        assertThrows(ResponseStatusException.class,
                () -> service.addAvailability(
                        request, "I001", start, end));

        assertTrue(availabilityRepository.readAllStrict().isEmpty());
    }

    @Test
    void shouldRejectMissingInstructor() {
        assertThrows(RecordNotFoundException.class,
                () -> service.addAvailability(
                        request, "I999", start, end));
    }

    @Test
    void shouldRejectInactiveInstructor() {
        addInstructor(false);

        assertThrows(ValidationException.class,
                () -> service.addAvailability(
                        request, "I001", start, end));
    }

    @Test
    void shouldRejectInvalidInstructorId() {
        addInstructor(true);

        for (String id : new String[]{
                null, "", "I000", "I1000", "i001", " I001"
        }) {
            assertThrows(ValidationException.class,
                    () -> service.addAvailability(
                            request, id, start, end));
        }
    }

    @Test
    void shouldRejectInvalidIntervals() {
        addInstructor(true);

        assertThrows(ValidationException.class,
                () -> service.addAvailability(
                        request, "I001", null, end));

        assertThrows(ValidationException.class,
                () -> service.addAvailability(
                        request, "I001", start, null));

        assertThrows(ValidationException.class,
                () -> service.addAvailability(
                        request, "I001", start, start));

        assertThrows(ValidationException.class,
                () -> service.addAvailability(
                        request, "I001", end, start));
    }

    @Test
    void shouldAllocateSequentialIds() {
        addInstructor(true);

        InstructorAvailability first =
                service.addAvailability(request, "I001", start, end);

        InstructorAvailability second =
                service.addAvailability(
                        request, "I001", end, end.plusHours(2));

        assertEquals("T001", first.getId());
        assertEquals("T002", second.getId());
        assertEquals(2, availabilityRepository.readAllStrict().size());
    }

    @Test
    void shouldAllowOverlappingAvailability() {
        addInstructor(true);

        service.addAvailability(request, "I001", start, end);
        service.addAvailability(
                request, "I001", start.plusHours(1), end.plusHours(1));

        assertEquals(2, availabilityRepository.readAllStrict().size());
    }

    @Test
    void shouldPersistAfterReopeningRepository() {
        addInstructor(true);

        InstructorAvailability saved =
                service.addAvailability(request, "I001", start, end);

        InstructorAvailabilityRepository reopened =
                new InstructorAvailabilityRepository(
                        tempDirectory,
                        new ObjectMapper().registerModule(new JavaTimeModule()));

        InstructorAvailability restored =
                reopened.findByIdStrict(saved.getId()).orElseThrow();

        assertEquals(saved.getId(), restored.getId());
        assertEquals(saved.getStartTime(), restored.getStartTime());
        assertEquals(saved.getEndTime(), restored.getEndTime());
    }

    @Test
    void shouldRejectCorruptedInstructorStorage() throws Exception {
        Files.writeString(
                tempDirectory.resolve("instructors.txt"),
                "invalid-json");

        assertThrows(StorageException.class,
                () -> service.addAvailability(
                        request, "I001", start, end));
    }

    @Test
    void shouldRejectCorruptedAvailabilityStorage() throws Exception {
        addInstructor(true);

        Files.writeString(
                tempDirectory.resolve("instructor-availability.txt"),
                "invalid-json");

        assertThrows(StorageException.class,
                () -> service.addAvailability(
                        request, "I001", start, end));
    }
}