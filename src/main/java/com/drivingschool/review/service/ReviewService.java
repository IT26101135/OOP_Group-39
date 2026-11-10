/**
============================================================
MEMBER   : IT26101137 - [Name]
MODULE   : Reviews and Reports
FILE     : ReviewService.java
WHAT IT DOES (simple words): Checks who may write or moderate a lesson review.
REAL-LIFE EXAMPLE: Like a feedback book and a school manager counting its records.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for reviews; reports READ saved data
============================================================
*/
package com.drivingschool.review.service;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.review.model.Review;
import com.drivingschool.review.model.ReviewStatus;
import com.drivingschool.review.api.ReviewStore;
import com.drivingschool.student.api.StudentLookup;
import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.instructor.model.Instructor;
import com.drivingschool.instructor.repository.InstructorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
// SOLID: S - Review rules live here; controllers handle forms and repositories handle files.
public class ReviewService implements ReviewOperations, ReviewModeration {

    /* Shared by every ReviewService instance in this JVM. */
    private static final Object REVIEW_WRITE_LOCK = new Object();
    private static final ZoneId COLOMBO_ZONE = ZoneId.of("Asia/Colombo");

    private final ReviewStore reviewRepository;
    private final AuthSessionService authSessionService;
    private final StudentLookup studentLookup;
    private final Clock clock;
    @Autowired(required = false) private BookingRepository bookings;
    @Autowired(required = false) private InstructorRepository instructors;

    // Receives the details this class needs and keeps them ready for its own job.
    public ReviewService(
            ReviewStore reviewRepository,
            AuthSessionService authSessionService,
            StudentLookup studentLookup,
            Clock clock) {
        this.reviewRepository = reviewRepository;
        this.authSessionService = authSessionService;
        this.studentLookup = studentLookup;
        this.clock = clock;
    }

    // Uses the configured clock and Colombo time for saved feedback timestamps.
    private LocalDateTime now() {
        return LocalDateTime.now(clock.withZone(COLOMBO_ZONE));
    }

    // Allows the legacy unlinked-review path only when lesson checks are not configured.
    // CRUD: CREATE - adds a new review and saves it
    public Review createReview(HttpServletRequest request, int rating, String comment) {
        if (bookings != null) {
            throw new ValidationException("Select one of your completed lessons to review.");
        }
        return createReviewRecord(request, null, null, rating, comment);
    }

