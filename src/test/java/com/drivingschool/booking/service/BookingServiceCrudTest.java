/**
 * ============================================================
 * MEMBER   : IT26101134 - [Name]
 * MODULE   : Bookings, FIFO queue and scheduling
 * FILE     : BookingServiceCrudTest.java
 * WHAT IT DOES (simple words): Checks booking CRUD, ownership, FIFO assignment and filter validation.
 * REAL-LIFE EXAMPLE: Check a spare waiting list before handling real lesson requests.
 * OOP USED : Encapsulation, ordering polymorphism and exception handling
 * CRUD     : Tests the related booking operations using temporary data
 * ============================================================
 */
package com.drivingschool.booking.service;

import com.drivingschool.auth.model.Admin;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.common.exception.*;
import com.drivingschool.instructor.repository.InstructorRepository;
import com.drivingschool.learningpackage.model.LearningPackage;
import com.drivingschool.learningpackage.repository.LearningPackageRepository;
import com.drivingschool.student.model.Student;
import com.drivingschool.vehicle.repository.VehicleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceCrudTest {
    @TempDir Path directory;
    private BookingRepository bookings;
    private BookingService service;
    private AuthSessionService auth;
    private Student student;
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final Clock clock = Clock.fixed(Instant.parse("2030-01-01T08:00:00Z"), ZoneOffset.UTC);

    // Builds a real temporary student/course filing cabinet with an active lesson balance.
    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        bookings = new BookingRepository(directory.toString(), mapper);
        LearningPackageRepository packages = new LearningPackageRepository(directory, mapper);
        packages.save(new LearningPackage("P001", "Manual basics", "Practical driving lessons",
                new BigDecimal("1000.00"), 5, 30, true));
        student = new Student("S001", "Student One", "student@example.test", "$2a$10$" + "A".repeat(53), "0771234567");
        student.setActivePackageId("P001");
        student.setLessonsRemaining(5);
        student.setActiveEnrollmentId("E001");
        UserRepository users = new UserRepository(directory.toString(), mapper);
        users.save(student);
        auth = mock(AuthSessionService.class);
        when(auth.requireLogin(request)).thenReturn(student);
        service = new BookingService(bookings, packages, new InstructorRepository(directory, mapper),
                new VehicleRepository(directory, mapper), auth, users, clock);
    }

    // Create, list, cancel and delete must all act on the same owned request.
    @Test
    void supportsBookingCrudWithoutChangingLessonBalance() {
        Booking created = service.requestLesson(request, "P001", LocalDateTime.of(2030, 1, 4, 10, 0));
        assertEquals(created.getId(), service.getOwnBookings(request).get(0).getId());
        assertEquals(1, service.getOwnQueuePosition(request, created.getId()));
        service.cancelOwnBooking(request, created.getId());
        assertEquals(BookingStatus.CANCELLED, bookings.findByIdStrict(created.getId()).orElseThrow().getStatus());
        service.deleteCancelledBooking(request, created.getId());
        assertTrue(bookings.findByIdStrict(created.getId()).isEmpty());
        assertEquals(5, student.getLessonsRemaining());
    }

    // An ID changed in the browser cannot cancel or remove another student's request.
    @Test
    void rejectsAnotherStudentsRequest() {
        bookings.save(booking("B001", "S002", 1, 4));
        assertThrows(ResponseStatusException.class, () -> service.cancelOwnBooking(request, "B001"));
        assertThrows(ResponseStatusException.class, () -> service.deleteCancelledBooking(request, "B001"));
        assertEquals(BookingStatus.PENDING, bookings.findByIdStrict("B001").orElseThrow().getStatus());
    }

    // A pending request must be cancelled first, and missing IDs give a friendly error.
    @Test
    void protectsPendingRequestsAndReportsMissingRecords() {
        Booking created = service.requestLesson(request, "P001", LocalDateTime.of(2030, 1, 4, 10, 0));
        assertThrows(ValidationException.class, () -> service.deleteCancelledBooking(request, created.getId()));
        assertThrows(RecordNotFoundException.class, () -> service.cancelOwnBooking(request, "B999"));
        assertEquals(1, bookings.readAllStrict().size());
    }

    // Administrator ordering must honour submission time even when lesson times run in reverse.
    @Test
    void administratorViewKeepsFifoAheadOfChronologicalHistory() {
        Booking older = booking("B001", "S001", 1, 7);
        Booking newer = booking("B002", "S001", 2, 6);
        bookings.save(newer);
        bookings.save(older);
        when(auth.requireAdmin(request)).thenReturn(new Admin("A001", "Admin User",
                "admin@example.test", "$2a$10$" + "A".repeat(53)));
        List<Booking> result = service.getAllBookings(request);
        assertEquals("B001", result.get(0).getId());
        assertEquals("B002", result.get(1).getId());
        assertEquals(1, service.getQueuePosition(request, "B001"));
        assertThrows(ValidationException.class, () -> service.assign(request, "B002", "I001", "V001"));
    }

    // Invalid filter dates become friendly errors instead of internal server errors.
    @Test
    void validatesFilterDatesAndIncludesRequestedInstructorMatches() {
        Booking pending = booking("B001", "S001", 1, 4);
        pending.setRequestedInstructorId("I001");
        bookings.save(pending);
        assertThrows(ValidationException.class, () -> service.filterBookings(request, "wrong-date", null, null, null, null));
        assertEquals(1, service.filterBookings(request, "2030-01-04", "2030-01-04", "I001", null, "pending").size());
        assertTrue(service.filterBookings(request, null, null, "I002", null, null).isEmpty());
    }

    // Separates the submission date from the desired lesson date for FIFO checks.
    private Booking booking(String id, String studentId, int submittedDay, int lessonDay) {
        LocalDateTime start = LocalDateTime.of(2030, 1, lessonDay, 10, 0);
        return new Booking(id, studentId, "E001", start, start.plusHours(1), null, null,
                BookingStatus.PENDING, LocalDateTime.of(2030, 1, submittedDay, 8, 0));
    }
}
