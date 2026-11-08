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
    @Autowired UserRepository users;
    @Autowired AvailabilityRepository hours;
    @Autowired BookingRepository bookings;
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
        enrollments.enroll(request, course.getId());
        lessonTime = LocalDate.now(clock).plusDays(3).atTime(10, 0);
        hours.save(new Availability(hours.nextId("H"), instructor.getId(), lessonTime.getDayOfWeek(),
                LocalTime.of(9, 0), LocalTime.of(12, 0)));
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
