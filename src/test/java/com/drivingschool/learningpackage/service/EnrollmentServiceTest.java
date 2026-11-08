/**
 * ============================================================
 * MEMBER   : IT26101136 - [Name]
 * MODULE   : Learning packages and enrollment
 * FILE     : EnrollmentServiceTest.java
 * WHAT IT DOES (simple words): Checks enrollment rules using temporary saved files.
 * REAL-LIFE EXAMPLE: Practise on spare forms before handling student records.
 * OOP USED : Encapsulation, abstraction and exception handling
 * CRUD     : CREATE, READ, UPDATE, DELETE
 * ============================================================
 */
package com.drivingschool.learningpackage.service;

import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.common.exception.*;
import com.drivingschool.learningpackage.model.Enrollment;
import com.drivingschool.learningpackage.model.LearningPackage;
import com.drivingschool.learningpackage.model.LearningRecord;
import com.drivingschool.learningpackage.repository.EnrollmentRepository;
import com.drivingschool.learningpackage.repository.LearningPackageRepository;
import com.drivingschool.student.model.Student;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Files;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EnrollmentServiceTest {
    @TempDir Path directory;
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-08T04:00:00Z"), ZoneOffset.UTC);
    private EnrollmentRepository enrollments;
    private UserRepository users;
    private BookingRepository bookings;
    private AuthSessionService auth;
    private EnrollmentService service;
    private Student student;
    private LearningPackage course;

    // Builds real temporary files and supplies a signed-in student without touching live data.
    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        users = new UserRepository(directory.toString(), mapper);
        enrollments = new EnrollmentRepository(directory.toString(), mapper);
        bookings = new BookingRepository(directory.toString(), mapper);
        LearningPackageRepository packages = new LearningPackageRepository(directory, mapper);
        course = new LearningPackage("P001", "Manual basics", "Practical lessons",
                new BigDecimal("25000.00"), 10, 30, true);
        packages.save(course);
        student = new Student("S001", "Test Student", "student@example.test",
                "$2a$10$" + "A".repeat(53), "0771234567");
        users.save(student);
        auth = mock(AuthSessionService.class);
        when(auth.requireLogin(request)).thenReturn(student);
        service = new EnrollmentService(enrollments, packages, users, auth, clock, bookings);
    }

    // Checks creation, own-list reading and a saved note update without changing the receipt.
    @Test
    void createsReadsAndUpdatesOwnEnrollment() {
        Enrollment created = service.enroll(request, "P001");
        assertEquals(1, service.ownEnrollments(request).size());
        assertEquals(LocalDate.of(2026, 12, 8), created.getValidUntil());
        service.updateLearningGoal(request, created.getId(), "  Practise parking  ");
        Enrollment saved = enrollments.findByIdStrict(created.getId()).orElseThrow();
        assertEquals("Practise parking", saved.getLearningGoal());
        assertEquals(new BigDecimal("25000.00"), saved.getAmount());
        assertEquals("PAID", saved.getPaymentStatus());
        Student savedStudent = (Student) users.findByIdStrict("S001").orElseThrow();
        assertEquals(10, savedStudent.getLessonsRemaining());
        assertEquals(created.getId(), savedStudent.getActiveEnrollmentId());
    }

    // A second active enrollment must not create a second receipt.
    @Test
    void rejectsDuplicateActiveEnrollment() {
        service.enroll(request, "P001");
        assertThrows(DuplicateRecordException.class, () -> service.enroll(request, "P001"));
        assertEquals(1, enrollments.readAllStrict().size());
    }

    // Invalid notes must leave the previous saved goal intact.
    @Test
    void rejectsLongGoalWithoutChangingSavedRecord() {
        Enrollment enrollment = service.enroll(request, "P001");
        service.updateLearningGoal(request, enrollment.getId(), "Parking");
        assertThrows(ValidationException.class, () -> service.updateLearningGoal(
                request, enrollment.getId(), "x".repeat(301)));
        assertEquals("Parking", enrollments.findByIdStrict(enrollment.getId()).orElseThrow().getLearningGoal());
    }

    // Only an unused unpaid record may be physically removed from the file.
    @Test
    void deletesInactiveUnpaidEnrollment() {
        enrollments.save(unpaid("E001", "S001"));
        service.deleteOwnEnrollment(request, "E001");
        assertTrue(enrollments.findByIdStrict("E001").isEmpty());
        assertTrue(users.findByIdStrict("S001").isPresent());
    }

    // A student's receipt remains even when they have no active lesson balance.
    @Test
    void protectsPaidReceipt() {
        Enrollment enrollment = service.enroll(request, "P001");
        student.setLessonsRemaining(0);
        assertThrows(ValidationException.class, () -> service.deleteOwnEnrollment(request, enrollment.getId()));
        assertTrue(enrollments.findByIdStrict(enrollment.getId()).isPresent());
    }

    // Active enrollments must survive a removal request, even if payment is unpaid.
    @Test
    void protectsActiveEnrollment() {
        enrollments.save(unpaid("E001", "S001"));
        student.setActiveEnrollmentId("E001");
        assertThrows(ValidationException.class, () -> service.deleteOwnEnrollment(request, "E001"));
        assertTrue(enrollments.findByIdStrict("E001").isPresent());
    }

    // Even cancelled lessons need their enrollment record for history.
    @Test
    void protectsBookingHistory() {
        enrollments.save(unpaid("E001", "S001"));
        LocalDateTime start = LocalDateTime.now(clock).plusDays(2);
        bookings.save(new Booking("B001", "S001", "E001", start, start.plusHours(1),
                null, null, BookingStatus.CANCELLED, LocalDateTime.now(clock)));
        assertThrows(ValidationException.class, () -> service.deleteOwnEnrollment(request, "E001"));
        assertTrue(enrollments.findByIdStrict("E001").isPresent());
    }

    // Neither editing nor deletion can reach another student's record.
    @Test
    void rejectsAnotherStudentsEnrollment() {
        enrollments.save(unpaid("E001", "S002"));
        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> service.updateLearningGoal(request, "E001", "Parking")).getStatusCode().value());
        assertThrows(ResponseStatusException.class, () -> service.deleteOwnEnrollment(request, "E001"));
        assertTrue(service.ownEnrollments(request).isEmpty());
    }

    // Missing and malformed IDs produce useful application errors.
    @Test
    void rejectsMissingAndMalformedIds() {
        assertThrows(RecordNotFoundException.class, () -> service.updateLearningGoal(request, "E999", "Parking"));
        assertThrows(ValidationException.class, () -> service.deleteOwnEnrollment(request, "wrong-id"));
    }

    // Older records without the optional goal or expiry still load successfully.
    @Test
    void readsLegacyEnrollmentWithoutGuessingExpiry() throws Exception {
        Files.writeString(directory.resolve("enrollments.txt"),
                "{\"id\":\"E001\",\"studentId\":\"S001\",\"packageId\":\"P001\","
                + "\"enrolledAt\":\"2026-01-01T09:00:00\",\"amount\":1000,\"paymentStatus\":\"PAID\"}\n");
        Enrollment legacy = service.ownEnrollments(request).get(0);
        assertNull(legacy.getValidUntil());
        assertNull(legacy.getLearningGoal());
    }

    // The same display-name contract gives a different heading for each record type.
    @Test
    void displaysBothLearningRecordTypesThroughTheirContract() {
        LearningRecord[] records = {course, unpaid("E001", "S001")};
        assertEquals("Manual basics", records[0].getDisplayName());
        assertEquals("Enrollment E001", records[1].getDisplayName());
    }

    // Supplies a non-active unpaid form for deletion-rule tests.
    private Enrollment unpaid(String id, String studentId) {
        return new Enrollment(id, studentId, "P001", LocalDateTime.now(clock),
                new BigDecimal("25000.00"), "UNPAID");
    }
}
