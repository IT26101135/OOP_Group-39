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

    // A request keeps its captured price but gives no lessons or validity before payment.
    @Test
    void createsReadsAndUpdatesOwnEnrollment() {
        Enrollment created = service.enroll(request, "P001");
        assertEquals(1, service.ownEnrollments(request).size());
        assertNull(created.getValidUntil());
        assertEquals("AWAITING_PAYMENT", created.getStatus());
        assertEquals("ENR-" + created.getId(), created.getReferenceCode());
        service.updateLearningGoal(request, created.getId(), "  Practise parking  ");
        Enrollment saved = enrollments.findByIdStrict(created.getId()).orElseThrow();
        assertEquals("Practise parking", saved.getLearningGoal());
        assertEquals(new BigDecimal("25000.00"), saved.getAmount());
        assertEquals("UNPAID", saved.getPaymentStatus());
        Student savedStudent = (Student) users.findByIdStrict("S001").orElseThrow();
        assertEquals(0, savedStudent.getLessonsRemaining());
        assertNull(savedStudent.getActiveEnrollmentId());
        assertNull(savedStudent.getActivePackageId());
    }

    // A second payment request must not create another receipt or lesson balance.
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
        Enrollment enrollment = new Enrollment("E001", "S001", "P001",
                LocalDateTime.now(clock), course.getPrice(), "PAID");
        enrollments.save(enrollment);
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
        assertEquals("PAID", legacy.getStatus());
        assertEquals("PAID", legacy.getPaymentStatus());
        assertEquals("ENR-E001", legacy.getReferenceCode());
    }

    // Receipts from before payment fields existed stay paid and retain the saved balance.
    @Test
    void loadsOldFormatWithoutPaymentFieldsAsPaidAndActive() throws Exception {
        student.setActivePackageId("P001");
        student.setLessonsRemaining(4);
        student.setActiveEnrollmentId("E001");
        users.update(student);
        Files.writeString(directory.resolve("enrollments.txt"),
                "{\"id\":\"E001\",\"studentId\":\"S001\",\"packageId\":\"P001\","
                + "\"enrolledAt\":\"2026-01-01T09:00:00\",\"amount\":1000}\n");
        Enrollment old = enrollments.findByIdStrict("E001").orElseThrow();
        assertEquals("PAID", old.getStatus());
        assertEquals("PAID", old.getPaymentStatus());
        assertEquals("ENR-E001", old.getReferenceCode());
        Student loaded = (Student) users.findByIdStrict("S001").orElseThrow();
        assertEquals("E001", loaded.getActiveEnrollmentId());
        assertEquals("P001", loaded.getActivePackageId());
        assertEquals(4, loaded.getLessonsRemaining());
    }

    // Old explicitly unpaid receipts remain requests; they must never become paid by default.
    @Test
    void preservesOldUnpaidReceiptAndNewCancelledState() throws Exception {
        Enrollment request = unpaid("E001", "S001");
        enrollments.save(request);
        assertEquals("AWAITING_PAYMENT", enrollments.findByIdStrict("E001").orElseThrow().getStatus());
        request.setStatus("CANCELLED");
        enrollments.update(request);
        Enrollment cancelled = enrollments.findByIdStrict("E001").orElseThrow();
        assertEquals("CANCELLED", cancelled.getStatus());
        assertEquals("UNPAID", cancelled.getPaymentStatus());
        assertFalse(cancelled.isPaid());
        assertNull(cancelled.getValidUntil());
    }

    // Cancelled history does not prevent a student from requesting a package again.
    @Test
    void permitsNewRequestAfterCancellation() {
        Enrollment cancelled = unpaid("E001", "S001");
        cancelled.setStatus("CANCELLED");
        enrollments.save(cancelled);
        Enrollment next = service.enroll(request, "P001");
        assertEquals("AWAITING_PAYMENT", next.getStatus());
        assertNotEquals(cancelled.getId(), next.getId());
        assertEquals(0, ((Student) users.findByIdStrict("S001").orElseThrow()).getLessonsRemaining());
    }

    // The payment page lookup refuses another student's reference and invalid states.
    @Test
    void protectsInstructionsAndRejectsUnknownState() {
        Enrollment other = unpaid("E001", "S002");
        enrollments.save(other);
        assertThrows(ResponseStatusException.class, () -> service.ownEnrollment(request, "E001"));
        assertThrows(ValidationException.class, () -> other.setStatus("APPROVED"));
        assertEquals("AWAITING_PAYMENT", other.getStatus());
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
