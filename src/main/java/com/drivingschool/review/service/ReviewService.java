// Module owner: IT26101137
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
import java.util.Optional;

@Service
public class ReviewService {

    /* Shared by every ReviewService instance in this JVM. */
    private static final Object REVIEW_WRITE_LOCK = new Object();
    private static final ZoneId COLOMBO_ZONE = ZoneId.of("Asia/Colombo");

    private final ReviewStore reviewRepository;
    private final AuthSessionService authSessionService;
    private final StudentLookup studentLookup;
    private final Clock clock;
    @Autowired(required = false) private BookingRepository bookings;
    @Autowired(required = false) private InstructorRepository instructors;

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

    private LocalDateTime now() {
        return LocalDateTime.now(clock.withZone(COLOMBO_ZONE));
    }

    public Review createReview(HttpServletRequest request, int rating, String comment) {
        if (bookings != null) throw new ValidationException("Select one of your completed lessons to review.");
        return createReviewRecord(request, null, null, rating, comment);
    }

    public Review createReview(HttpServletRequest request, String bookingId,
                               int rating, String comment) {
        if (bookings == null) throw new ValidationException("Booking review checks are unavailable.");
        User user = authSessionService.requireLogin(request);
        Booking booking = bookings.findByIdStrict(bookingId)
                .orElseThrow(() -> new RecordNotFoundException("Booking not found."));
        if (!booking.getStudentId().equals(user.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "You can review only your own completed lessons.");
        if (booking.getStatus() != BookingStatus.COMPLETED)
            throw new ValidationException("A lesson must be completed before it can be reviewed.");
        return createReviewRecord(request, bookingId, booking.getInstructorId(), rating, comment);
    }

    private Review createReviewRecord(HttpServletRequest request, String bookingId,
                                      String instructorId, int rating, String comment) {
        synchronized (REVIEW_WRITE_LOCK) {
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
            boolean alreadyReviewed = bookingId == null
                    ? reviews.stream().anyMatch(review -> review.getStudentId().equals(studentId))
                    : reviews.stream().anyMatch(review -> bookingId.equals(review.getBookingId()));
            if (alreadyReviewed) {
                throw new DuplicateRecordException("You have already submitted a review.");
            }
            String reviewId = reviewRepository.nextId("R");
            LocalDateTime timestamp = now();
            Review review = new Review(reviewId, studentId, bookingId, instructorId, rating, trimmedComment,
                    ReviewStatus.PENDING, timestamp, timestamp);
            reviewRepository.save(review);
            return review;
        }
    }

    public List<Review> getVisibleReviews(HttpServletRequest request) {
        User user = authSessionService.requireLogin(request);
        List<Review> reviews = reviewRepository.readAllStrict();
        if (user.getRole() == Role.ADMIN) return reviews;
        if (user.getRole() == Role.INSTRUCTOR) return reviews.stream()
                .filter(review -> user.getId().equals(review.getInstructorId())).toList();
        return reviews.stream()
                .filter(review -> review.getStatus() == ReviewStatus.APPROVED
                        || review.getStudentId().equals(user.getId()))
                .toList();
    }

    public Optional<Review> getVisibleReviewById(HttpServletRequest request, String reviewId) {
        User user = authSessionService.requireLogin(request);
        validateReviewId(reviewId);
        Optional<Review> result = reviewRepository.findByIdStrict(reviewId);
        if (result.isEmpty()) return Optional.empty();
        Review review = result.get();
        if (user.getRole() == Role.ADMIN || review.getStatus() == ReviewStatus.APPROVED
                || review.getStudentId().equals(user.getId())) return result;
        return Optional.empty();
    }

    public Review updateOwnReview(HttpServletRequest request, String reviewId,
                                  int rating, String comment) {
        synchronized (REVIEW_WRITE_LOCK) {
            User user = authSessionService.requireLogin(request);
            if (user.getRole() != Role.STUDENT) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Only students can update reviews.");
            }
            validateReviewId(reviewId);
            String studentId = user.getId();
            if (!studentLookup.exists(studentId)) {
                throw new RecordNotFoundException("Student account not found.");
            }
            Review review = reviewRepository.findByIdStrict(reviewId)
                    .orElseThrow(() -> new RecordNotFoundException("Review not found."));
            if (!review.getStudentId().equals(studentId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You can only update your own review.");
            }
            if (review.updateContent(rating, comment, now())) {
                reviewRepository.update(review);
                recalculateRating(review.getInstructorId());
            }
            return review;
        }
    }

    public void deleteOwnReview(HttpServletRequest request, String reviewId) {
        synchronized (REVIEW_WRITE_LOCK) {
            User user = authSessionService.requireLogin(request);
            if (user.getRole() != Role.STUDENT) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Only students can delete reviews.");
            }
            validateReviewId(reviewId);
            String studentId = user.getId();
            if (!studentLookup.exists(studentId)) {
                throw new RecordNotFoundException("Student account not found.");
            }
            Review review = reviewRepository.findByIdStrict(reviewId)
                    .orElseThrow(() -> new RecordNotFoundException("Review not found."));
            if (!review.getStudentId().equals(studentId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You can only delete your own review.");
            }
            String instructorId = review.getInstructorId();
            reviewRepository.delete(reviewId);
            recalculateRating(instructorId);
        }
    }

    public Review approveReview(HttpServletRequest request, String reviewId) {
        synchronized (REVIEW_WRITE_LOCK) {
            authSessionService.requireAdmin(request);
            validateReviewId(reviewId);
            Review review = reviewRepository.findByIdStrict(reviewId)
                    .orElseThrow(() -> new RecordNotFoundException("Review not found."));
            if (review.updateStatus(ReviewStatus.APPROVED, now())) reviewRepository.update(review);
            recalculateRating(review.getInstructorId());
            return review;
        }
    }

    public Review hideReview(HttpServletRequest request, String reviewId) {
        synchronized (REVIEW_WRITE_LOCK) {
            authSessionService.requireAdmin(request);
            validateReviewId(reviewId);
            Review review = reviewRepository.findByIdStrict(reviewId)
                    .orElseThrow(() -> new RecordNotFoundException("Review not found."));
            if (review.updateStatus(ReviewStatus.HIDDEN, now())) reviewRepository.update(review);
            recalculateRating(review.getInstructorId());
            return review;
        }
    }

    public void deleteReview(HttpServletRequest request, String reviewId) {
        synchronized (REVIEW_WRITE_LOCK) {
            authSessionService.requireAdmin(request);
            validateReviewId(reviewId);
            Review review = reviewRepository.findByIdStrict(reviewId)
                    .orElseThrow(() -> new RecordNotFoundException("Review not found."));
            reviewRepository.delete(reviewId);
            recalculateRating(review.getInstructorId());
        }
    }

    public List<Review> reviewsForInstructor(String instructorId) {
        return reviewRepository.readAllStrict().stream()
                .filter(review -> instructorId.equals(review.getInstructorId()))
                .toList();
    }

    private void recalculateRating(String instructorId) {
        if (instructorId == null || instructors == null) return;
        Instructor instructor = instructors.findByIdStrict(instructorId).orElse(null);
        if (instructor == null) return;
        List<Review> approved = reviewRepository.readAllStrict().stream()
                .filter(review -> instructorId.equals(review.getInstructorId())
                        && review.getStatus() == ReviewStatus.APPROVED).toList();
        double average = approved.stream().mapToInt(Review::getRating).average().orElse(0.0);
        instructor.setAverageRating(Math.round(average * 100.0) / 100.0);
        instructors.update(instructor);
    }

    private void validateReviewId(String reviewId) {
        if (reviewId == null || !reviewId.matches("R(?!000)[0-9]{3}")) {
            throw new ValidationException("Review ID must be between R001 and R999.");
        }
    }
}
