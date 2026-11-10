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
import com.drivingschool.common.exception.DuplicateRecordException;
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
        assertTrue(assertThrows(ValidationException.class, () -> service.markPaid(admin, "E001")).getMessage().contains("no longer available"));
        assertWaitingWithoutLessons();
    }

    // Suspending a student prevents confirmation even if a valid receipt was received.
    @Test
    void rejectsSuspendedStudent() {
        student.setStatus(AccountStatus.SUSPENDED);
        users.update(student);
        assertTrue(assertThrows(ValidationException.class, () -> service.markPaid(admin, "E001")).getMessage().contains("suspended"));
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

    @Test
    void expiredRenewalKeepsOldReceiptAndLessonHistory() {
        prepareRenewal(false);
        LocalDateTime start = LocalDateTime.now(clock).minusDays(2);
        bookings.save(new Booking("B001", "S001", "E001", start, start.plusHours(1),
                "I001", "V001", BookingStatus.COMPLETED, start.minusDays(1)));
        Enrollment next = service.enroll(admin, "P001");
        assertEquals("AWAITING_PAYMENT", next.getStatus());
        assertEquals("E001", ((Student) users.findByIdStrict("S001").orElseThrow()).getActiveEnrollmentId());
        service.markPaid(admin, next.getId());
        assertEquals(2, receipts.readAllStrict().size());
        Enrollment old = receipts.findByIdStrict("E001").orElseThrow();
        assertEquals("PAID", old.getStatus());
        assertEquals(new BigDecimal("1500"), old.getAmount());
        assertEquals(LocalDate.now(clock).minusDays(1), old.getValidUntil());
        assertEquals(BookingStatus.COMPLETED, bookings.findByIdStrict("B001").orElseThrow().getStatus());
        assertEquals(next.getId(), ((Student) users.findByIdStrict("S001").orElseThrow()).getActiveEnrollmentId());
        assertEquals(5, ((Student) users.findByIdStrict("S001").orElseThrow()).getLessonsRemaining());
    }

    @Test
    void firstPaymentStillHandlesAnOlderUnpaidPendingRequest() {
        LocalDateTime start = LocalDateTime.now(clock).plusDays(2);
        bookings.save(new Booking("B001", "S001", "E001", start, start.plusHours(1),
                null, null, BookingStatus.PENDING, LocalDateTime.now(clock)));
        assertEquals("PAID", service.markPaid(admin, "E001").getStatus());
        assertEquals(5, ((Student) users.findByIdStrict("S001").orElseThrow()).getLessonsRemaining());
    }

    @Test
    void usedUpStudentCanRenewAndOldPaymentCannotBeConfirmedAgain() {
        prepareRenewal(true);
        Enrollment next = service.enroll(admin, "P001");
        service.markPaid(admin, next.getId());
        assertEquals(2, receipts.readAllStrict().size());
        assertEquals(5, ((Student) users.findByIdStrict("S001").orElseThrow()).getLessonsRemaining());
        assertThrows(ValidationException.class, () -> service.markPaid(admin, "E001"));
    }

    @Test
    void pendingLessonPreventsRenewal() {
        assertOpenLessonPreventsRenewal(BookingStatus.PENDING);
    }

    @Test
    void confirmedLessonPreventsRenewal() {
        assertOpenLessonPreventsRenewal(BookingStatus.CONFIRMED);
    }

    private void assertOpenLessonPreventsRenewal(BookingStatus status) {
        prepareRenewal(false);
        LocalDateTime start = LocalDateTime.now(clock).plusDays(2);
        bookings.save(new Booking("B001", "S001", "E001", start, start.plusHours(1),
                status == BookingStatus.CONFIRMED ? "I001" : null,
                status == BookingStatus.CONFIRMED ? "V001" : null, status, LocalDateTime.now(clock)));
        assertTrue(assertThrows(DuplicateRecordException.class, () -> service.enroll(admin, "P001"))
                .getMessage().contains("pending or confirmed"));
        assertEquals(1, receipts.readAllStrict().size());
    }

    @Test
    void paymentRechecksOpenLessonsCreatedAfterRenewalRequest() {
        prepareRenewal(false);
        Enrollment next = service.enroll(admin, "P001");
        LocalDateTime start = LocalDateTime.now(clock).plusDays(2);
        bookings.save(new Booking("B001", "S001", "E001", start, start.plusHours(1),
                null, null, BookingStatus.PENDING, LocalDateTime.now(clock)));
        assertThrows(ValidationException.class, () -> service.markPaid(admin, next.getId()));
        assertEquals("AWAITING_PAYMENT", receipts.findByIdStrict(next.getId()).orElseThrow().getStatus());
        assertEquals("E001", ((Student) users.findByIdStrict("S001").orElseThrow()).getActiveEnrollmentId());
    }

    @Test
    void usablePackageIncludingItsExpiryDayCannotBeReplaced() {
        service.markPaid(admin, "E001");
        when(auth.requireLogin(admin)).thenAnswer(invocation -> users.findByIdStrict("S001").orElseThrow());
        Enrollment paid = receipts.findByIdStrict("E001").orElseThrow();
        paid.setValidUntil(LocalDate.now(clock));
        receipts.update(paid);
        assertTrue(assertThrows(DuplicateRecordException.class, () -> service.enroll(admin, "P001"))
                .getMessage().contains("still have lessons"));
        assertEquals(1, receipts.readAllStrict().size());
    }

    @Test
    void paymentRechecksUsablePackageRestoredAfterRenewalRequest() {
        prepareRenewal(true);
        Enrollment next = service.enroll(admin, "P001");
        Student restored = (Student) users.findByIdStrict("S001").orElseThrow();
        restored.setLessonsRemaining(2);
        restored.setActivePackageId("P001");
        restored.setActiveEnrollmentId("E001");
        users.update(restored);
        assertThrows(ValidationException.class, () -> service.markPaid(admin, next.getId()));
        assertEquals("AWAITING_PAYMENT", receipts.findByIdStrict(next.getId()).orElseThrow().getStatus());
        assertEquals(2, ((Student) users.findByIdStrict("S001").orElseThrow()).getLessonsRemaining());
    }

    @Test
    void concurrentRenewalRequestsCreateOnlyOneAwaitingReceipt() throws Exception {
        prepareRenewal(false);
        ExecutorService callers = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Callable<Boolean> attempt = () -> {
                start.await(5, TimeUnit.SECONDS);
                try {
                    service.enroll(admin, "P001");
                    return true;
                } catch (DuplicateRecordException error) {
                    assertTrue(error.getMessage().contains("awaiting payment"));
                    return false;
                }
            };
            Future<Boolean> first = callers.submit(attempt);
            Future<Boolean> second = callers.submit(attempt);
            start.countDown();
            assertNotEquals(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS));
            assertEquals(2, receipts.readAllStrict().size());
        } finally {
            start.countDown();
            callers.shutdownNow();
        }
    }

    private void prepareRenewal(boolean usedUp) {
        service.markPaid(admin, "E001");
        Student paid = (Student) users.findByIdStrict("S001").orElseThrow();
        if (usedUp) {
            paid.setLessonsRemaining(0);
            users.update(paid);
        } else {
            Enrollment old = receipts.findByIdStrict("E001").orElseThrow();
            old.setValidUntil(LocalDate.now(clock).minusDays(1));
            receipts.update(old);
        }
        when(auth.requireLogin(admin)).thenAnswer(invocation -> users.findByIdStrict("S001").orElseThrow());
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
