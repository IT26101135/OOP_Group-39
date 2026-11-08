/**
============================================================
MEMBER   : IT26101137 - [Name]
MODULE   : Reviews and Reports
FILE     : ReviewWebIntegrationTest.java
WHAT IT DOES (simple words): Rehearses feedback forms, moderation and report downloads with spare files.
REAL-LIFE EXAMPLE: Testing the feedback desk before students arrive.
OOP USED : Service interfaces, ownership checks and custom exceptions
CRUD     : CREATE, READ, UPDATE, DELETE for reviews
============================================================
*/
package com.drivingschool.review;

import com.drivingschool.auth.model.*;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.service.*;
import com.drivingschool.auth.util.PasswordHasher;
import com.drivingschool.booking.model.*;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.review.model.*;
import com.drivingschool.review.repository.ReviewRepository;
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
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ReviewWebIntegrationTest {
    @TempDir static Path directory;
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired AuthService authentication;
    @Autowired AuthSessionService sessions;
    @Autowired PasswordHasher passwords;
    @Autowired BookingRepository bookings;
    @Autowired ReviewRepository reviews;
    @Autowired Clock clock;
    private Student student;
    private MockHttpSession studentSession;
    private MockHttpSession adminSession;
    private static final String PASSWORD = "TestingPass123!";

    // Keeps all test writes away from the project data and disables application seeding.
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.data-directory", () -> directory.toString());
        registry.add("app.admin-seed.enabled", () -> false);
        registry.add("app.demo-seed.enabled", () -> false);
    }

    // Gives every test a new active student and administrator using normal login sessions.
    @BeforeEach
    void setUp() {
        String number = String.valueOf(users.readAllStrict().size());
        student = authentication.registerStudent("Learner " + number, "learner" + number + "@example.test", "0771234567", PASSWORD);
        student.setStatus(AccountStatus.ACTIVE);
        users.update(student);
        Admin admin = new Admin(users.nextId("A"), "Manager " + number, "admin" + number + "@example.test", passwords.hash(PASSWORD));
        users.save(admin);
        studentSession = login(student.getEmail());
        adminSession = login(admin.getEmail());
    }

    // A student can create, read, update and delete only their own completed-lesson review.
    @Test
    void supportsReviewCrudThroughRealRoutes() throws Exception {
        Review review = submitReview(completedLesson(student.getId(), BookingStatus.COMPLETED));
        mvc.perform(get("/reviews").session(studentSession)).andExpect(status().isOk())
                .andExpect(model().attributeExists("reviews", "completedBookings"));
        mvc.perform(post("/reviews/update").session(studentSession).param("_csrf", token("/reviews", studentSession))
                .param("reviewId", review.getId()).param("rating", "3").param("comment", "Now updated after practice"))
                .andExpect(redirectedUrl("/reviews"));
        assertEquals(3, reviews.findByIdStrict(review.getId()).orElseThrow().getRating());
        assertEquals(ReviewStatus.PENDING, reviews.findByIdStrict(review.getId()).orElseThrow().getStatus());
        mvc.perform(post("/reviews/delete").session(studentSession).param("_csrf", token("/reviews", studentSession))
                .param("reviewId", review.getId())).andExpect(redirectedUrl("/reviews"));
        assertTrue(reviews.findByIdStrict(review.getId()).isEmpty());
    }

    // A hidden or pending review still reserves its lesson, preventing a second review.
    @Test
    void duplicateCompletedLessonReviewIsRejected() throws Exception {
        Booking lesson = completedLesson(student.getId(), BookingStatus.COMPLETED);
        submitReview(lesson);
        mvc.perform(post("/reviews").session(studentSession).param("_csrf", token("/reviews", studentSession))
                .param("bookingId", lesson.getId()).param("rating", "5").param("comment", "Another review for same lesson"))
                .andExpect(status().isConflict()).andExpect(view().name("common/error"));
    }

    // Lessons still awaiting attendance cannot receive feedback.
    @Test
    void incompleteLessonIsRejectedWithFriendlyValidation() throws Exception {
        Booking lesson = completedLesson(student.getId(), BookingStatus.CONFIRMED);
        mvc.perform(post("/reviews").session(studentSession).param("_csrf", token("/reviews", studentSession))
                .param("bookingId", lesson.getId()).param("rating", "5").param("comment", "Tried before lesson completion"))
                .andExpect(status().isBadRequest()).andExpect(view().name("common/error"));
    }

    // A valid completed lesson is still private to its own student.
    @Test
    void anotherStudentsLessonIsForbidden() throws Exception {
        String anotherStudent = "S999";
        Booking lesson = completedLesson(anotherStudent, BookingStatus.COMPLETED);
        mvc.perform(post("/reviews").session(studentSession).param("_csrf", token("/reviews", studentSession))
                .param("bookingId", lesson.getId()).param("rating", "5").param("comment", "Tried reviewing another learner"))
                .andExpect(status().isForbidden());
    }

    // Invalid stars use the existing custom validation exception and leave no new record.
    @Test
    void invalidRatingIsRejectedWithoutSaving() throws Exception {
        Booking lesson = completedLesson(student.getId(), BookingStatus.COMPLETED);
        int before = reviews.readAllStrict().size();
        mvc.perform(post("/reviews").session(studentSession).param("_csrf", token("/reviews", studentSession))
                .param("bookingId", lesson.getId()).param("rating", "0").param("comment", "A comment with invalid stars"))
                .andExpect(status().isBadRequest()).andExpect(view().name("common/error"));
        assertEquals(before, reviews.readAllStrict().size());
    }

    // Approval makes feedback public; hiding removes it from the public catalog again.
    @Test
    void moderationControlsPublicVisibility() throws Exception {
        Review review = submitReview(completedLesson(student.getId(), BookingStatus.COMPLETED));
        String csrf = token("/admin/reviews", adminSession);
        mvc.perform(post("/admin/reviews/approve").session(adminSession).param("_csrf", csrf).param("reviewId", review.getId()))
                .andExpect(redirectedUrl("/admin/reviews"));
        assertTrue(publicReviews().contains(review.getId()));
        mvc.perform(post("/admin/reviews/hide").session(adminSession).param("_csrf", csrf).param("reviewId", review.getId()))
                .andExpect(redirectedUrl("/admin/reviews"));
        assertFalse(publicReviews().contains(review.getId()));
    }

    // Both the report page and download enforce admin access before exposing any totals.
    @Test
    void reportsAndCsvAreAdminOnly() throws Exception {
        mvc.perform(get("/admin/reports")).andExpect(redirectedUrl("/login?required"));
        mvc.perform(get("/admin/reports/export.csv").session(studentSession)).andExpect(status().isForbidden());
        mvc.perform(get("/admin/reports").session(adminSession)).andExpect(status().isOk())
                .andExpect(model().attributeExists("reports"));
        mvc.perform(get("/admin/reports/export.csv").session(adminSession)).andExpect(status().isOk())
                .andExpect(content().contentType("text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=drivespark-reports.csv"));
    }

    // Missing CSRF protection must block both student submissions and administrator moderation.
    @Test
    void writeRoutesRequireCsrf() throws Exception {
        mvc.perform(post("/reviews").session(studentSession).param("bookingId", "B001").param("rating", "5")
                .param("comment", "Missing token example feedback")).andExpect(status().isForbidden());
        mvc.perform(post("/admin/reviews/approve").session(adminSession).param("reviewId", "R001"))
                .andExpect(status().isForbidden());
    }

    // Creates an isolated attendance fixture; this never drives live lesson actions.
    private Booking completedLesson(String studentId, BookingStatus status) {
        LocalDateTime start = LocalDateTime.now(clock).minusDays(1);
        Booking booking = new Booking(bookings.nextId("B"), studentId, "E001", start, start.plusHours(1),
                "I001", "V001", status, start.minusDays(1));
        bookings.save(booking);
        return booking;
    }

    // Submits the real review form and finds the saved feedback for this lesson.
    private Review submitReview(Booking lesson) throws Exception {
        mvc.perform(post("/reviews").session(studentSession).param("_csrf", token("/reviews", studentSession))
                .param("bookingId", lesson.getId()).param("rating", "5").param("comment", "Clear and helpful driving lesson"))
                .andExpect(redirectedUrl("/reviews"));
        for (Review review : reviews.readAllStrict()) {
            if (lesson.getId().equals(review.getBookingId())) {
                return review;
            }
        }
        throw new AssertionError("Submitted review was not saved");
    }

    // Gets the normal session token supplied to forms on the selected page.
    private String token(String path, MockHttpSession session) throws Exception {
        MvcResult result = mvc.perform(get(path).session(session)).andExpect(status().isOk()).andReturn();
        return (String) result.getModelAndView().getModel().get("csrfToken");
    }

    // Uses the normal password check and session rotation for each test account.
    private MockHttpSession login(String email) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertTrue(sessions.login(request, email, PASSWORD));
        return (MockHttpSession) request.getSession(false);
    }

    // Reads only IDs from the public model so pending comments stay outside the public response.
    private List<String> publicReviews() throws Exception {
        MvcResult result = mvc.perform(get("/public/reviews")).andExpect(status().isOk()).andReturn();
        @SuppressWarnings("unchecked")
        List<Review> visible = (List<Review>) result.getModelAndView().getModel().get("reviews");
        java.util.ArrayList<String> ids = new java.util.ArrayList<>();
        for (Review review : visible) {
            ids.add(review.getId());
        }
        return ids;
    }
}
