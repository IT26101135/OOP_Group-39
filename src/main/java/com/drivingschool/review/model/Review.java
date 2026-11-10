/**
============================================================
MEMBER   : IT26101137 - [Name]
MODULE   : Reviews and Reports
FILE     : Review.java
WHAT IT DOES (simple words): Keeps validated feedback and its approval status together.
REAL-LIFE EXAMPLE: Like a feedback book and a school manager counting its records.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for reviews; reports READ saved data
============================================================
*/
package com.drivingschool.review.model;

import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.common.model.Identifiable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

// OOP: ENCAPSULATION - Feedback is private and can change only after validation.
// OOP: EXCEPTION HANDLING - Invalid stars, comments and timestamps throw ValidationException.
public class Review implements Identifiable {

    private final String id;
    private final String studentId;
    private final String bookingId;
    private final String instructorId;

    private int rating;
    private String comment;
    private ReviewStatus status;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Receives the details this class needs and keeps them ready for its own job.
    public Review(
            String id, String studentId, int rating, String comment,
            ReviewStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, studentId, null, null, rating, comment, status, createdAt, updatedAt);
    }

    // Receives the details this class needs and keeps them ready for its own job.
    @JsonCreator
    public Review(@JsonProperty("id") String id,
                  @JsonProperty("studentId") String studentId,
                  @JsonProperty("bookingId") String bookingId,
                  @JsonProperty("instructorId") String instructorId,
                  @JsonProperty("rating") int rating,
                  @JsonProperty("comment") String comment,
                  @JsonProperty("status") ReviewStatus status,
                  @JsonProperty("createdAt") LocalDateTime createdAt,
                  @JsonProperty("updatedAt") LocalDateTime updatedAt) {

        this.id = validateId(id, "R");
        this.studentId = validateId(studentId, "S");
        this.bookingId = optionalId(bookingId, "B");
        this.instructorId = optionalId(instructorId, "I");
        validateRating(rating);

        this.rating = rating;
        this.comment = validateComment(comment);
        this.status = requireValue(status, "status");
        this.createdAt = requireValue(createdAt, "createdAt");
        this.updatedAt = requireValue(updatedAt, "updatedAt");

        if (updatedAt.isBefore(createdAt)) {
            throw new ValidationException(
                    "updatedAt cannot be earlier than createdAt");
        }
    }

    // Refuses malformed IDs before looking in the filing drawer.
    private static String validateId(String value, String prefix) {
        if (value == null || !value.matches(prefix + "[0-9]{3}")) {
            throw new ValidationException(
                    "ID must match " + prefix + "001 to " + prefix + "999");
        }

        int number = Integer.parseInt(value.substring(1));

        if (number < 1 || number > 999) {
            throw new ValidationException(
                    "ID number must be between 001 and 999");
        }

        return value;
    }

    // Handles the validate rating step for this page or record.
    private static void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new ValidationException(
                    "rating must be between 1 and 5");
        }
    }

    // Handles the validate comment step for this page or record.
    private static String validateComment(String comment) {
        if (comment == null) {
            throw new ValidationException("comment cannot be null");
        }

        String trimmed = comment.trim();

        if (trimmed.length() < 10 || trimmed.length() > 1000) {
            throw new ValidationException(
                    "comment must contain 10 to 1000 characters");
        }

        return trimmed;
    }

    // Refuses a missing required value while preserving its original Java type.
    private static <T> T requireValue(T value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " cannot be null");
        }

        return value;
    }

    // Returns the id for display or school rules.
    @Override
    public String getId() {
        return id;
    }

    // Returns the student id for display or school rules.
    public String getStudentId() {
        return studentId;
    }
    // Returns the booking id for display or school rules.
    public String getBookingId() { return bookingId; }
    // Returns the instructor id for display or school rules.
    public String getInstructorId() { return instructorId; }

    // Handles the optional id step for this page or record.
    private String optionalId(String value, String prefix) {
        if (value == null) return null;
        return validateId(value, prefix);
    }

    // Returns the rating for display or school rules.
    public int getRating() {
        return rating;
    }

    // Returns the comment for display or school rules.
    public String getComment() {
        return comment;
    }

    // Returns the status for display or school rules.
    public ReviewStatus getStatus() {
        return status;
    }

    // Returns the created at for display or school rules.
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Returns the updated at for display or school rules.
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Handles the update content step for this page or record.
    public boolean updateContent(
            int newRating,
            String newComment,
            LocalDateTime changeTime) {

        // Step 1: Check the replacement stars, comment and time before changing feedback.
        validateRating(newRating);
        String trimmedComment = validateComment(newComment);
        requireValue(changeTime, "changeTime");

        if (changeTime.isBefore(createdAt)
                || changeTime.isBefore(updatedAt)) {
            throw new ValidationException(
                    "changeTime cannot be earlier than existing timestamps");
        }

        // Step 2: Changed feedback returns to the approval queue; identical feedback stays as it is.
        boolean changed = this.rating != newRating
                || !this.comment.equals(trimmedComment);

        if (changed) {
            this.rating = newRating;
            this.comment = trimmedComment;
            this.status = ReviewStatus.PENDING;
            this.updatedAt = changeTime;
        }

        return changed;
    }

    // Handles the update status step for this page or record.
    public boolean updateStatus(
            ReviewStatus newStatus,
            LocalDateTime changeTime) {

        // Step 1: Moderation can publish or hide feedback, but cannot create a new pending review.
        requireValue(newStatus, "status");
        requireValue(changeTime, "changeTime");

        if (newStatus == ReviewStatus.PENDING) {
            throw new ValidationException(
                    "Moderation status must be APPROVED or HIDDEN.");
        }

        // Step 2: An identical moderation choice needs no file rewrite or newer timestamp.
        if (this.status == newStatus) {
            return false;
        }

        if (changeTime.isBefore(createdAt)
                || changeTime.isBefore(updatedAt)) {
            throw new ValidationException(
                    "changeTime cannot be earlier than existing timestamps");
        }

        // Step 3: Store the checked moderation decision and its time.
        this.status = newStatus;
        this.updatedAt = changeTime;

        return true;
    }
}
