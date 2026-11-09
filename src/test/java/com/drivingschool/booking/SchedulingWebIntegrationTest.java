/**
 * ============================================================
 * MEMBER   : IT26101134 - [Name]
 * MODULE   : Scheduling integration with instructor availability
 * FILE     : SchedulingWebIntegrationTest.java
 * WHAT IT DOES (simple words): Checks real scheduling routes with spare data and real sessions.
 * REAL-LIFE EXAMPLE: Rehearse the reception and instructor hand-off before a demonstration.
 * OOP USED : Service interfaces, role checks and friendly exceptions
 * CRUD     : CREATE, READ, UPDATE, DELETE for temporary schedule records
 * ============================================================
 */
package com.drivingschool.booking;

import com.drivingschool.auth.model.AccountStatus;
import com.drivingschool.auth.model.Admin;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.booking.service.BookingService;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.service.AuthService;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.instructor.model.Availability;
import com.drivingschool.instructor.model.Instructor;
import com.drivingschool.instructor.repository.AvailabilityRepository;
import com.drivingschool.instructor.service.InstructorAccountService;
import com.drivingschool.learningpackage.model.LearningPackage;
import com.drivingschool.learningpackage.model.Enrollment;
import com.drivingschool.learningpackage.repository.EnrollmentRepository;
import com.drivingschool.learningpackage.service.LearningPackageService;
import com.drivingschool.learningpackage.service.EnrollmentService;
import com.drivingschool.student.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SchedulingWebIntegrationTest {
    @TempDir static Path directory;
    @Autowired MockMvc mvc;
    @Autowired AuthService authentication;
    @Autowired AuthSessionService sessions;
    @Autowired InstructorAccountService instructorAccounts;
    @Autowired LearningPackageService packages;
    @Autowired EnrollmentService enrollments;
    @Autowired EnrollmentRepository receipts;
    @Autowired com.drivingschool.learningpackage.repository.LearningPackageRepository packageRecords;
    @Autowired UserRepository users;
    @Autowired AvailabilityRepository hours;
    @Autowired BookingRepository bookings;
    @Autowired BookingService bookingService;
    @Autowired Clock clock;
    private Instructor instructor;
    private Student student;
    private LearningPackage course;
    private MockHttpSession instructorSession;
    private MockHttpSession studentSession;
    private LocalDateTime lessonTime;
    private static final String PASSWORD = "TestingPass123!";

    // Keeps every write in a temporary filing cabinet, with normal application seeding off.
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.data-directory", () -> directory.toString());
        registry.add("app.admin-seed.enabled", () -> false);
        registry.add("app.demo-seed.enabled", () -> false);
    }

    // Gives each test a separate instructor, active student, course and free working block.
    @BeforeEach
    void setUp() {
        String number = String.valueOf(users.readAllStrict().size());
        instructor = instructorAccounts.create("Teacher " + number, "teacher" + number + "@example.test",
                "0771234567", 5, PASSWORD);
        student = authentication.registerStudent("Learner " + number,
                "learner" + number + "@example.test", "0771234567", PASSWORD);
        student.setStatus(AccountStatus.ACTIVE);
        users.update(student);
        instructorSession = login(instructor.getEmail());
        studentSession = login(student.getEmail());
        course = packages.createPackage("Course " + number, "Practical driving lessons",
                new BigDecimal("1000.00"), 5, 30, true);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(studentSession);
        preparePaidEnrollment(request);
        lessonTime = LocalDate.now(clock).plusDays(3).atTime(10, 0);
        hours.save(new Availability(hours.nextId("H"), instructor.getId(), lessonTime.getDayOfWeek(),
                LocalTime.of(9, 0), LocalTime.of(12, 0)));
    }

    // Scheduling tests start after payment; enrollment creation itself is now unpaid.
    private void preparePaidEnrollment(MockHttpServletRequest request) {
        // Step 1: Record the request through the real enrollment service.
        Enrollment enrollment = enrollments.enroll(request, course.getId());
        // Step 2: Prepare a paid fixture instead of assuming enrollment grants lessons.
        enrollment.setStatus("PAID");
        enrollment.setValidUntil(LocalDate.now(clock).plusDays(course.getValidityDays()));
        receipts.update(enrollment);
        student.setActivePackageId(course.getId());
        student.setLessonsRemaining(course.getLessonCount());
        student.setActiveEnrollmentId(enrollment.getId());
        users.update(student);
    }

    // A real student form must create, show, cancel and delete its own request with CSRF protection.
    @Test
    void supportsBookingCrudThroughStudentRoutes() throws Exception {
        Booking created = requestLesson();
        mvc.perform(get("/bookings").session(studentSession)).andExpect(status().isOk())
                .andExpect(model().attributeExists("openBookings", "historyBookings", "queuePositions"));
        String token = token("/bookings", studentSession);
        mvc.perform(post("/bookings/cancel").session(studentSession).param("_csrf", token)
                .param("bookingId", created.getId())).andExpect(redirectedUrl("/bookings"));
        assertEquals(BookingStatus.CANCELLED, bookings.findByIdStrict(created.getId()).orElseThrow().getStatus());
        mvc.perform(post("/bookings/delete").session(studentSession).param("_csrf", token)
                .param("bookingId", created.getId())).andExpect(redirectedUrl("/bookings"));
        assertTrue(bookings.findByIdStrict(created.getId()).isEmpty());
    }

    // The separate details UI is read-only and does not spend a lesson or alter the queue.
    @Test
    void ownerCanReadBookingDetailsWithoutChangingRecords() throws Exception {
        Booking booking = requestLesson();
        byte[] before = java.nio.file.Files.readAllBytes(directory.resolve("bookings.txt"));
        int balance = ((Student) users.findByIdStrict(student.getId()).orElseThrow()).getLessonsRemaining();
        mvc.perform(get("/bookings/" + booking.getId() + "/details").session(studentSession))
                .andExpect(status().isOk()).andExpect(view().name("booking/details"))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(model().attribute("booking", org.hamcrest.Matchers.hasProperty("id", org.hamcrest.Matchers.is(booking.getId()))))
                .andExpect(model().attribute("queuePosition", org.hamcrest.Matchers.greaterThan(0)))
                .andExpect(model().attribute("backUrl", "/bookings"))
                .andExpect(model().attribute("packageDetails", org.hamcrest.Matchers.hasProperty("packageName", org.hamcrest.Matchers.is(course.getName()))));
        assertArrayEquals(before, java.nio.file.Files.readAllBytes(directory.resolve("bookings.txt")));
        assertEquals(balance, ((Student) users.findByIdStrict(student.getId()).orElseThrow()).getLessonsRemaining());
    }

    // Administrator access does not depend on being the student named on the lesson.
    @Test
    void adminCanReadAnotherStudentsBookingDetails() throws Exception {
        Booking booking = requestLesson();
        MockHttpSession admin = (MockHttpSession) adminRequest().getSession(false);
        mvc.perform(get("/bookings/" + booking.getId() + "/details").session(admin))
                .andExpect(status().isOk()).andExpect(view().name("booking/details"))
                .andExpect(model().attribute("backUrl", "/admin/bookings"))
                .andExpect(model().attribute("booking", org.hamcrest.Matchers.hasProperty("studentId", org.hamcrest.Matchers.is(student.getId()))));
    }

    // Changing a URL cannot reveal another student's record or package information.
    @Test
    void otherStudentCannotReadBookingDetails() throws Exception {
        Booking booking = requestLesson();
        Student other = authentication.registerStudent("Other Learner", "other-" + student.getId() + "@example.test", "0771234567", PASSWORD);
        mvc.perform(get("/bookings/" + booking.getId() + "/details").session(login(other.getEmail())))
                .andExpect(status().isForbidden())
                .andExpect(model().attributeDoesNotExist("booking", "packageDetails"));
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> bookingService.getBookingDetails(instructorRequest(), booking.getId()));
    }

    // Login and role guards also protect direct navigation, including guesses of missing IDs.
    @Test
    void detailsRequireLoginAndRejectInstructorRole() throws Exception {
        Booking booking = requestLesson();
        mvc.perform(get("/bookings/" + booking.getId() + "/details"))
                .andExpect(redirectedUrl("/login?required"));
        mvc.perform(get("/bookings/" + booking.getId() + "/details").session(instructorSession))
                .andExpect(status().isForbidden())
                .andExpect(model().attributeDoesNotExist("booking", "packageDetails"));
        mvc.perform(get("/bookings/B999/details").session(instructorSession))
                .andExpect(status().isForbidden());
    }

    // Invalid and unknown numbers must produce friendly errors without changing stored requests.
    @Test
    void detailsHandleInvalidAndMissingBookingIds() throws Exception {
        mvc.perform(get("/bookings/B000/details").session(studentSession))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/bookings/B999/details").session(studentSession))
                .andExpect(status().isNotFound());
    }

    // History remains readable when imported data contains a missing receipt or course.
    @Test
    void detailsHandleMissingEnrollmentAndPackage() throws Exception {
        Booking booking = requestLesson();
        Enrollment receipt = receipts.findByIdStrict(booking.getEnrollmentId()).orElseThrow();
        receipts.delete(receipt.getId());
        mvc.perform(get("/bookings/" + booking.getId() + "/details").session(studentSession))
                .andExpect(status().isOk())
                .andExpect(model().attribute("packageDetails", org.hamcrest.Matchers.hasProperty("packageName", org.hamcrest.Matchers.is("Package unavailable"))));
        receipts.save(receipt);
        packageRecords.delete(course.getId());
        mvc.perform(get("/bookings/" + booking.getId() + "/details").session(studentSession))
                .andExpect(status().isOk())
                .andExpect(model().attribute("packageDetails", org.hamcrest.Matchers.hasProperty("packageName", org.hamcrest.Matchers.is("Package unavailable (" + course.getId() + ")"))));
    }

    // An assigned request and historical rejection have details, but no unassigned FIFO position.
    @Test
    void assignedAndHistoricalDetailsDoNotShowQueuePosition() throws Exception {
        Booking booking = requestLesson();
        booking.assignForInstructor(instructor.getId(), "V001");
        bookings.update(booking);
        mvc.perform(get("/bookings/" + booking.getId() + "/details").session(studentSession))
                .andExpect(status().isOk()).andExpect(model().attribute("queuePosition", 0));
        booking.reject("The instructor cannot teach at this time.");
        bookings.update(booking);
        mvc.perform(get("/bookings/" + booking.getId() + "/details").session(studentSession))
                .andExpect(status().isOk()).andExpect(model().attribute("queuePosition", 0))
                .andExpect(model().attribute("booking", org.hamcrest.Matchers.hasProperty("rejectionReason", org.hamcrest.Matchers.is("The instructor cannot teach at this time."))));
    }

    // The new URL has no write handler; existing POST action routes remain separate.
    @Test
    void detailsUrlDoesNotAcceptPost() throws Exception {
        Booking booking = requestLesson();
        mvc.perform(post("/bookings/" + booking.getId() + "/details").session(studentSession)
                .param("_csrf", token("/bookings", studentSession)))
                .andExpect(status().isMethodNotAllowed());
        assertEquals(BookingStatus.PENDING, bookings.findByIdStrict(booking.getId()).orElseThrow().getStatus());
    }

    private MockHttpServletRequest instructorRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(instructorSession);
        return request;
    }

    // Rescheduling must cancel the old request only after a new pending request is saved.
    @Test
    void rescheduleCreatesNewPendingRequest() throws Exception {
        Booking old = requestLesson();
        mvc.perform(post("/bookings/reschedule").session(studentSession)
                .param("_csrf", token("/bookings", studentSession)).param("bookingId", old.getId())
                .param("startTime", lessonTime.plusHours(1).toString())).andExpect(redirectedUrl("/bookings"));
        assertEquals(BookingStatus.CANCELLED, bookings.findByIdStrict(old.getId()).orElseThrow().getStatus());
        Booking replacement = ownPendingBooking();
        assertNotEquals(old.getId(), replacement.getId());
        assertEquals(lessonTime.plusHours(1), replacement.getStartTime());
    }

    // Both attendance routes must preserve status and balance when a future lesson is blocked.
    @Test
    void futureAttendanceIsRefusedForAdminAndInstructor() throws Exception {
        Booking booking = requestLesson();
        booking.confirm(instructor.getId(), "V001");
        bookings.update(booking);
        MockHttpServletRequest teacher = new MockHttpServletRequest();
        teacher.setSession(instructorSession);
        ValidationException error = assertThrows(ValidationException.class,
                () -> bookingService.completeAssignedLesson(teacher, booking.getId()));
        assertTrue(error.getMessage().contains("Completion becomes available at"));
        assertThrows(ValidationException.class, () -> bookingService.complete(adminRequest(), booking.getId()));
        assertThrows(ValidationException.class, () -> bookingService.markNoShowAssignedLesson(teacher, booking.getId()));
        assertEquals(BookingStatus.CONFIRMED, bookings.findByIdStrict(booking.getId()).orElseThrow().getStatus());
        assertEquals(5, ((Student) users.findByIdStrict(student.getId()).orElseThrow()).getLessonsRemaining());
    }

    // A delivered lesson can be completed once and spends exactly one ticket.
    @Test
    void endedLessonCanBeCompleted() {
        LocalDateTime end = LocalDateTime.now(clock).minusMinutes(1);
        Booking booking = new Booking(bookings.nextId("B"), student.getId(), student.getActiveEnrollmentId(),
                end.minusHours(1), end, instructor.getId(), "V001", BookingStatus.CONFIRMED, end.minusDays(1));
        bookings.save(booking);
        bookingService.complete(adminRequest(), booking.getId());
        assertEquals(BookingStatus.COMPLETED, bookings.findByIdStrict(booking.getId()).orElseThrow().getStatus());
        assertEquals(4, ((Student) users.findByIdStrict(student.getId()).orElseThrow()).getLessonsRemaining());
        assertThrows(ValidationException.class, () -> bookingService.complete(adminRequest(), booking.getId()));
    }

    // Only this test opts into the explicit demo switch; normal tests keep the default gate.
    @Test
    void demoSettingAllowsInstructorToCompleteFutureLesson() throws Exception {
        Booking booking = requestLesson();
        booking.confirm(instructor.getId(), "V001");
        bookings.update(booking);
        MockHttpServletRequest teacher = new MockHttpServletRequest();
        teacher.setSession(instructorSession);
        org.springframework.test.util.ReflectionTestUtils.setField(bookingService, "requireLessonEnded", false);
        try {
            bookingService.completeAssignedLesson(teacher, booking.getId());
            assertEquals(BookingStatus.COMPLETED, bookings.findByIdStrict(booking.getId()).orElseThrow().getStatus());
            assertEquals(4, ((Student) users.findByIdStrict(student.getId()).orElseThrow()).getLessonsRemaining());
        } finally {
            org.springframework.test.util.ReflectionTestUtils.setField(bookingService, "requireLessonEnded", true);
        }
    }

    // The slot-based request path uses the same cap as the simpler service overload.
    @Test
    void requestCapAllowsReplacementButRefusesExtraRequest() throws Exception {
        Student saved = (Student) users.findByIdStrict(student.getId()).orElseThrow();
        saved.setLessonsRemaining(1);
        users.update(saved);
        Booking old = requestLesson();
        ValidationException error = assertThrows(ValidationException.class,
                () -> bookingService.requestLesson(studentRequest(), course.getId(), instructor.getId(), lessonTime.plusHours(1)));
        assertTrue(error.getMessage().contains("You have 1 lessons left and 1 already requested"));
        Booking replacement = bookingService.rescheduleOwnBooking(studentRequest(), old.getId(), lessonTime.plusHours(1));
        assertEquals(BookingStatus.CANCELLED, bookings.findByIdStrict(old.getId()).orElseThrow().getStatus());
        assertEquals(BookingStatus.PENDING, replacement.getStatus());
    }

    // Legacy over-cap data cannot gain assignments or replacements; the original is preserved.
    @Test
    void overCapAssignmentAndRescheduleAreRefused() throws Exception {
        clearTemporaryBookings();
        Booking first = requestLesson();
        bookingService.requestLesson(studentRequest(), course.getId(), instructor.getId(), lessonTime.plusHours(1));
        Student saved = (Student) users.findByIdStrict(student.getId()).orElseThrow();
        saved.setLessonsRemaining(1);
        users.update(saved);
        assertThrows(ValidationException.class, () -> bookingService.assign(adminRequest(), first.getId(), instructor.getId(), "V001"));
        assertThrows(ValidationException.class, () -> bookingService.rescheduleOwnBooking(studentRequest(), first.getId(), lessonTime.minusHours(1)));
        assertEquals(BookingStatus.PENDING, bookings.findByIdStrict(first.getId()).orElseThrow().getStatus());
        assertNull(bookings.findByIdStrict(first.getId()).orElseThrow().getInstructorId());
    }

    // Wrong-role requests and missing tokens must be stopped before any schedule change.
    @Test
    void keepsRoleAndCsrfGuards() throws Exception {
        mvc.perform(get("/bookings").session(instructorSession)).andExpect(status().isForbidden());
        mvc.perform(get("/instructor/availability").session(studentSession)).andExpect(status().isForbidden());
        mvc.perform(post("/instructor/availability/update").session(instructorSession)
                .param("id", "H001").param("dayOfWeek", "MONDAY")
                .param("startTime", "09:00").param("endTime", "12:00")).andExpect(status().isForbidden());
        mvc.perform(get("/admin/bookings").session(studentSession)).andExpect(status().isForbidden());
    }

    // The new instructor update route must edit only that instructor's working block.
    @Test
    void updatesOwnedWorkingHoursThroughController() throws Exception {
        Availability own = ownBlock();
        mvc.perform(post("/instructor/availability/update").session(instructorSession)
                .param("_csrf", token("/instructor/availability", instructorSession))
                .param("id", own.getId()).param("dayOfWeek", own.getDayOfWeek().name())
                .param("startTime", "08:00").param("endTime", "12:00"))
                .andExpect(redirectedUrl("/instructor/availability"));
        assertEquals(LocalTime.of(8, 0), hours.findByIdStrict(own.getId()).orElseThrow().getStartTime());
    }

    // A forged form cannot book an unpaid course, and neither service overload may bypass payment.
    @Test
    void unpaidEnrollmentBlocksBothRequestPaths() throws Exception {
        changePaymentState("AWAITING_PAYMENT");
        MockHttpServletRequest request = studentRequest();
        assertThrows(ValidationException.class, () -> bookingService.requestLesson(request, course.getId(), lessonTime));
        assertThrows(ValidationException.class, () -> bookingService.requestLesson(request, course.getId(), instructor.getId(), lessonTime));
        mvc.perform(post("/bookings").session(studentSession).param("_csrf", token("/bookings", studentSession))
                .param("packageId", course.getId()).param("instructorId", instructor.getId())
                .param("startTime", lessonTime.toString())).andExpect(status().isBadRequest())
                .andExpect(model().attribute("message", org.hamcrest.Matchers.containsString("Paid enrollment")));
    }

    // Failed rescheduling must leave the original request and its date intact.
    @Test
    void unpaidEnrollmentCannotReschedule() throws Exception {
        Booking old = requestLesson();
        changePaymentState("AWAITING_PAYMENT");
        assertThrows(ValidationException.class, () -> bookingService.rescheduleOwnBooking(studentRequest(), old.getId(), lessonTime.plusHours(1)));
        Booking saved = bookings.findByIdStrict(old.getId()).orElseThrow();
        assertEquals(BookingStatus.PENDING, saved.getStatus());
        assertEquals(lessonTime, saved.getStartTime());
    }

    // The oldest unpaid request is rejected with a reason before invalid resource IDs are used.
    @Test
    void adminAssignmentRejectsUnpaidHead() throws Exception {
        clearTemporaryBookings();
        Booking old = requestLesson();
        changePaymentState("AWAITING_PAYMENT");
        Booking result = bookingService.assign(adminRequest(), old.getId(), "I999", "V999");
        assertEquals(BookingStatus.REJECTED, result.getStatus());
        assertTrue(result.getRejectionReason().contains("Awaiting payment"));
    }

    // A cancelled legacy head leaves the FIFO line and cannot prevent the next request joining it.
    @Test
    void fifoProcessingRejectsCancelledHeadAndContinues() throws Exception {
        clearTemporaryBookings();
        Booking old = requestLesson();
        changePaymentState("CANCELLED");
        MockHttpServletRequest admin = adminRequest();
        Booking rejected = bookingService.processNext(admin, "I999", "V999");
        assertEquals(BookingStatus.REJECTED, rejected.getStatus());
        assertTrue(rejected.getRejectionReason().contains("Cancelled"));
        assertTrue(bookingService.getPendingQueue(admin).isEmpty());
        changePaymentState("PAID");
        Booking next = requestLesson();
        assertEquals(next.getId(), bookingService.getPendingQueue(admin).get(0).getId());
    }

    // Reassignment cannot move resources for a lesson whose saved receipt is no longer Paid.
    @Test
    void reassignRequiresPaidReceipt() throws Exception {
        Booking old = requestLesson();
        old.confirm(instructor.getId(), "V001");
        bookings.update(old);
        changePaymentState("AWAITING_PAYMENT");
        assertThrows(ValidationException.class, () -> bookingService.reassign(adminRequest(), old.getId(), "I999", "V999"));
        assertEquals("V001", bookings.findByIdStrict(old.getId()).orElseThrow().getVehicleId());
    }

    // Instructor and completion routes must recheck the receipt instead of trusting assigned IDs.
    @Test
    void instructorAndCompletionWritesRequirePaidReceipt() throws Exception {
        Booking old = requestLesson();
        old.assignForInstructor(instructor.getId(), "V001");
        bookings.update(old);
        changePaymentState("AWAITING_PAYMENT");
        MockHttpServletRequest teacher = new MockHttpServletRequest();
        teacher.setSession(instructorSession);
        assertThrows(ValidationException.class, () -> bookingService.acceptAssignedLesson(teacher, old.getId()));
        old.confirm(instructor.getId(), "V001");
        bookings.update(old);
        assertThrows(ValidationException.class, () -> bookingService.complete(adminRequest(), old.getId()));
        assertThrows(ValidationException.class, () -> bookingService.completeAssignedLesson(teacher, old.getId()));
        assertThrows(ValidationException.class, () -> bookingService.markNoShowAssignedLesson(teacher, old.getId()));
        assertEquals(BookingStatus.CONFIRMED, bookings.findByIdStrict(old.getId()).orElseThrow().getStatus());
    }

    // A paid receipt with no tickets left still cannot create another request.
    @Test
    void exhaustedEnrollmentCannotRequestLessons() {
        student.setLessonsRemaining(0);
        users.update(student);
        assertThrows(ValidationException.class, () -> bookingService.requestLesson(studentRequest(), course.getId(), lessonTime));
    }

    // A Paid receipt whose validity has ended cannot reserve a new lesson.
    @Test
    void expiredPaidEnrollmentCannotRequestLessons() {
        Enrollment expired = new Enrollment(student.getActiveEnrollmentId(), student.getId(), course.getId(),
                LocalDateTime.now(clock).minusDays(10), course.getPrice(), "PAID");
        expired.setValidUntil(LocalDate.now(clock).minusDays(1));
        receipts.update(expired);
        assertTrue(assertThrows(ValidationException.class,
                () -> bookingService.requestLesson(studentRequest(), course.getId(), lessonTime)).getMessage().contains("expired"));
    }

    // Changes only this test receipt to reproduce old or externally changed payment data.
    private void changePaymentState(String status) {
        Enrollment receipt = receipts.findByIdStrict(student.getActiveEnrollmentId()).orElseThrow();
        receipt.setStatus(status);
        receipts.update(receipt);
    }

    // Supplies the real signed-in student's session to direct service checks.
    private MockHttpServletRequest studentRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(studentSession);
        return request;
    }

    // Creates a real temporary administrator session for assignment checks.
    private MockHttpServletRequest adminRequest() {
        String id = users.nextId("A");
        String email = "admin-" + id + "@example.test";
        users.save(new Admin(id, "Test Cashier", email, student.getPasswordHash()));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(login(email));
        return request;
    }

    // Gives FIFO checks a private empty line inside this class's temporary directory.
    private void clearTemporaryBookings() {
        for (Booking booking : bookings.readAllStrict()) bookings.delete(booking.getId());
    }

    // Creates a student request using the actual slot and conflict services.
    private Booking requestLesson() throws Exception {
        mvc.perform(post("/bookings").session(studentSession).param("_csrf", token("/bookings", studentSession))
                .param("packageId", course.getId()).param("instructorId", instructor.getId())
                .param("startTime", lessonTime.toString())).andExpect(redirectedUrl("/bookings"));
        return ownPendingBooking();
    }

    // Finds the current test student's pending request among the temporary records.
    private Booking ownPendingBooking() {
        for (Booking booking : bookings.readAllStrict()) {
            if (booking.getStudentId().equals(student.getId()) && booking.getStatus() == BookingStatus.PENDING) return booking;
        }
        throw new AssertionError("Expected a pending request for this student.");
    }

    // Finds the current test instructor's saved working block.
    private Availability ownBlock() {
        for (Availability block : hours.readAllStrict()) {
            if (block.getInstructorId().equals(instructor.getId())) return block;
        }
        throw new AssertionError("Expected working hours for this instructor.");
    }

    // Starts a real role session through the existing password checks.
    private MockHttpSession login(String email) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertTrue(sessions.login(request, email, PASSWORD));
        return (MockHttpSession) request.getSession(false);
    }

    // Reads the same token that the server supplies to the browser form.
    private String token(String page, MockHttpSession session) throws Exception {
        MvcResult result = mvc.perform(get(page).session(session)).andExpect(status().isOk()).andReturn();
        return (String) result.getModelAndView().getModel().get("csrfToken");
    }
}
