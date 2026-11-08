/**
 * ============================================================
 * MEMBER   : IT26101136 - [Name]
 * MODULE   : Learning packages and enrollment
 * FILE     : EnrollmentPaymentServiceTest.java
 * WHAT IT DOES (simple words): Checks cashier decisions on temporary receipts.
 * REAL-LIFE EXAMPLE: Two receptionists practise handling the same receipt safely.
 * OOP USED : Encapsulation, abstraction and exception handling
 * CRUD     : CREATE, READ and UPDATE enrollment requests
 * ============================================================
 */
package com.drivingschool.learningpackage.service;

import com.drivingschool.auth.model.AccountStatus;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.learningpackage.model.Enrollment;
import com.drivingschool.learningpackage.model.LearningPackage;
import com.drivingschool.learningpackage.repository.EnrollmentRepository;
import com.drivingschool.learningpackage.repository.LearningPackageRepository;
import com.drivingschool.student.model.Student;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpServletRequest;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EnrollmentPaymentServiceTest {
    @TempDir Path folder;
    private UserRepository users;
    private EnrollmentRepository receipts;
    private LearningPackageRepository packages;
    private BookingRepository bookings;
    private EnrollmentService service;
    private AuthSessionService auth;
    private Student student;
    private LearningPackage course;
    private final MockHttpServletRequest admin = new MockHttpServletRequest();
    private final Clock clock = Clock.fixed(Instant.parse("2030-01-10T08:00:00Z"), ZoneOffset.UTC);

    // Uses spare files with one old unpaid request so no real data can be changed.
    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        users = new UserRepository(folder.toString(), mapper);
        receipts = new EnrollmentRepository(folder.toString(), mapper);
        packages = new LearningPackageRepository(folder, mapper);
        bookings = new BookingRepository(folder.toString(), mapper);
        student = new Student("S001", "Test Learner", "student@example.test", "$2a$10$" + "A".repeat(53), "0771234567");
        users.save(student);
        course = new LearningPackage("P001", "Manual lessons", "Driving practice lessons", new BigDecimal("2000"), 5, 30, true);
        packages.save(course);
        receipts.save(new Enrollment("E001", "S001", "P001", LocalDateTime.now(clock).minusDays(5), new BigDecimal("1500"), "UNPAID"));
        auth = mock(AuthSessionService.class);
        service = new EnrollmentService(receipts, packages, users, auth, clock, bookings);
    }

    // Confirmation starts validity today and uses the captured amount rather than today's price.
    @Test
    void confirmsPaymentAndUnlocksLessonsFromToday() {
        Enrollment paid = service.markPaid(admin, "E001");
        assertEquals("PAID", paid.getPaymentStatus());
        assertEquals("PAID", paid.getStatus());
        assertEquals(new BigDecimal("1500"), paid.getAmount());
        assertEquals(LocalDate.of(2030, 2, 9), paid.getValidUntil());
        Student saved = (Student) users.findByIdStrict("S001").orElseThrow();
        assertEquals("E001", saved.getActiveEnrollmentId());
        assertEquals("P001", saved.getActivePackageId());
        assertEquals(5, saved.getLessonsRemaining());
    }

    // An inactive course leaves the receipt unpaid and the balance unchanged.
    @Test
    void rejectsInactivePackage() {
        course.setActive(false);
        packages.update(course);
        assertTrue(assertThrows(ValidationException.class, () -> service.markPaid(admin, "E001")).getMessage().contains("inactive"));
        assertWaitingWithoutLessons();
    }

    // Suspending a student prevents confirmation even if a valid receipt was received.
    @Test
    void rejectsSuspendedStudent() {
        student.setStatus(AccountStatus.SUSPENDED);
        users.update(student);
        assertTrue(assertThrows(ValidationException.class, () -> service.markPaid(admin, "E001")).getMessage().contains("Suspended"));
        assertWaitingWithoutLessons();
    }

    // A repeated confirmation cannot refill lessons that have already been used.
    @Test
    void repeatedConfirmationDoesNotRefillBalance() {
        service.markPaid(admin, "E001");
        Student saved = (Student) users.findByIdStrict("S001").orElseThrow();
        saved.setLessonsRemaining(3);
        users.update(saved);
        assertTrue(assertThrows(ValidationException.class, () -> service.markPaid(admin, "E001")).getMessage().contains("already handled"));
        assertEquals(3, ((Student) users.findByIdStrict("S001").orElseThrow()).getLessonsRemaining());
    }

    // Paid receipts remain permanent even if cancellation is requested afterwards.
    @Test
    void refusesCancellationAfterPayment() {
        service.markPaid(admin, "E001");
        assertTrue(assertThrows(ValidationException.class, () -> service.cancelRequest(admin, "E001", "Changed mind")).getMessage().contains("Paid"));
        assertEquals("PAID", receipts.findByIdStrict("E001").orElseThrow().getStatus());
    }

    // Cancelling an unpaid legacy receipt rejects pending requests and keeps their explanation.
    @Test
    void cancellationRejectsPendingQueueAndAllowsNewEnrollment() {
        LocalDateTime time = LocalDateTime.now(clock).plusDays(3);
        bookings.save(new Booking("B001", "S001", "E001", time, time.plusHours(1), null, null, BookingStatus.PENDING, time.minusDays(1)));
        service.cancelRequest(admin, "E001", "  Receipt was withdrawn  ");
        Enrollment cancelled = receipts.findByIdStrict("E001").orElseThrow();
        assertEquals("CANCELLED", cancelled.getStatus());
        assertEquals("Receipt was withdrawn", cancelled.getCancellationReason());
        assertEquals(BookingStatus.REJECTED, bookings.findByIdStrict("B001").orElseThrow().getStatus());
        assertTrue(bookings.findByIdStrict("B001").orElseThrow().getRejectionReason().contains("cancelled"));
        when(auth.requireLogin(admin)).thenReturn(student);
        assertEquals("AWAITING_PAYMENT", service.enroll(admin, "P001").getStatus());
    }

    // Repeated cancellation or confirmation after cancellation leaves the handled record intact.
    @Test
    void cancelledRequestCannotBeHandledAgain() {
        service.cancelRequest(admin, "E001", null);
        assertThrows(ValidationException.class, () -> service.cancelRequest(admin, "E001", "Again"));
        assertThrows(ValidationException.class, () -> service.markPaid(admin, "E001"));
        assertEquals("CANCELLED", receipts.findByIdStrict("E001").orElseThrow().getStatus());
    }

    // A note that is too long is refused before changing either file.
    @Test
    void rejectsLongCancellationReason() {
        assertThrows(ValidationException.class, () -> service.cancelRequest(admin, "E001", "x".repeat(301)));
        assertWaitingWithoutLessons();
    }

    // Two administrators confirming at once must produce exactly one successful decision.
    @Test
    void concurrentConfirmationsHandleReceiptOnce() throws Exception {
        raceDecisions(false);
    }

    // A payment and cancellation arriving together cannot both succeed.
    @Test
    void confirmationAndCancellationCannotBothSucceed() throws Exception {
        raceDecisions(true);
    }

    // Starts two callers together and checks the loser gets the same friendly explanation.
    private void raceDecisions(boolean cancelSecond) throws Exception {
        ExecutorService desk = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Boolean> first = desk.submit(() -> attemptDecision(start, false));
            Future<Boolean> second = desk.submit(() -> attemptDecision(start, cancelSecond));
            start.countDown();
            boolean firstWon = first.get(10, TimeUnit.SECONDS);
            boolean secondWon = second.get(10, TimeUnit.SECONDS);
            assertNotEquals(firstWon, secondWon);
        } finally {
            start.countDown();
            desk.shutdownNow();
        }
    }

    // Returns success only for the first admin; a second admin must hear 'already handled'.
    private boolean attemptDecision(CountDownLatch start, boolean cancel) throws Exception {
        start.await(5, TimeUnit.SECONDS);
        try {
            if (cancel) service.cancelRequest(admin, "E001", "Cancelled by second admin");
            else service.markPaid(admin, "E001");
            return true;
        } catch (ValidationException exception) {
            assertTrue(exception.getMessage().contains("already handled"));
            return false;
        }
    }

    // Verifies failed decisions did not grant a package or start validity.
    private void assertWaitingWithoutLessons() {
        Enrollment saved = receipts.findByIdStrict("E001").orElseThrow();
        assertEquals("AWAITING_PAYMENT", saved.getStatus());
        assertNull(saved.getValidUntil());
        assertEquals(0, ((Student) users.findByIdStrict("S001").orElseThrow()).getLessonsRemaining());
    }
}
