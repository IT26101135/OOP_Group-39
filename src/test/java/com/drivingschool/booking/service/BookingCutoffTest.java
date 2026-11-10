/**
 * ============================================================
 * MEMBER   : IT26101134 - [Name]
 * MODULE   : Bookings and scheduling
 * FILE     : BookingCutoffTest.java
 * WHAT IT DOES (simple words): Checks lesson rules with spare accounts and receipts.
 * REAL-LIFE EXAMPLE: Practises checking tickets before reserving driving slots.
 * OOP USED : Abstraction and exception handling
 * CRUD     : READ and UPDATE booking rules
 * ============================================================
 */
package com.drivingschool.booking.service;

import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.booking.model.*;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.instructor.repository.InstructorRepository;
import com.drivingschool.learningpackage.repository.LearningPackageRepository;
import com.drivingschool.student.model.Student;
import com.drivingschool.vehicle.repository.VehicleRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingCutoffTest {
    // Checks a scheduling rule with temporary or mocked records, keeping live data untouched.
    private Student student() { return new Student("S001", "Student One", "one@example.test",
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("StrongPass123"), "+94771234567"); }
    // Checks a scheduling rule with temporary or mocked records, keeping live data untouched.
    private BookingService service(Booking booking, BookingRepository repo, AuthSessionService auth) {
        when(repo.findByIdStrict(booking.getId())).thenReturn(Optional.of(booking));
        return new BookingService(repo, mock(LearningPackageRepository.class),
                mock(InstructorRepository.class), mock(VehicleRepository.class), auth,
                mock(UserRepository.class), Clock.fixed(Instant.parse("2030-01-01T08:00:00Z"), ZoneOffset.UTC),
                mock(PaidEnrollmentGuard.class));
    }
    // Checks a scheduling rule with temporary or mocked records, keeping live data untouched.
    @Test void changeInside24HoursIsRejected() {
        Student owner = student(); AuthSessionService auth = mock(AuthSessionService.class);
        when(auth.requireLogin(any())).thenReturn(owner);
        Booking b = new Booking("B001", "S001", "E001", LocalDateTime.parse("2030-01-02T07:59"),
                LocalDateTime.parse("2030-01-02T08:59"), null, null, BookingStatus.PENDING,
                LocalDateTime.parse("2030-01-01T07:00"));
        BookingRepository repo = mock(BookingRepository.class);
        BookingService service = service(b, repo, auth);
        assertThrows(com.drivingschool.common.exception.ValidationException.class,
                () -> service.cancelOwnBooking(mock(HttpServletRequest.class), "B001"));
        verify(repo, never()).update(any());
    }
    // Checks a scheduling rule with temporary or mocked records, keeping live data untouched.
    @Test void changeExactly24HoursAwayIsAllowed() {
        Student owner = student(); AuthSessionService auth = mock(AuthSessionService.class);
        when(auth.requireLogin(any())).thenReturn(owner);
        Booking b = new Booking("B001", "S001", "E001", LocalDateTime.parse("2030-01-02T08:00"),
                LocalDateTime.parse("2030-01-02T09:00"), null, null, BookingStatus.PENDING,
                LocalDateTime.parse("2030-01-01T07:00"));
        BookingRepository repo = mock(BookingRepository.class);
        BookingService service = service(b, repo, auth);
        assertDoesNotThrow(() -> service.cancelOwnBooking(mock(HttpServletRequest.class), "B001"));
        assertEquals(BookingStatus.CANCELLED, b.getStatus()); verify(repo).update(b);
    }
}
