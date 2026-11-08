/**
 * ============================================================
 * MEMBER   : IT26101135 - [Name]
 * MODULE   : Integration and dashboard
 * FILE     : DashboardSummaryServiceTest.java
 * WHAT IT DOES (simple words): Checks totals come from saved records without writes.
 * REAL-LIFE EXAMPLE: Check the receptionist's daily tally against the filing cabinet.
 * OOP USED : Abstraction through the summary provider
 * CRUD     : READ (dashboard totals)
 * ============================================================
 */
package com.drivingschool.common.controller;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.common.model.DashboardSummary;
import com.drivingschool.common.service.DashboardSummaryService;
import com.drivingschool.instructor.repository.InstructorRepository;
import com.drivingschool.learningpackage.model.Enrollment;
import com.drivingschool.learningpackage.repository.EnrollmentRepository;
import com.drivingschool.learningpackage.repository.LearningPackageRepository;
import com.drivingschool.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardSummaryServiceTest {
    // Checks mixed account roles, booking states and payment states in the tally.
    @Test
    void countsOnlyStudentsAndPaidAmountsWithoutWritingRecords() {
        UserRepository users = mock(UserRepository.class);
        BookingRepository bookings = mock(BookingRepository.class);
        EnrollmentRepository enrollments = mock(EnrollmentRepository.class);
        User student = mock(User.class);
        User administrator = mock(User.class);
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(administrator.getRole()).thenReturn(Role.ADMIN);
        when(users.readAllStrict()).thenReturn(List.of(student, administrator));
        List<Booking> lessons = List.of(booking(BookingStatus.PENDING),
                booking(BookingStatus.COMPLETED), booking(BookingStatus.CANCELLED));
        when(bookings.readAllStrict()).thenReturn(lessons);
        when(enrollments.readAllStrict()).thenReturn(List.of(enrollment("E001", "PAID"),
                enrollment("E002", "UNPAID")));
        DashboardSummary summary = new DashboardSummaryService(users,
                mock(InstructorRepository.class), mock(VehicleRepository.class),
                mock(LearningPackageRepository.class), bookings, enrollments).readSummary();
        assertEquals(1, summary.getStudentCount());
        assertEquals(1, summary.getPendingBookings());
        assertEquals(1, summary.getCompletedBookings());
        assertEquals(0, summary.getConfirmedBookings());
        assertEquals(new BigDecimal("2500.50"), summary.getPaidRevenue());
        verify(users).readAllStrict();
        verify(bookings).readAllStrict();
        verify(enrollments).readAllStrict();
        verifyNoMoreInteractions(users, bookings, enrollments);
    }

    // Supplies one lesson state without needing unrelated lesson fields.
    private Booking booking(BookingStatus status) {
        Booking booking = mock(Booking.class);
        when(booking.getStatus()).thenReturn(status);
        return booking;
    }

    // Supplies a real money value so the test covers decimal addition.
    private Enrollment enrollment(String id, String status) {
        return new Enrollment(id, "S001", "P001", LocalDateTime.of(2026, 1, 1, 9, 0),
                new BigDecimal("2500.50"), status);
    }
}
