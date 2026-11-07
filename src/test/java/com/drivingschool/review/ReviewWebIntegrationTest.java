package com.drivingschool.review;

import com.drivingschool.auth.service.AdminSeedService;
import com.drivingschool.auth.service.AuthService;
import com.drivingschool.review.model.Review;
import com.drivingschool.review.model.ReviewStatus;
import com.drivingschool.review.repository.ReviewRepository;
import com.drivingschool.student.model.Student;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Path;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ReviewWebIntegrationTest {

    private static final String REVIEWS = "/reviews";
    private static final String ADMIN_REVIEWS = "/admin/reviews";
    private static final String PASSWORD = "ReviewTestPass123!";
    private static final String ADMIN_EMAIL = "review-admin@example.com";

    @TempDir
    static Path dataDirectory;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("app.data-directory", () -> dataDirectory.toString());
        registry.add("app.admin-seed.enabled", () -> false);
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    AuthService authService;

    @Autowired
    AdminSeedService adminSeedService;

    @Autowired
    ReviewRepository reviewRepository;

    private Student createStudent() {
        return authService.registerStudent(
                "Review Learner",
                email(),
                "0771234567",
                PASSWORD
        );
    }

    private String email() {
        return "review-" + UUID.randomUUID() + "@example.com";
    }

    private MockHttpSession login(String email) throws Exception {

        MvcResult page = mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult result = mvc.perform(post("/login")
                        .session(sessionOf(page))
                        .param("_csrf", tokenOf(page))
                        .param("email", email)
                        .param("password", PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        return sessionOf(result);
    }

    private MockHttpSession sessionOf(MvcResult result) {
        return (MockHttpSession) result.getRequest()
                .getSession(false);
    }

    private String tokenOf(MvcResult result) {
        return (String) result.getModelAndView()
                .getModel().get("csrfToken");
    }

    @Test
    void anonymousUsersCannotAccessReviewPagesOrSubmitReviews() throws Exception {

        mvc.perform(get(REVIEWS))
                .andExpect(status().isUnauthorized());

        mvc.perform(get(REVIEWS + "/create"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get(ADMIN_REVIEWS))
                .andExpect(status().isUnauthorized());

        mvc.perform(post(REVIEWS + "/create"))
                .andExpect(status().isForbidden());

        mvc.perform(post(ADMIN_REVIEWS + "/R001/approve"))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentCanSeeApprovedReviewsAndOwnReviewsOnly() throws Exception {

        Student student = createStudent();
        Student otherStudent = createStudent();

        LocalDateTime now = pastTime();

        Review approvedReview = new Review(
                reviewRepository.nextId("R"),
                otherStudent.getId(),
                5,
                "Excellent driving school",
                ReviewStatus.APPROVED,
                now,
                now
        );

        reviewRepository.save(approvedReview);

        Review ownPendingReview = new Review(
                reviewRepository.nextId("R"),
                student.getId(),
                4,
                "Good driving school",
                ReviewStatus.PENDING,
                now,
                now
        );

        reviewRepository.save(ownPendingReview);

        Review hiddenOtherReview = new Review(
                reviewRepository.nextId("R"),
                otherStudent.getId(),
                2,
                "Not a good experience",
                ReviewStatus.HIDDEN,
                now,
                now
        );

        reviewRepository.save(hiddenOtherReview);

        MockHttpSession session = login(student.getEmail());

        MvcResult result = mvc.perform(get(REVIEWS).session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("review/list"))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(model().attribute("currentStudentId", student.getId()))
                .andReturn();

        @SuppressWarnings("unchecked")
        List<Review> reviews = (List<Review>) result.getModelAndView()
                .getModel().get("reviews");

        assertTrue(reviews.stream().anyMatch(
                review -> review.getId().equals(approvedReview.getId())));
        assertTrue(reviews.stream().anyMatch(
                review -> review.getId().equals(ownPendingReview.getId())));
        assertFalse(reviews.stream().anyMatch(
                review -> review.getId().equals(hiddenOtherReview.getId())));
    }
    @Test
    void studentCannotAccessAdminReviewPagesOrModerateReviews() throws Exception {

        Student student = createStudent();
        MockHttpSession session = login(student.getEmail());

        MvcResult page = mvc.perform(get(REVIEWS).session(session))
                .andExpect(status().isOk())
                .andReturn();

        String csrfToken = tokenOf(page);

        mvc.perform(get(ADMIN_REVIEWS).session(session))
                .andExpect(status().isForbidden());

        mvc.perform(post(ADMIN_REVIEWS + "/R001/approve")
                        .session(session)
                        .param("_csrf", csrfToken))
                .andExpect(status().isForbidden());

        mvc.perform(post(ADMIN_REVIEWS + "/R001/hide")
                        .session(session)
                        .param("_csrf", csrfToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanViewApproveAndHideReviews() throws Exception {

        Student student1 = createStudent();
        Student student2 = createStudent();

        LocalDateTime now = pastTime();

        Review pendingReview = new Review(
                reviewRepository.nextId("R"),
                student1.getId(),
                5,
                "Excellent driving school",
                ReviewStatus.PENDING,
                now,
                now
        );

        reviewRepository.save(pendingReview);

        Review anotherReview = new Review(
                reviewRepository.nextId("R"),
                student2.getId(),
                3,
                "Good driving school",
                ReviewStatus.PENDING,
                now,
                now
        );

        reviewRepository.save(anotherReview);

        adminSeedService.seedIfNeeded(
                "Review Test Admin",
                ADMIN_EMAIL,
                PASSWORD
        );

        MockHttpSession session = login(ADMIN_EMAIL);

        MvcResult page = mvc.perform(get(ADMIN_REVIEWS).session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("review/admin-list"))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andReturn();

        @SuppressWarnings("unchecked")
        List<Review> reviews = (List<Review>) page.getModelAndView()
                .getModel().get("reviews");

        assertTrue(reviews.stream().anyMatch(
                review -> review.getId().equals(pendingReview.getId())));

        assertTrue(reviews.stream().anyMatch(
                review -> review.getId().equals(anotherReview.getId())));

        String csrfToken = tokenOf(page);

        mvc.perform(post(ADMIN_REVIEWS + "/" + pendingReview.getId() + "/approve")
                        .session(session)
                        .param("_csrf", csrfToken))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ADMIN_REVIEWS))
                .andExpect(flash().attributeExists("moderationSuccess"));

        assertEquals(
                ReviewStatus.APPROVED,
                reviewRepository.findByIdStrict(pendingReview.getId())
                        .orElseThrow().getStatus()
        );

        mvc.perform(post(ADMIN_REVIEWS + "/" + anotherReview.getId() + "/hide")
                        .session(session)
                        .param("_csrf", csrfToken))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ADMIN_REVIEWS))
                .andExpect(flash().attributeExists("moderationSuccess"));

        assertEquals(
                ReviewStatus.HIDDEN,
                reviewRepository.findByIdStrict(anotherReview.getId())
                        .orElseThrow().getStatus()
        );
    }

    @Test
    void studentCanCreateReviewSuccessfully() throws Exception {

        Student student = createStudent();
        MockHttpSession session = login(student.getEmail());

        MvcResult page = mvc.perform(get(REVIEWS + "/create")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("review/create"))
                .andReturn();

        String csrfToken = tokenOf(page);

        mvc.perform(post(REVIEWS + "/create")
                        .session(session)
                        .param("_csrf", csrfToken)
                        .param("rating", "5")
                        .param("comment", "Excellent driving school experience")
                        .param("studentId", "S999")
                        .param("status", "APPROVED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(REVIEWS))
                .andExpect(flash().attribute("reviewSubmitted", true));

        Review savedReview = reviewRepository.readAllStrict()
                .stream()
                .filter(review -> review.getStudentId().equals(student.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(5, savedReview.getRating());
        assertEquals(
                "Excellent driving school experience",
                savedReview.getComment()
        );
        assertEquals(ReviewStatus.PENDING, savedReview.getStatus());
        assertNotEquals("S999", savedReview.getStudentId());
    }

    @Test
    void studentCanEditOwnReview() throws Exception {

        Student student = createStudent();
        LocalDateTime now = pastTime();

        Review review = new Review(
                reviewRepository.nextId("R"),
                student.getId(),
                3,
                "Good driving school",
                ReviewStatus.APPROVED,
                now,
                now
        );

        reviewRepository.save(review);

        MockHttpSession session = login(student.getEmail());

        MvcResult page = mvc.perform(get(
                        REVIEWS + "/" + review.getId() + "/edit")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("review/edit"))
                .andReturn();

        mvc.perform(post(REVIEWS + "/" + review.getId() + "/edit")
                        .session(session)
                        .param("_csrf", tokenOf(page))
                        .param("rating", "5")
                        .param("comment", "Excellent driving school experience")
                        .param("studentId", "S999")
                        .param("status", "APPROVED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(REVIEWS))
                .andExpect(flash().attribute("reviewUpdated", true));

        Review updated = reviewRepository.findByIdStrict(review.getId())
                .orElseThrow();

        assertEquals(5, updated.getRating());
        assertEquals("Excellent driving school experience", updated.getComment());
        assertEquals(ReviewStatus.PENDING, updated.getStatus());
        assertEquals(student.getId(), updated.getStudentId());
    }

    @Test
    void studentCannotEditAnotherStudentsReview() throws Exception {

        Student owner = createStudent();
        Student otherStudent = createStudent();

        LocalDateTime now = pastTime();

        Review review = new Review(
                reviewRepository.nextId("R"),
                owner.getId(),
                4,
                "Good driving school",
                ReviewStatus.APPROVED,
                now,
                now
        );

        reviewRepository.save(review);

        MockHttpSession session = login(otherStudent.getEmail());

        MvcResult page = mvc.perform(get(REVIEWS).session(session))
                .andExpect(status().isOk())
                .andReturn();

        String csrfToken = tokenOf(page);

        mvc.perform(get(REVIEWS + "/" + review.getId() + "/edit")
                        .session(session))
                .andExpect(status().isNotFound());

        mvc.perform(post(REVIEWS + "/" + review.getId() + "/edit")
                        .session(session)
                        .param("_csrf", csrfToken)
                        .param("rating", "5")
                        .param("comment", "Trying to change another review"))
                .andExpect(status().isNotFound());

        Review unchanged = reviewRepository.findByIdStrict(review.getId())
                .orElseThrow();

        assertEquals(4, unchanged.getRating());
        assertEquals(owner.getId(), unchanged.getStudentId());
    }

    @Test
    void studentCanDeleteOwnReview() throws Exception {

        Student student = createStudent();
        LocalDateTime now = pastTime();

        Review review = new Review(
                reviewRepository.nextId("R"),
                student.getId(),
                4,
                "Good driving school",
                ReviewStatus.PENDING,
                now,
                now
        );

        reviewRepository.save(review);

        MockHttpSession session = login(student.getEmail());

        MvcResult page = mvc.perform(get(REVIEWS).session(session))
                .andExpect(status().isOk())
                .andReturn();

        mvc.perform(post(REVIEWS + "/" + review.getId() + "/delete")
                        .session(session)
                        .param("_csrf", tokenOf(page)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(REVIEWS))
                .andExpect(flash().attribute("reviewDeleted", true));

        assertTrue(reviewRepository.findByIdStrict(review.getId()).isEmpty());
    }

    @Test
    void duplicateReviewAndInvalidCsrfAreRejected() throws Exception {

        Student student = createStudent();
        MockHttpSession session = login(student.getEmail());

        MvcResult page = mvc.perform(get(REVIEWS + "/create")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();

        String csrfToken = tokenOf(page);

        mvc.perform(post(REVIEWS + "/create")
                        .session(session)
                        .param("rating", "5")
                        .param("comment", "Excellent driving school"))
                .andExpect(status().isForbidden());

        mvc.perform(post(REVIEWS + "/create")
                        .session(session)
                        .param("_csrf", csrfToken)
                        .param("rating", "5")
                        .param("comment", "Excellent driving school"))
                .andExpect(status().is3xxRedirection());

        MvcResult duplicate = mvc.perform(post(REVIEWS + "/create")
                        .session(session)
                        .param("_csrf", csrfToken)
                        .param("rating", "4")
                        .param("comment", "Another driving school review"))
                .andExpect(status().isConflict())
                .andExpect(view().name("review/create"))
                .andExpect(model().attributeExists("formError", "csrfToken"))
                .andReturn();

        assertNotNull(tokenOf(duplicate));
        assertEquals(1, reviewRepository.readAllStrict().stream()
                .filter(review -> review.getStudentId().equals(student.getId()))
                .count());
    }

    @Test
    void adminCannotEditOrDeleteStudentReviews() throws Exception {

        Student student = createStudent();
        LocalDateTime now = pastTime();

        Review review = new Review(
                reviewRepository.nextId("R"),
                student.getId(),
                4,
                "Good driving school",
                ReviewStatus.APPROVED,
                now,
                now
        );

        reviewRepository.save(review);

        adminSeedService.seedIfNeeded(
                "Review Test Admin",
                ADMIN_EMAIL,
                PASSWORD
        );

        MockHttpSession session = login(ADMIN_EMAIL);

        MvcResult page = mvc.perform(get(ADMIN_REVIEWS).session(session))
                .andExpect(status().isOk())
                .andReturn();

        String csrfToken = tokenOf(page);

        mvc.perform(get(REVIEWS + "/" + review.getId() + "/edit")
                        .session(session))
                .andExpect(status().isForbidden());

        mvc.perform(post(REVIEWS + "/" + review.getId() + "/edit")
                        .session(session)
                        .param("_csrf", csrfToken)
                        .param("rating", "1")
                        .param("comment", "Changed by administrator"))
                .andExpect(status().isForbidden());

        mvc.perform(post(REVIEWS + "/" + review.getId() + "/delete")
                        .session(session)
                        .param("_csrf", csrfToken))
                .andExpect(status().isForbidden());

        Review unchanged = reviewRepository.findByIdStrict(review.getId())
                .orElseThrow();

        assertEquals(4, unchanged.getRating());
        assertEquals(ReviewStatus.APPROVED, unchanged.getStatus());
    }

    @Test
    void createRejectsInvalidRating() throws Exception {

        Student student = createStudent();
        MockHttpSession session = login(student.getEmail());

        MvcResult page = mvc.perform(get(REVIEWS + "/create")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();

        String csrfToken = tokenOf(page);

        for (String rating : List.of("0", "6")) {

            mvc.perform(post(REVIEWS + "/create")
                            .session(session)
                            .param("_csrf", csrfToken)
                            .param("rating", rating)
                            .param("comment", "Good driving school experience"))
                    .andExpect(status().isBadRequest())
                    .andExpect(view().name("review/create"))
                    .andExpect(model().attributeExists("formError", "csrfToken"));
        }

        assertTrue(reviewRepository.readAllStrict().stream()
                .noneMatch(review -> review.getStudentId().equals(student.getId())));
    }

    @Test
    void createRejectsNonNumericRating() throws Exception {

        Student student = createStudent();
        MockHttpSession session = login(student.getEmail());

        MvcResult page = mvc.perform(get(REVIEWS + "/create")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();

        mvc.perform(post(REVIEWS + "/create")
                        .session(session)
                        .param("_csrf", tokenOf(page))
                        .param("rating", "abc")
                        .param("comment", "Good driving school experience"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("review/create"))
                .andExpect(model().attributeExists("formError"));
    }

    @Test
    void createRejectsShortAndTooLongComment() throws Exception {

        Student student = createStudent();
        MockHttpSession session = login(student.getEmail());

        MvcResult page = mvc.perform(get(REVIEWS + "/create")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();

        String csrfToken = tokenOf(page);

        for (String comment : List.of("short", "x".repeat(1001))) {

            mvc.perform(post(REVIEWS + "/create")
                            .session(session)
                            .param("_csrf", csrfToken)
                            .param("rating", "5")
                            .param("comment", comment))
                    .andExpect(status().isBadRequest())
                    .andExpect(view().name("review/create"))
                    .andExpect(model().attributeExists("formError", "csrfToken"));
        }

        assertTrue(reviewRepository.readAllStrict().stream()
                .noneMatch(review -> review.getStudentId().equals(student.getId())));
    }

    @Test
    void editRejectsInvalidInput() throws Exception {

        Student student = createStudent();
        LocalDateTime now = pastTime();

        Review review = new Review(
                reviewRepository.nextId("R"),
                student.getId(),
                4,
                "Good driving school",
                ReviewStatus.APPROVED,
                now,
                now
        );

        reviewRepository.save(review);

        MockHttpSession session = login(student.getEmail());

        MvcResult page = mvc.perform(get(
                        REVIEWS + "/" + review.getId() + "/edit")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();

        mvc.perform(post(REVIEWS + "/" + review.getId() + "/edit")
                        .session(session)
                        .param("_csrf", tokenOf(page))
                        .param("rating", "0")
                        .param("comment", "Good driving school experience"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("review/edit"))
                .andExpect(model().attribute("reviewId", review.getId()));

        Review unchanged = reviewRepository.findByIdStrict(review.getId())
                .orElseThrow();

        assertEquals(4, unchanged.getRating());
        assertEquals("Good driving school", unchanged.getComment());
        assertEquals(ReviewStatus.APPROVED, unchanged.getStatus());
    }

    @Test
    void unchangedEditKeepsStatus() throws Exception {

        Student student = createStudent();
        LocalDateTime now = pastTime();

        Review review = new Review(
                reviewRepository.nextId("R"),
                student.getId(),
                5,
                "Excellent driving school",
                ReviewStatus.APPROVED,
                now,
                now
        );

        reviewRepository.save(review);

        MockHttpSession session = login(student.getEmail());

        MvcResult page = mvc.perform(get(
                        REVIEWS + "/" + review.getId() + "/edit")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();

        mvc.perform(post(REVIEWS + "/" + review.getId() + "/edit")
                        .session(session)
                        .param("_csrf", tokenOf(page))
                        .param("rating", "5")
                        .param("comment", "Excellent driving school"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(REVIEWS))
                .andExpect(flash().attribute("reviewUpdated", true));

        Review unchanged = reviewRepository.findByIdStrict(review.getId())
                .orElseThrow();

        assertEquals(ReviewStatus.APPROVED, unchanged.getStatus());
    }

    @Test
    void studentCanEditOwnHiddenReview() throws Exception {

        Student student = createStudent();
        LocalDateTime now = pastTime();

        Review review = new Review(
                reviewRepository.nextId("R"),
                student.getId(),
                3,
                "Good driving school",
                ReviewStatus.HIDDEN,
                now,
                now
        );

        reviewRepository.save(review);

        MockHttpSession session = login(student.getEmail());

        MvcResult page = mvc.perform(get(
                        REVIEWS + "/" + review.getId() + "/edit")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("review/edit"))
                .andReturn();

        mvc.perform(post(REVIEWS + "/" + review.getId() + "/edit")
                        .session(session)
                        .param("_csrf", tokenOf(page))
                        .param("rating", "4")
                        .param("comment", "Excellent driving school experience"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(REVIEWS));

        Review updated = reviewRepository.findByIdStrict(review.getId())
                .orElseThrow();

        assertEquals(ReviewStatus.PENDING, updated.getStatus());
        assertEquals(4, updated.getRating());
    }

    @Test
    void studentCannotDeleteAnotherStudentsReview() throws Exception {

        Student owner = createStudent();
        Student otherStudent = createStudent();

        LocalDateTime now = pastTime();

        Review approvedReview = new Review(
                reviewRepository.nextId("R"),
                owner.getId(),
                4,
                "Good driving school",
                ReviewStatus.APPROVED,
                now,
                now
        );

        reviewRepository.save(approvedReview);

        Review hiddenReview = new Review(
                reviewRepository.nextId("R"),
                owner.getId(),
                3,
                "Average driving school",
                ReviewStatus.HIDDEN,
                now,
                now
        );

        reviewRepository.save(hiddenReview);

        MockHttpSession session = login(otherStudent.getEmail());

        MvcResult page = mvc.perform(get(REVIEWS).session(session))
                .andExpect(status().isOk())
                .andReturn();

        String csrfToken = tokenOf(page);

        for (Review review : List.of(approvedReview, hiddenReview)) {

            mvc.perform(post(REVIEWS + "/" + review.getId() + "/delete")
                            .session(session)
                            .param("_csrf", csrfToken))
                    .andExpect(status().isNotFound());

            assertTrue(reviewRepository.findByIdStrict(review.getId()).isPresent());
        }
    }

    @Test
    void createFormRedirectsWhenAlreadyReviewed() throws Exception {

        Student student = createStudent();
        LocalDateTime now = pastTime();

        Review review = new Review(
                reviewRepository.nextId("R"),
                student.getId(),
                5,
                "Excellent driving school",
                ReviewStatus.PENDING,
                now,
                now
        );

        reviewRepository.save(review);

        MockHttpSession session = login(student.getEmail());

        mvc.perform(get(REVIEWS + "/create").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(REVIEWS));
    }

    @Test
    void adminSeesReviewsOfEveryStatus() throws Exception {

        Student student = createStudent();
        LocalDateTime now = pastTime();

        Review pending = new Review(
                reviewRepository.nextId("R"),
                student.getId(),
                5,
                "Pending review comment",
                ReviewStatus.PENDING,
                now,
                now
        );

        Review approved = new Review(
                reviewRepository.nextId("R"),
                student.getId(),
                4,
                "Approved review comment",
                ReviewStatus.APPROVED,
                now,
                now
        );

        Review hidden = new Review(
                reviewRepository.nextId("R"),
                student.getId(),
                3,
                "Hidden review comment",
                ReviewStatus.HIDDEN,
                now,
                now
        );

        reviewRepository.save(pending);
        reviewRepository.save(approved);
        reviewRepository.save(hidden);

        adminSeedService.seedIfNeeded(
                "Review Test Admin",
                ADMIN_EMAIL,
                PASSWORD
        );

        MockHttpSession session = login(ADMIN_EMAIL);

        MvcResult page = mvc.perform(get(ADMIN_REVIEWS).session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("review/admin-list"))
                .andReturn();

        @SuppressWarnings("unchecked")
        List<Review> reviews = (List<Review>) page.getModelAndView()
                .getModel().get("reviews");

        assertTrue(reviews.stream().anyMatch(
                review -> review.getId().equals(pending.getId())));

        assertTrue(reviews.stream().anyMatch(
                review -> review.getId().equals(approved.getId())));

        assertTrue(reviews.stream().anyMatch(
                review -> review.getId().equals(hidden.getId())));
    }

    @Test
    void adminModerationNeedsCsrfAndValidIds() throws Exception {

        adminSeedService.seedIfNeeded(
                "Review Test Admin",
                ADMIN_EMAIL,
                PASSWORD
        );

        MockHttpSession session = login(ADMIN_EMAIL);

        MvcResult page = mvc.perform(get(ADMIN_REVIEWS).session(session))
                .andExpect(status().isOk())
                .andReturn();

        String csrfToken = tokenOf(page);

        // Reject moderation without CSRF token
        mvc.perform(post(ADMIN_REVIEWS + "/R999/approve")
                        .session(session))
                .andExpect(status().isForbidden());

        // Reject a non-existing review ID
        mvc.perform(post(ADMIN_REVIEWS + "/R999/approve")
                        .session(session)
                        .param("_csrf", csrfToken))
                .andExpect(status().isNotFound());

        // Reject an invalid review ID
        mvc.perform(post(ADMIN_REVIEWS + "/abc/approve")
                        .session(session)
                        .param("_csrf", csrfToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminHideThenApproveRoundTrip() throws Exception {

        Student student = createStudent();
        LocalDateTime now = pastTime();

        Review review = new Review(
                reviewRepository.nextId("R"),
                student.getId(),
                5,
                "Excellent driving school",
                ReviewStatus.PENDING,
                now,
                now
        );

        reviewRepository.save(review);

        adminSeedService.seedIfNeeded(
                "Review Test Admin",
                ADMIN_EMAIL,
                PASSWORD
        );

        MockHttpSession session = login(ADMIN_EMAIL);

        MvcResult page = mvc.perform(get(ADMIN_REVIEWS).session(session))
                .andExpect(status().isOk())
                .andReturn();

        String csrfToken = tokenOf(page);

        mvc.perform(post(ADMIN_REVIEWS + "/" + review.getId() + "/hide")
                        .session(session)
                        .param("_csrf", csrfToken))
                .andExpect(status().is3xxRedirection());

        assertEquals(
                ReviewStatus.HIDDEN,
                reviewRepository.findByIdStrict(review.getId())
                        .orElseThrow().getStatus()
        );

        mvc.perform(post(ADMIN_REVIEWS + "/" + review.getId() + "/approve")
                        .session(session)
                        .param("_csrf", csrfToken))
                .andExpect(status().is3xxRedirection());

        assertEquals(
                ReviewStatus.APPROVED,
                reviewRepository.findByIdStrict(review.getId())
                        .orElseThrow().getStatus()
        );
    }

    private static LocalDateTime pastTime() {
        return LocalDateTime.now(
                java.time.ZoneId.of("Asia/Colombo")
        ).minusDays(1);
    }

}
