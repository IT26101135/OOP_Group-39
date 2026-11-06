package com.drivingschool.booking.service;

import com.drivingschool.common.exception.StorageException;
import java.io.IOException;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.learningpackage.api.EnrollmentLookup;
import com.drivingschool.learningpackage.api.EnrollmentSummary;
import com.drivingschool.student.api.StudentLookup;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    private BookingRepository bookingRepository;
    private AuthSessionService authSessionService;
    private StudentLookup studentLookup;
    private EnrollmentLookup enrollmentLookup;
    private Clock clock;

    private BookingService bookingService;

    private User student;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        authSessionService = mock(AuthSessionService.class);
        studentLookup = mock(StudentLookup.class);
        enrollmentLookup = mock(EnrollmentLookup.class);

        clock = Clock.fixed(
                Instant.parse("2026-11-01T02:30:00Z"),
                ZoneOffset.UTC
        );

        bookingService = new BookingService(
                bookingRepository,
                authSessionService,
                studentLookup,
                enrollmentLookup,
                clock
        );

        student = mock(User.class);
        request = mock(HttpServletRequest.class);
    }

    @Test
    void createBookingSuccessfullyForStudent() {

        // Test setup
        when(authSessionService.requireLogin(request))
                .thenReturn(student);

        when(student.getId()).thenReturn("S001");
        when(student.getRole()).thenReturn(Role.STUDENT);

        when(studentLookup.exists("S001")).thenReturn(true);

        EnrollmentSummary enrollment = new EnrollmentSummary(
                "EN001",
                "S001",
                "PKG001",
                10,
                LocalDateTime.of(2026, 11, 1, 0, 0),
                LocalDateTime.of(2026, 12, 1, 0, 0)
        );

        when(enrollmentLookup.findById("EN001"))
                .thenReturn(Optional.of(enrollment));

        when(bookingRepository.readAllStrict())
                .thenReturn(List.of());

        when(bookingRepository.nextId("B"))
                .thenReturn("B001");

        // Requested lesson time
        LocalDateTime startTime =
                LocalDateTime.of(2026, 11, 5, 9, 0);

        // Execute
        Booking result = bookingService.createBooking(
                request,
                "EN001",
                startTime
        );

        // Verify result
        assertNotNull(result);
        assertEquals("B001", result.getId());
        assertEquals("S001", result.getStudentId());
        assertEquals("EN001", result.getEnrollmentId());
        assertEquals(startTime, result.getStartTime());
        assertEquals(startTime.plusMinutes(60), result.getEndTime());
        assertEquals(BookingStatus.PENDING, result.getStatus());

        assertNull(result.getInstructorId());
        assertNull(result.getVehicleId());

        // Verify repository save
        verify(bookingRepository).save(result);
    }

    @Test
    void adminCannotCreateStudentBooking() {

        when(authSessionService.requireLogin(request))
                .thenReturn(student);

        when(student.getRole()).thenReturn(Role.ADMIN);

        org.springframework.web.server.ResponseStatusException exception =
                assertThrows(
                        org.springframework.web.server.ResponseStatusException.class,
                        () -> bookingService.createBooking(
                                request,
                                "EN001",
                                LocalDateTime.of(2026, 11, 5, 9, 0)
                        )
                );

        assertEquals(
                org.springframework.http.HttpStatus.FORBIDDEN,
                exception.getStatusCode()
        );

        verifyNoInteractions(studentLookup, enrollmentLookup);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void unauthenticatedUserCannotCreateBooking() {

        when(authSessionService.requireLogin(request))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED
                ));

        assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> bookingService.createBooking(
                        request,
                        "EN001",
                        LocalDateTime.of(2026, 11, 5, 9, 0)
                )
        );

        verifyNoInteractions(studentLookup, enrollmentLookup);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingRejectsEnrollmentBelongingToAnotherStudent() {

        when(authSessionService.requireLogin(request)).thenReturn(student);
        when(student.getId()).thenReturn("S001");
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(studentLookup.exists("S001")).thenReturn(true);

        EnrollmentSummary enrollment = new EnrollmentSummary(
                "EN001", "S002", "PKG001", 10,
                LocalDateTime.of(2026, 11, 1, 0, 0),
                LocalDateTime.of(2026, 12, 1, 0, 0)
        );

        when(enrollmentLookup.findById("EN001"))
                .thenReturn(Optional.of(enrollment));

        assertThrows(
                com.drivingschool.common.exception.ValidationException.class,
                () -> bookingService.createBooking(
                        request, "EN001",
                        LocalDateTime.of(2026, 11, 5, 9, 0)
                )
        );

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingRejectsMissingEnrollment() {

        when(authSessionService.requireLogin(request)).thenReturn(student);
        when(student.getId()).thenReturn("S001");
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(studentLookup.exists("S001")).thenReturn(true);

        when(enrollmentLookup.findById("EN999"))
                .thenReturn(Optional.empty());

        assertThrows(
                com.drivingschool.common.exception.RecordNotFoundException.class,
                () -> bookingService.createBooking(
                        request, "EN999",
                        LocalDateTime.of(2026, 11, 5, 9, 0)
                )
        );

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingRejectsExhaustedLessonAllowance() {

        when(authSessionService.requireLogin(request)).thenReturn(student);
        when(student.getId()).thenReturn("S001");
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(studentLookup.exists("S001")).thenReturn(true);

        EnrollmentSummary enrollment = new EnrollmentSummary(
                "EN001", "S001", "PKG001", 1,
                LocalDateTime.of(2026, 11, 1, 0, 0),
                LocalDateTime.of(2026, 12, 1, 0, 0)
        );

        when(enrollmentLookup.findById("EN001"))
                .thenReturn(Optional.of(enrollment));

        Booking existingBooking = new Booking(
                "B001", "S001", "EN001",
                LocalDateTime.of(2026, 11, 3, 9, 0),
                LocalDateTime.of(2026, 11, 3, 10, 0),
                null, null, BookingStatus.PENDING,
                LocalDateTime.of(2026, 11, 1, 9, 0)
        );

        when(bookingRepository.readAllStrict())
                .thenReturn(List.of(existingBooking));

        assertThrows(
                com.drivingschool.common.exception.ValidationException.class,
                () -> bookingService.createBooking(
                        request, "EN001",
                        LocalDateTime.of(2026, 11, 5, 9, 0)
                )
        );

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingRejectsLessonOutsideEnrollmentValidity() {

        when(authSessionService.requireLogin(request)).thenReturn(student);
        when(student.getId()).thenReturn("S001");
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(studentLookup.exists("S001")).thenReturn(true);

        EnrollmentSummary enrollment = new EnrollmentSummary(
                "EN001", "S001", "PKG001", 10,
                LocalDateTime.of(2026, 11, 1, 0, 0),
                LocalDateTime.of(2026, 11, 5, 9, 30)
        );

        when(enrollmentLookup.findById("EN001"))
                .thenReturn(Optional.of(enrollment));

        assertThrows(
                com.drivingschool.common.exception.ValidationException.class,
                () -> bookingService.createBooking(
                        request, "EN001",
                        LocalDateTime.of(2026, 11, 5, 9, 0)
                )
        );

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingRejectsPastLessonTime() {

        when(authSessionService.requireLogin(request)).thenReturn(student);
        when(student.getId()).thenReturn("S001");
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(studentLookup.exists("S001")).thenReturn(true);

        EnrollmentSummary enrollment = new EnrollmentSummary(
                "EN001", "S001", "PKG001", 10,
                LocalDateTime.of(2026, 11, 1, 0, 0),
                LocalDateTime.of(2026, 12, 1, 0, 0)
        );

        when(enrollmentLookup.findById("EN001"))
                .thenReturn(Optional.of(enrollment));

        assertThrows(
                com.drivingschool.common.exception.ValidationException.class,
                () -> bookingService.createBooking(
                        request, "EN001",
                        LocalDateTime.of(2026, 10, 31, 9, 0)
                )
        );

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingRejectsCurrentTime() {

        when(authSessionService.requireLogin(request)).thenReturn(student);
        when(student.getId()).thenReturn("S001");
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(studentLookup.exists("S001")).thenReturn(true);

        EnrollmentSummary enrollment = new EnrollmentSummary(
                "EN001", "S001", "PKG001", 10,
                LocalDateTime.of(2026, 11, 1, 0, 0),
                LocalDateTime.of(2026, 12, 1, 0, 0)
        );

        when(enrollmentLookup.findById("EN001"))
                .thenReturn(Optional.of(enrollment));

        assertThrows(
                com.drivingschool.common.exception.ValidationException.class,
                () -> bookingService.createBooking(
                        request, "EN001",
                        LocalDateTime.of(2026, 11, 1, 8, 0)
                )
        );

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingRejectsOverlappingStudentBooking() {

        when(authSessionService.requireLogin(request)).thenReturn(student);
        when(student.getId()).thenReturn("S001");
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(studentLookup.exists("S001")).thenReturn(true);

        EnrollmentSummary enrollment = new EnrollmentSummary(
                "EN001", "S001", "PKG001", 10,
                LocalDateTime.of(2026, 11, 1, 0, 0),
                LocalDateTime.of(2026, 12, 1, 0, 0)
        );

        when(enrollmentLookup.findById("EN001"))
                .thenReturn(Optional.of(enrollment));

        Booking existingBooking = new Booking(
                "B001", "S001", "EN001",
                LocalDateTime.of(2026, 11, 5, 9, 30),
                LocalDateTime.of(2026, 11, 5, 10, 30),
                null, null, BookingStatus.PENDING,
                LocalDateTime.of(2026, 11, 1, 9, 0)
        );

        when(bookingRepository.readAllStrict())
                .thenReturn(List.of(existingBooking));

        assertThrows(
                com.drivingschool.common.exception.ValidationException.class,
                () -> bookingService.createBooking(
                        request, "EN001",
                        LocalDateTime.of(2026, 11, 5, 9, 0)
                )
        );

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void cancelledBookingDoesNotConsumeLessonAllowance() {

        when(authSessionService.requireLogin(request)).thenReturn(student);
        when(student.getId()).thenReturn("S001");
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(studentLookup.exists("S001")).thenReturn(true);

        EnrollmentSummary enrollment = new EnrollmentSummary(
                "EN001", "S001", "PKG001", 1,
                LocalDateTime.of(2026, 11, 1, 0, 0),
                LocalDateTime.of(2026, 12, 1, 0, 0)
        );

        when(enrollmentLookup.findById("EN001"))
                .thenReturn(Optional.of(enrollment));

        Booking cancelledBooking = new Booking(
                "B001", "S001", "EN001",
                LocalDateTime.of(2026, 11, 3, 9, 0),
                LocalDateTime.of(2026, 11, 3, 10, 0),
                null, null, BookingStatus.CANCELLED,
                LocalDateTime.of(2026, 11, 1, 9, 0)
        );

        when(bookingRepository.readAllStrict())
                .thenReturn(List.of(cancelledBooking));

        when(bookingRepository.nextId("B")).thenReturn("B002");

        Booking result = bookingService.createBooking(
                request, "EN001",
                LocalDateTime.of(2026, 11, 5, 9, 0)
        );

        assertEquals("B002", result.getId());
        assertEquals(BookingStatus.PENDING, result.getStatus());

        verify(bookingRepository).save(result);
    }

    @Test
    void completedBookingConsumesLessonAllowance() {

        when(authSessionService.requireLogin(request)).thenReturn(student);
        when(student.getId()).thenReturn("S001");
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(studentLookup.exists("S001")).thenReturn(true);

        EnrollmentSummary enrollment = new EnrollmentSummary(
                "EN001", "S001", "PKG001", 1,
                LocalDateTime.of(2026, 11, 1, 0, 0),
                LocalDateTime.of(2026, 12, 1, 0, 0)
        );

        when(enrollmentLookup.findById("EN001"))
                .thenReturn(Optional.of(enrollment));

        Booking completedBooking = new Booking(
                "B001", "S001", "EN001",
                LocalDateTime.of(2026, 11, 3, 9, 0),
                LocalDateTime.of(2026, 11, 3, 10, 0),
                "I001", "V001", BookingStatus.COMPLETED,
                LocalDateTime.of(2026, 11, 1, 9, 0)
        );

        when(bookingRepository.readAllStrict())
                .thenReturn(List.of(completedBooking));

        assertThrows(
                com.drivingschool.common.exception.ValidationException.class,
                () -> bookingService.createBooking(
                        request, "EN001",
                        LocalDateTime.of(2026, 11, 5, 9, 0)
                )
        );

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingAllowsBackToBackLessons() {

        when(authSessionService.requireLogin(request)).thenReturn(student);
        when(student.getId()).thenReturn("S001");
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(studentLookup.exists("S001")).thenReturn(true);

        EnrollmentSummary enrollment = new EnrollmentSummary(
                "EN001", "S001", "PKG001", 10,
                LocalDateTime.of(2026, 11, 1, 0, 0),
                LocalDateTime.of(2026, 12, 1, 0, 0)
        );

        when(enrollmentLookup.findById("EN001"))
                .thenReturn(Optional.of(enrollment));

        Booking existingBooking = new Booking(
                "B001", "S001", "EN001",
                LocalDateTime.of(2026, 11, 5, 9, 0),
                LocalDateTime.of(2026, 11, 5, 10, 0),
                null, null, BookingStatus.PENDING,
                LocalDateTime.of(2026, 11, 1, 9, 0)
        );

        when(bookingRepository.readAllStrict())
                .thenReturn(List.of(existingBooking));

        when(bookingRepository.nextId("B")).thenReturn("B002");

        Booking result = bookingService.createBooking(
                request, "EN001",
                LocalDateTime.of(2026, 11, 5, 10, 0)
        );

        assertNotNull(result);
        assertEquals(BookingStatus.PENDING, result.getStatus());

        verify(bookingRepository).save(result);
    }

    @Test
    void createBookingRejectsBlankEnrollmentId() {

        when(authSessionService.requireLogin(request)).thenReturn(student);
        when(student.getId()).thenReturn("S001");
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(studentLookup.exists("S001")).thenReturn(true);

        assertThrows(
                com.drivingschool.common.exception.ValidationException.class,
                () -> bookingService.createBooking(
                        request,
                        "   ",
                        LocalDateTime.of(2026, 11, 5, 9, 0)
                )
        );

        verifyNoInteractions(enrollmentLookup);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingRejectsNullStartTime() {

        when(authSessionService.requireLogin(request)).thenReturn(student);
        when(student.getId()).thenReturn("S001");
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(studentLookup.exists("S001")).thenReturn(true);

        assertThrows(
                com.drivingschool.common.exception.ValidationException.class,
                () -> bookingService.createBooking(
                        request,
                        "EN001",
                        null
                )
        );

        verifyNoInteractions(enrollmentLookup);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingPropagatesStorageFailure() {

        when(authSessionService.requireLogin(request)).thenReturn(student);
        when(student.getId()).thenReturn("S001");
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(studentLookup.exists("S001")).thenReturn(true);

        EnrollmentSummary enrollment = new EnrollmentSummary(
                "EN001", "S001", "PKG001", 10,
                LocalDateTime.of(2026, 11, 1, 0, 0),
                LocalDateTime.of(2026, 12, 1, 0, 0)
        );

        when(enrollmentLookup.findById("EN001"))
                .thenReturn(Optional.of(enrollment));

        StorageException storageFailure = new StorageException(
                "Unable to read booking data.",
                new IOException("Test storage failure")
        );

        when(bookingRepository.readAllStrict())
                .thenThrow(storageFailure);

        StorageException result = assertThrows(
                StorageException.class,
                () -> bookingService.createBooking(
                        request,
                        "EN001",
                        LocalDateTime.of(2026, 11, 5, 9, 0)
                )
        );

        assertSame(storageFailure, result);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingRejectsNonExistingStudent() {

        when(authSessionService.requireLogin(request)).thenReturn(student);
        when(student.getId()).thenReturn("S999");
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(studentLookup.exists("S999")).thenReturn(false);

        assertThrows(
                com.drivingschool.common.exception.RecordNotFoundException.class,
                () -> bookingService.createBooking(
                        request,
                        "EN001",
                        LocalDateTime.of(2026, 11, 5, 9, 0)
                )
        );

        verifyNoInteractions(enrollmentLookup);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingPropagatesIdGenerationFailure() {

        when(authSessionService.requireLogin(request)).thenReturn(student);
        when(student.getId()).thenReturn("S001");
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(studentLookup.exists("S001")).thenReturn(true);

        EnrollmentSummary enrollment = new EnrollmentSummary(
                "EN001", "S001", "PKG001", 10,
                LocalDateTime.of(2026, 11, 1, 0, 0),
                LocalDateTime.of(2026, 12, 1, 0, 0)
        );

        when(enrollmentLookup.findById("EN001"))
                .thenReturn(Optional.of(enrollment));

        when(bookingRepository.readAllStrict()).thenReturn(List.of());

        com.drivingschool.common.exception.StorageException failure =
                new com.drivingschool.common.exception.StorageException(
                        "ID generation failed.",
                        new java.io.IOException("Test failure")
                );

        when(bookingRepository.nextId("B")).thenThrow(failure);

        assertSame(
                failure,
                assertThrows(
                        com.drivingschool.common.exception.StorageException.class,
                        () -> bookingService.createBooking(
                                request,
                                "EN001",
                                LocalDateTime.of(2026, 11, 5, 9, 0)
                        )
                )
        );

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingPropagatesStorageFailureDuringSave() {

        when(authSessionService.requireLogin(request)).thenReturn(student);
        when(student.getId()).thenReturn("S001");
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(studentLookup.exists("S001")).thenReturn(true);

        EnrollmentSummary enrollment = new EnrollmentSummary(
                "EN001", "S001", "PKG001", 10,
                LocalDateTime.of(2026, 11, 1, 0, 0),
                LocalDateTime.of(2026, 12, 1, 0, 0)
        );

        when(enrollmentLookup.findById("EN001"))
                .thenReturn(Optional.of(enrollment));

        when(bookingRepository.readAllStrict()).thenReturn(List.of());
        when(bookingRepository.nextId("B")).thenReturn("B001");

        com.drivingschool.common.exception.StorageException failure =
                new com.drivingschool.common.exception.StorageException(
                        "Saving booking failed.",
                        new java.io.IOException("Test failure")
                );

        doThrow(failure).when(bookingRepository).save(any(Booking.class));

        assertSame(
                failure,
                assertThrows(
                        com.drivingschool.common.exception.StorageException.class,
                        () -> bookingService.createBooking(
                                request,
                                "EN001",
                                LocalDateTime.of(2026, 11, 5, 9, 0)
                        )
                )
        );

        verify(bookingRepository).save(any(Booking.class));
    }
    @Test
    void confirmedBookingConsumesLessonAllowance() {

        when(authSessionService.requireLogin(request)).thenReturn(student);
        when(student.getId()).thenReturn("S001");
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(studentLookup.exists("S001")).thenReturn(true);

        EnrollmentSummary enrollment = new EnrollmentSummary(
                "EN001", "S001", "PKG001", 1,
                LocalDateTime.of(2026, 11, 1, 0, 0),
                LocalDateTime.of(2026, 12, 1, 0, 0)
        );

        when(enrollmentLookup.findById("EN001"))
                .thenReturn(Optional.of(enrollment));

        Booking confirmedBooking = new Booking(
                "B001", "S001", "EN001",
                LocalDateTime.of(2026, 11, 3, 9, 0),
                LocalDateTime.of(2026, 11, 3, 10, 0),
                "I001", "V001", BookingStatus.CONFIRMED,
                LocalDateTime.of(2026, 11, 1, 9, 0)
        );

        when(bookingRepository.readAllStrict())
                .thenReturn(List.of(confirmedBooking));

        assertThrows(
                com.drivingschool.common.exception.ValidationException.class,
                () -> bookingService.createBooking(
                        request, "EN001",
                        LocalDateTime.of(2026, 11, 5, 9, 0)
                )
        );

        verify(bookingRepository, never()).save(any());
    }
}