    // Accepts feedback only for the signed-in student's completed lesson.
    // CRUD: CREATE - adds a new review and saves it
    public Review createReview(HttpServletRequest request, String bookingId,
                               int rating, String comment) {
        if (bookings == null) {
            throw new ValidationException("We could not check this lesson. Try again before sending your review.");
        }
        User user = authSessionService.requireLogin(request);
        Booking booking = bookings.findByIdStrict(bookingId)
                .orElseThrow(() -> new RecordNotFoundException("Booking not found."));
        if (!booking.getStudentId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can review only your own completed lessons.");
        }
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new ValidationException("A lesson must be completed before it can be reviewed.");
        }
        return createReviewRecord(request, bookingId, booking.getInstructorId(), rating, comment);
    }

    // Holds the write lock while checking duplicates and saving validated pending feedback.
    // CRUD: CREATE - adds a new review and saves it
    private Review createReviewRecord(HttpServletRequest request, String bookingId,
                                      String instructorId, int rating, String comment) {
        synchronized (REVIEW_WRITE_LOCK) {
            // Step 1: Check the signed-in student before reserving a review ID.
            User user = authSessionService.requireLogin(request);
            if (user.getRole() != Role.STUDENT) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Only students can create reviews.");
            }
            String studentId = user.getId();
            if (!studentLookup.exists(studentId)) {
                throw new RecordNotFoundException("Student account not found.");
            }
            String trimmedComment = comment == null ? null : comment.trim();
            List<Review> reviews = reviewRepository.readAllStrict();
            // Step 2: One completed lesson gets one review, even after it is hidden.
            ensureNoDuplicate(reviews, studentId, bookingId);
            // Step 3: Validate the feedback and save it as pending moderation.
            String reviewId = reviewRepository.nextId("R");
            LocalDateTime timestamp = now();
            Review review = new Review(reviewId, studentId, bookingId, instructorId, rating, trimmedComment,
                    ReviewStatus.PENDING, timestamp, timestamp);
            reviewRepository.save(review);
            return review;
        }
    }

    // CRUD: READ   - finds or lists reviews
    // Applies the same visibility rule to the list and an individual review.
    public List<Review> getVisibleReviews(HttpServletRequest request) {
        User user = authSessionService.requireLogin(request);
        List<Review> visible = new ArrayList<>();
        for (Review review : reviewRepository.readAllStrict()) {
            if (canSeeReview(user, review)) {
                visible.add(review);
            }
        }
        return visible;
    }

    // CRUD: READ   - finds or lists reviews
    // Returns only approved feedback to visitors; no login is needed for public feedback.
    public List<Review> getApprovedReviews() {
        List<Review> approved = new ArrayList<>();
        for (Review review : reviewRepository.readAllStrict()) {
            if (review.getStatus() == ReviewStatus.APPROVED) {
                approved.add(review);
            }
        }
        return approved;
    }

    // CRUD: READ   - finds or lists reviews
    // Looking up an ID must reveal no more feedback than browsing the list.
    public Optional<Review> getVisibleReviewById(HttpServletRequest request, String reviewId) {
        User user = authSessionService.requireLogin(request);
        validateReviewId(reviewId);
        Optional<Review> result = reviewRepository.findByIdStrict(reviewId);
        if (result.isPresent() && canSeeReview(user, result.get())) {
            return result;
        }
        return Optional.empty();
    }

    // Instructors see feedback attached to them; students see their own or approved feedback.
    private boolean canSeeReview(User user, Review review) {
        if (user.getRole() == Role.ADMIN) {
            return true;
        }
        if (user.getRole() == Role.INSTRUCTOR) {
            return user.getId().equals(review.getInstructorId());
        }
        return review.getStatus() == ReviewStatus.APPROVED || review.getStudentId().equals(user.getId());
    }

    // Checks the author before saving changed feedback and refreshing public ratings.
    // CRUD: UPDATE - changes an existing review
    public Review updateOwnReview(HttpServletRequest request, String reviewId,
                                  int rating, String comment) {
        synchronized (REVIEW_WRITE_LOCK) {
            Review review = ownReview(request, reviewId, "update");
            if (review.updateContent(rating, comment, now())) {
                reviewRepository.update(review);
                recalculateRating(review.getInstructorId());
            }
            return review;
        }
    }

    // Checks the author before removing feedback and refreshing public ratings.
    // CRUD: DELETE - removes a review
    public void deleteOwnReview(HttpServletRequest request, String reviewId) {
        synchronized (REVIEW_WRITE_LOCK) {
            Review review = ownReview(request, reviewId, "delete");
            String instructorId = review.getInstructorId();
            reviewRepository.delete(reviewId);
            recalculateRating(instructorId);
        }
    }

    // Requires an administrator before publishing feedback and refreshing public ratings.
    // CRUD: UPDATE - changes an existing review
    public Review approveReview(HttpServletRequest request, String reviewId) {
        synchronized (REVIEW_WRITE_LOCK) {
            authSessionService.requireAdmin(request);
            validateReviewId(reviewId);
            Review review = requireReview(reviewId);
            if (review.updateStatus(ReviewStatus.APPROVED, now())) {
                reviewRepository.update(review);
            }
            recalculateRating(review.getInstructorId());
            return review;
        }
    }

    // Requires an administrator before hiding feedback and refreshing public ratings.
    // CRUD: UPDATE - changes an existing review
    public Review hideReview(HttpServletRequest request, String reviewId) {
        synchronized (REVIEW_WRITE_LOCK) {
            authSessionService.requireAdmin(request);
            validateReviewId(reviewId);
            Review review = requireReview(reviewId);
            if (review.updateStatus(ReviewStatus.HIDDEN, now())) {
                reviewRepository.update(review);
            }
            recalculateRating(review.getInstructorId());
            return review;
        }
    }

    // Requires an administrator before removing feedback and refreshing public ratings.
    // CRUD: DELETE - removes a review
    public void deleteReview(HttpServletRequest request, String reviewId) {
        synchronized (REVIEW_WRITE_LOCK) {
            authSessionService.requireAdmin(request);
            validateReviewId(reviewId);
            Review review = requireReview(reviewId);
            reviewRepository.delete(reviewId);
            recalculateRating(review.getInstructorId());
        }
    }

    // Lists the saved feedback attached to a teaching profile.
    // CRUD: READ   - finds or lists reviews
    public List<Review> reviewsForInstructor(String instructorId) {
        List<Review> result = new ArrayList<>();
        for (Review review : reviewRepository.readAllStrict()) {
            if (instructorId != null && instructorId.equals(review.getInstructorId())) {
                result.add(review);
            }
        }
        return result;
    }

    // Recounts approved stars after moderation, editing or deletion changes the public rating.
    private void recalculateRating(String instructorId) {
        if (instructorId == null || instructors == null) {
            return;
        }
        Optional<Instructor> result = instructors.findByIdStrict(instructorId);
        if (result.isPresent()) {
            Instructor instructor = result.get();
            instructor.setAverageRating(approvedAverage(instructorId));
            instructors.update(instructor);
        }
    }

    // Divides the approved star total by its count; an instructor with no feedback starts at zero.
    private double approvedAverage(String instructorId) {
        int ratingTotal = 0;
        int approvedCount = 0;
        for (Review review : reviewsForInstructor(instructorId)) {
            if (review.getStatus() == ReviewStatus.APPROVED) {
                ratingTotal += review.getRating();
                approvedCount++;
            }
        }
        if (approvedCount == 0) {
            return 0.0;
        }
        double average = (double) ratingTotal / approvedCount;
        return Math.round(average * 100.0) / 100.0;
    }

    // Refuses duplicate lesson reviews while still supporting older unlinked review records.
    private void ensureNoDuplicate(List<Review> reviews, String studentId, String bookingId) {
        for (Review review : reviews) {
            boolean duplicate;
            if (bookingId == null) {
                duplicate = review.getStudentId().equals(studentId);
            } else {
                duplicate = bookingId.equals(review.getBookingId());
            }
            if (duplicate) {
                throw new DuplicateRecordException("You have already submitted a review.");
            }
        }
    }

    // Checks the signed-in student and record owner before a feedback change.
    private Review ownReview(HttpServletRequest request, String reviewId, String action) {
        User student = authSessionService.requireLogin(request);
        if (student.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only students can " + action + " reviews.");
        }
        validateReviewId(reviewId);
        if (!studentLookup.exists(student.getId())) {
            throw new RecordNotFoundException("Student account not found.");
        }
        Review review = requireReview(reviewId);
        if (!review.getStudentId().equals(student.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only " + action + " your own review.");
        }
        return review;
    }

    // Turns a missing feedback record into a friendly page instead of an empty lookup failure.
    private Review requireReview(String reviewId) {
        Optional<Review> result = reviewRepository.findByIdStrict(reviewId);
        if (result.isEmpty()) {
            throw new RecordNotFoundException("Review not found.");
        }
        return result.get();
    }

    // Refuses malformed feedback IDs before opening the review file.
    private void validateReviewId(String reviewId) {
        if (reviewId == null || !reviewId.matches("R(?!000)[0-9]{3}")) {
            throw new ValidationException("We could not identify this review. Return to your reviews and try again.");
        }
    }
}
