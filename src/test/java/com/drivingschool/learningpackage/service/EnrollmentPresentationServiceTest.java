/**
 * MEMBER   : IT26101136 - [Name]
 * MODULE   : Learning packages and enrollment
 * FILE     : EnrollmentPresentationServiceTest.java
 * WHAT IT DOES: Checks safe read-only package labels and counts.
 * OOP USED : Encapsulation; mocked storage isolates presentation from persistence.
 * CRUD     : READ only; test fixtures are in memory.
 */
package com.drivingschool.learningpackage.service;

import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.booking.service.StudentBookingOperations;
import com.drivingschool.learningpackage.dto.EnrollmentUsageView;
import com.drivingschool.learningpackage.model.Enrollment;
import com.drivingschool.learningpackage.model.LearningPackage;
import com.drivingschool.learningpackage.repository.EnrollmentRepository;
import com.drivingschool.student.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EnrollmentPresentationServiceTest {
    private final EnrollmentRepository receipts = mock(EnrollmentRepository.class);
    private final LearningPackageCatalog packages = mock(LearningPackageCatalog.class);
    private final StudentBookingOperations bookings = mock(StudentBookingOperations.class);
    private final AuthSessionService auth = mock(AuthSessionService.class);
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final Clock clock = Clock.fixed(Instant.parse("2030-01-10T08:00:00Z"), ZoneOffset.UTC);
    private final EnrollmentPresentationService service = new EnrollmentPresentationService(receipts, packages, bookings, auth, clock);
    private Student student;
    private Enrollment current;
    private LearningPackage course;

    @BeforeEach
    void prepare() {
        // Step 1: Give the current receipt a real account balance; old receipts have no balance.
        student = new Student("S001", "Student One", "one@example.test", "$2a$10$" + "A".repeat(53), "0771234567");
        student.setActiveEnrollmentId("E002");
        student.setActivePackageId("P001");
        student.setLessonsRemaining(3);
        current = receipt("E002", "PAID");
        current.setValidUntil(LocalDate.of(2030, 2, 1));
        course = new LearningPackage("P001", "Manual basics", "Practical driving lessons", new BigDecimal("1000"), 5, 30, true);
        when(receipts.readAllStrict()).thenReturn(List.of(current));
        when(packages.listPackages()).thenReturn(List.of(course));
        when(auth.requireLogin(request)).thenReturn(student);
        when(bookings.getOwnBookings(request)).thenReturn(List.of());
    }

    @Test
    void countsOnlyMatchingEnrollmentAndStudentAndKeepsCommitmentsSeparate() {
        List<Booking> lessons = List.of(lesson("B001", "E002", BookingStatus.PENDING),
                lesson("B002", "E002", BookingStatus.CONFIRMED), lesson("B003", "E002", BookingStatus.COMPLETED),
                lesson("B004", "E002", BookingStatus.CANCELLED), lesson("B005", "E002", BookingStatus.NO_SHOW),
                lesson("B006", "E002", BookingStatus.REJECTED), lesson("B007", "E001", BookingStatus.PENDING));
        EnrollmentUsageView view = currentView(lessons);
        assertEquals(1, view.getRequested());
        assertEquals(1, view.getConfirmed());
        assertEquals(1, view.getCompleted());
        assertEquals("3", view.getLessonsLeft());
        assertEquals("Active", view.getStatus());
        assertTrue(view.isBookable());
    }

    @Test
    void historicalReceiptNeverBorrowsCurrentBalance() {
        Enrollment old = receipt("E001", "PAID");
        when(receipts.readAllStrict()).thenReturn(List.of(old, current));
        ExtendedModelMap model = new ExtendedModelMap();
        service.addStudent(request, model, List.of());
        Map<?, ?> usage = (Map<?, ?>) model.get("enrollmentUsage");
        EnrollmentUsageView history = (EnrollmentUsageView) usage.get("E001");
        assertEquals("Historical", history.getStatus());
        assertEquals("Not recorded", history.getLessonsLeft());
        assertEquals("Historical enrollment — remaining balance not recorded", history.getMessage());
        assertFalse(history.isBookable());
        assertEquals("3", ((EnrollmentUsageView) model.get("currentUsage")).getLessonsLeft());
    }

    @Test
    void expiredReceiptHasNoBookLinkAndDoesNotPromiseRenewal() {
        current.setValidUntil(LocalDate.of(2030, 1, 9));
        EnrollmentUsageView view = currentView(List.of());
        assertEquals("Expired", view.getStatus());
        assertFalse(view.isBookable());
        assertNotEquals("/bookings", view.getNextUrl());
        assertTrue(view.getMessage().contains("Buying another package is not available yet"));
    }

    @Test
    void validityIncludesToday() {
        current.setValidUntil(LocalDate.of(2030, 1, 10));
        assertEquals("Active", currentView(List.of()).getStatus());
    }

    @Test
    void nullValidityHasExactFallbackAndRemainsUsable() {
        current.setValidUntil(null);
        EnrollmentUsageView view = currentView(List.of());
        assertEquals("Validity not recorded", view.getValidity());
        assertEquals("Active", view.getStatus());
    }

    @Test
    void missingPackageIsSafeAndNotBookable() {
        when(packages.listPackages()).thenReturn(List.of());
        EnrollmentUsageView view = currentView(List.of());
        assertEquals("Package unavailable (P001)", view.getPackageName());
        assertEquals("Not recorded", view.getType());
        assertEquals("Unavailable", view.getStatus());
        assertFalse(view.isBookable());
    }

    @Test
    void usedUpReceiptHasNoBookLink() {
        student.setLessonsRemaining(0);
        EnrollmentUsageView view = currentView(List.of());
        assertNull(student.getActiveEnrollmentId());
        assertNull(student.getActivePackageId());
        assertEquals("Used up", view.getStatus());
        assertFalse(view.isBookable());
        assertNotEquals("/bookings", view.getNextUrl());
    }

    @Test
    void unpaidAndCancelledReceiptsNeverOfferBooking() {
        for (String state : List.of("AWAITING_PAYMENT", "CANCELLED")) {
            current.setStatus(state);
            EnrollmentUsageView view = currentView(List.of());
            assertEquals(state.equals("CANCELLED") ? "Cancelled" : "Awaiting payment", view.getStatus());
            assertFalse(view.isBookable());
            assertNotEquals("/bookings", view.getNextUrl());
        }
    }

    @Test
    void fullCommitmentsKeepPackageActiveButLinkToExistingBookings() {
        student.setLessonsRemaining(2);
        EnrollmentUsageView view = currentView(List.of(lesson("B001", "E002", BookingStatus.PENDING),
                lesson("B002", "E002", BookingStatus.CONFIRMED)));
        assertEquals("Active", view.getStatus());
        assertEquals("View my bookings", view.getNextLabel());
        assertTrue(view.getMessage().contains("already requested"));
    }

    @Test
    void bookingWithMissingEnrollmentDoesNotProduceAnError() {
        Map<String, EnrollmentUsageView> rows = service.forBookings(List.of(lesson("B001", "E999", BookingStatus.PENDING)));
        assertEquals("Unavailable", rows.get("B001").getStatus());
        assertEquals("Package unavailable", rows.get("B001").getPackageName());
    }

    @Test
    void catalogsAreLoadedOnceAndHistoricalBookingUsesItsOwnPackage() {
        Enrollment old = new Enrollment("E001", "S001", "P002", LocalDateTime.now(clock), new BigDecimal("2000"), "PAID");
        LearningPackage other = new LearningPackage("P002", "Other package", "Historical course details", new BigDecimal("2000"), 8, 30, true);
        when(receipts.readAllStrict()).thenReturn(List.of(old, current));
        when(packages.listPackages()).thenReturn(List.of(course, other));
        Map<String, EnrollmentUsageView> rows = service.forBookings(List.of(
                lesson("B001", "E001", BookingStatus.PENDING), lesson("B002", "E002", BookingStatus.CONFIRMED)));
        assertEquals("Other package", rows.get("B001").getPackageName());
        assertEquals("Manual basics", rows.get("B002").getPackageName());
        verify(receipts, times(1)).readAllStrict();
        verify(packages, times(1)).listPackages();
        verifyNoMoreInteractions(receipts, packages);
    }

    @Test
    void studentCatalogsLoadedOnceAndOtherStudentsReceiptsAreNotExposed() {
        Enrollment other = new Enrollment("E003", "S002", "P001", LocalDateTime.now(clock), new BigDecimal("1000"), "PAID");
        when(receipts.readAllStrict()).thenReturn(List.of(current, other));
        ExtendedModelMap model = new ExtendedModelMap();
        service.addStudent(request, model, List.of());
        assertEquals(1, ((List<?>) model.get("enrollments")).size());
        assertEquals(1, ((Map<?, ?>) model.get("enrollmentUsage")).size());
        verify(receipts, times(1)).readAllStrict();
        verify(packages, times(1)).listPackages();
        verifyNoInteractions(bookings);
    }

    private EnrollmentUsageView currentView(List<Booking> lessons) {
        ExtendedModelMap model = new ExtendedModelMap();
        service.addStudent(request, model, lessons);
        return (EnrollmentUsageView) model.get("currentUsage");
    }

    private Enrollment receipt(String id, String state) {
        return new Enrollment(id, "S001", "P001", LocalDateTime.now(clock).minusDays(30), new BigDecimal("1000"), "UNPAID", state);
    }

    private Booking lesson(String id, String enrollment, BookingStatus state) {
        LocalDateTime start = LocalDateTime.now(clock).plusDays(1);
        boolean assigned = state == BookingStatus.CONFIRMED || state == BookingStatus.COMPLETED || state == BookingStatus.NO_SHOW;
        return new Booking(id, "S001", enrollment, start, start.plusHours(1),
                assigned ? "I001" : null, assigned ? "V001" : null, state, start.minusDays(1));
    }
}
