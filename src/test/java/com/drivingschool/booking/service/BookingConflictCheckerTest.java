/**
 * ============================================================
 * MEMBER   : IT26101134 - [Name]
 * MODULE   : Bookings and scheduling
 * FILE     : BookingConflictCheckerTest.java
 * WHAT IT DOES (simple words): Checks lesson rules with spare accounts and receipts.
 * REAL-LIFE EXAMPLE: Practises checking tickets before reserving driving slots.
 * OOP USED : Abstraction and exception handling
 * CRUD     : READ and UPDATE booking rules
 * ============================================================
 */
package com.drivingschool.booking.service;

import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.booking.model.*;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.instructor.model.*;
import com.drivingschool.instructor.repository.*;
import com.drivingschool.learningpackage.model.*;
import com.drivingschool.learningpackage.repository.LearningPackageRepository;
import com.drivingschool.learningpackage.repository.EnrollmentRepository;
import com.drivingschool.student.model.Student;
import com.drivingschool.vehicle.model.*;
import com.drivingschool.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingConflictCheckerTest {
    BookingRepository bookings = mock(BookingRepository.class);
    AvailabilityRepository availability = mock(AvailabilityRepository.class);
    InstructorLeaveRepository leaves = mock(InstructorLeaveRepository.class);
    InstructorRepository instructors = mock(InstructorRepository.class);
    VehicleRepository vehicles = mock(VehicleRepository.class);
    LearningPackageRepository packages = mock(LearningPackageRepository.class);
    UserRepository users = mock(UserRepository.class);
    Clock clock = Clock.fixed(Instant.parse("2030-01-01T08:00:00Z"), ZoneOffset.UTC);
    Student student;
    LearningPackage pack;
    Instructor instructor;
    Vehicle vehicle;
    Booking target;
    BookingConflictChecker checker;
    EnrollmentRepository enrollments = mock(EnrollmentRepository.class);

    // Checks a scheduling rule with temporary or mocked records, keeping live data untouched.
    @BeforeEach void setUp() {
        student = new Student("S001", "Demo Student", "student@example.test",
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("StrongPass123"), "+94771234567");
        student.setActivePackageId("P001"); student.setActiveEnrollmentId("E001"); student.setLessonsRemaining(5);
        pack = new LearningPackage("P001", "Manual course", "A suitable package description.",
                new BigDecimal("10000.00"), 10, 90, true);
        instructor = new Instructor("I001", "Teacher One", "teacher@example.test", "0771234567", 5, true);
        instructor.setVehicleTypes(List.of("MANUAL"));
        vehicle = new Vehicle("V001", "ABC1234", "Make", "Model", TransmissionType.MANUAL,
                VehicleCategory.CAR, VehicleOperationalStatus.IN_SERVICE);
        LocalDateTime start = LocalDateTime.parse("2030-01-02T10:00");
        target = new Booking("B001", "S001", "E001", start, start.plusHours(1), null, null,
                BookingStatus.PENDING, LocalDateTime.parse("2030-01-01T08:00"));
        when(users.findByIdStrict("S001")).thenReturn(Optional.of(student));
        when(enrollments.findByIdStrict("E001")).thenReturn(Optional.of(new Enrollment("E001", "S001", "P001",
                LocalDateTime.now(clock), new BigDecimal("10000"), "PAID")));
        when(packages.findByIdStrict("P001")).thenReturn(Optional.of(pack));
        when(instructors.findByIdStrict("I001")).thenReturn(Optional.of(instructor));
        when(vehicles.findByIdStrict("V001")).thenReturn(Optional.of(vehicle));
        when(availability.readAllStrict()).thenReturn(List.of(new Availability("H001", "I001",
                DayOfWeek.WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(17, 0))));
        when(leaves.readAllStrict()).thenReturn(List.of());
        when(bookings.readAllStrict()).thenReturn(List.of());
        checker = new BookingConflictChecker(bookings, availability, leaves, instructors, vehicles,
                packages, users, clock, new PaidEnrollmentGuard(enrollments, users, packages, clock));
    }

    // Checks a scheduling rule with temporary or mocked records, keeping live data untouched.
    @Test void acceptsFreeValidSlot() { assertNull(checker.conflictReason(target, "I001", "V001")); }
    // Checks a scheduling rule with temporary or mocked records, keeping live data untouched.
    @Test void rejectsPastTime() {
        Booking past = new Booking("B002", "S001", "E001", LocalDateTime.parse("2029-12-31T10:00"),
                LocalDateTime.parse("2029-12-31T11:00"), null, null, BookingStatus.PENDING,
                LocalDateTime.parse("2029-12-30T09:00"));
        assertEquals("The lesson time must be in the future.", checker.conflictReason(past, "I001", "V001"));
    }
    // Checks a scheduling rule with temporary or mocked records, keeping live data untouched.
    @Test void rejectsMissingActiveEnrollment() {
        student.setLessonsRemaining(0);
        assertTrue(checker.conflictReason(target, "I001", "V001").contains("Paid enrollment"));
    }
    // Checks a scheduling rule with temporary or mocked records, keeping live data untouched.
    @Test void rejectsInstructorOutsideAvailability() {
        when(availability.readAllStrict()).thenReturn(List.of());
        assertTrue(checker.conflictReason(target, "I001", "V001").contains("not working"));
    }
    // Checks a scheduling rule with temporary or mocked records, keeping live data untouched.
    @Test void rejectsInstructorLeave() {
        when(leaves.readAllStrict()).thenReturn(List.of(new InstructorLeave("L001", "I001",
                LocalDate.parse("2030-01-02"), "Day off")));
        assertTrue(checker.conflictReason(target, "I001", "V001").contains("on leave"));
    }
    // Checks a scheduling rule with temporary or mocked records, keeping live data untouched.
    @Test void rejectsVehicleNotAvailable() {
        vehicle.setOperationalStatus(VehicleOperationalStatus.MAINTENANCE);
        assertTrue(checker.conflictReason(target, "I001", "V001").contains("maintenance"));
    }
    // Checks a scheduling rule with temporary or mocked records, keeping live data untouched.
    @Test void rejectsVehicleTypeMismatch() {
        vehicle.setVehicleCategory(VehicleCategory.MOTORCYCLE);
        assertTrue(checker.conflictReason(target, "I001", "V001").contains("type does not match"));
    }
    // Checks a scheduling rule with temporary or mocked records, keeping live data untouched.
    @Test void rejectsInstructorOverlap() {
        when(bookings.readAllStrict()).thenReturn(List.of(other("B002", "S002", "I001", "V002")));
        assertTrue(checker.conflictReason(target, "I001", "V001").contains("instructor already"));
    }
    // Checks a scheduling rule with temporary or mocked records, keeping live data untouched.
    @Test void rejectsVehicleOverlap() {
        when(bookings.readAllStrict()).thenReturn(List.of(other("B002", "S002", "I002", "V001")));
        assertTrue(checker.conflictReason(target, "I001", "V001").contains("vehicle already"));
    }
    // Checks a scheduling rule with temporary or mocked records, keeping live data untouched.
    @Test void rejectsStudentOverlap() {
        when(bookings.readAllStrict()).thenReturn(List.of(other("B002", "S001", "I002", "V002")));
        assertTrue(checker.conflictReason(target, "I001", "V001").contains("student already"));
    }
    // Builds a second reservation to check which person or vehicle already occupies the slot.
    private Booking other(String id, String studentId, String instructorId, String vehicleId) {
        return new Booking(id, studentId, "E001", target.getStartTime(), target.getEndTime(),
                instructorId, vehicleId, BookingStatus.CONFIRMED, target.getCreatedAt());
    }
}
