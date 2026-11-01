package com.drivingschool.review.service;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.review.model.Review;
import com.drivingschool.review.model.ReviewStatus;
import com.drivingschool.review.repository.ReviewRepository;
import com.drivingschool.student.api.StudentLookup;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AuthSessionService authSessionService;
    private final StudentLookup studentLookup;

    private static final ZoneId COLOMBO_ZONE =
            ZoneId.of("Asia/Colombo");

    public ReviewService(
            ReviewRepository reviewRepository,
            AuthSessionService authSessionService,
            StudentLookup studentLookup) {

        this.reviewRepository = reviewRepository;
        this.authSessionService = authSessionService;
        this.studentLookup = studentLookup;
    }

    public Review createReview(
            HttpServletRequest request,
            int rating,
            String comment) {

        User user = authSessionService.requireLogin(request);

        if (user.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only students can create reviews."
            );
        }

        String studentId = user.getId();

        if (!studentLookup.exists(studentId)) {
            throw new RecordNotFoundException(
                    "Student account not found."
            );
        }

        String trimmedComment = comment == null ? null : comment.trim();

        List<Review> reviews = reviewRepository.readAllStrict();

        boolean alreadyReviewed = reviews.stream()
                .anyMatch(review -> review.getStudentId().equals(studentId));

        if (alreadyReviewed) {
            throw new DuplicateRecordException(
                    "You have already submitted a review."
            );
        }

        String reviewId = reviewRepository.nextId("R");

        LocalDateTime now = LocalDateTime.now(COLOMBO_ZONE);

        Review review = new Review(
                reviewId,
                studentId,
                rating,
                trimmedComment,
                ReviewStatus.PENDING,
                now,
                now
        );

        reviewRepository.save(review);

        return review;
    }

    public List<Review> getVisibleReviews(
            HttpServletRequest request) {

        User user = authSessionService.requireLogin(request);

        List<Review> reviews = reviewRepository.readAllStrict();

        if (user.getRole() == Role.ADMIN) {
            return reviews;
        }

        return reviews.stream()
                .filter(review ->
                        review.getStatus() == ReviewStatus.APPROVED
                                || review.getStudentId().equals(user.getId()))
                .toList();
    }

    public Optional<Review> getVisibleReviewById(
            HttpServletRequest request,
            String reviewId) {

        User user = authSessionService.requireLogin(request);

        validateReviewId(reviewId);

        Optional<Review> result =
                reviewRepository.findByIdStrict(reviewId);

        if (result.isEmpty()) {
            return Optional.empty();
        }

        Review review = result.get();

        if (user.getRole() == Role.ADMIN) {
            return result;
        }

        if (review.getStatus() == ReviewStatus.APPROVED
                || review.getStudentId().equals(user.getId())) {
            return result;
        }

        return Optional.empty();
    }

    public Review updateOwnReview(
            HttpServletRequest request,
            String reviewId,
            int rating,
            String comment) {

        User user = authSessionService.requireLogin(request);

        if (user.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only students can update reviews."
            );
        }

        validateReviewId(reviewId);

        String studentId = user.getId();

        if (!studentLookup.exists(studentId)) {
            throw new RecordNotFoundException(
                    "Student account not found."
            );
        }

        Review review = reviewRepository.findByIdStrict(reviewId)
                .orElseThrow(() -> new RecordNotFoundException(
                        "Review not found."
                ));

        if (!review.getStudentId().equals(studentId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only update your own review."
            );
        }

        LocalDateTime now = LocalDateTime.now(COLOMBO_ZONE);

        boolean changed = review.updateContent(rating, comment, now);

        if (changed) {
            reviewRepository.update(review);
        }

        return review;
    }

    public void deleteOwnReview(
            HttpServletRequest request,
            String reviewId) {

        User user = authSessionService.requireLogin(request);

        if (user.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only students can delete reviews."
            );
        }

        validateReviewId(reviewId);

        String studentId = user.getId();

        if (!studentLookup.exists(studentId)) {
            throw new RecordNotFoundException(
                    "Student account not found."
            );
        }

        Review review = reviewRepository.findByIdStrict(reviewId)
                .orElseThrow(() -> new RecordNotFoundException(
                        "Review not found."
                ));

        if (!review.getStudentId().equals(studentId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only delete your own review."
            );
        }

        reviewRepository.delete(reviewId);
    }

    public Review approveReview(
            HttpServletRequest request,
            String reviewId) {

        authSessionService.requireAdmin(request);
        validateReviewId(reviewId);

        Review review = reviewRepository.findByIdStrict(reviewId)
                .orElseThrow(() -> new RecordNotFoundException(
                        "Review not found."
                ));

        LocalDateTime now = LocalDateTime.now(COLOMBO_ZONE);

        if (review.updateStatus(ReviewStatus.APPROVED, now)) {
            reviewRepository.update(review);
        }

        return review;
    }

    public Review hideReview(
            HttpServletRequest request,
            String reviewId) {

        authSessionService.requireAdmin(request);
        validateReviewId(reviewId);

        Review review = reviewRepository.findByIdStrict(reviewId)
                .orElseThrow(() -> new RecordNotFoundException(
                        "Review not found."
                ));

        LocalDateTime now = LocalDateTime.now(COLOMBO_ZONE);

        if (review.updateStatus(ReviewStatus.HIDDEN, now)) {
            reviewRepository.update(review);
        }

        return review;
    }

    private void validateReviewId(String reviewId) {

        if (reviewId == null
                || !reviewId.matches("R(?!000)[0-9]{3}")) {

            throw new ValidationException(
                    "Review ID must be between R001 and R999."
            );
        }
    }

}