package com.drivingschool.review.model;

import com.drivingschool.common.model.Identifiable;
import java.time.LocalDateTime;
import java.util.Objects;

public class Review implements Identifiable {

    private final String id;
    private final String studentId;

    private int rating;
    private String comment;
    private ReviewStatus status;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Review(
            String id,
            String studentId,
            int rating,
            String comment,
            ReviewStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.id = validateId(id, "id");
        this.studentId = validateId(studentId, "studentId");
        validateRating(rating);

        this.rating = rating;
        this.comment = validateComment(comment);

        this.status = Objects.requireNonNull(
                status, "status cannot be null");

        this.createdAt = Objects.requireNonNull(
                createdAt, "createdAt cannot be null");

        this.updatedAt = Objects.requireNonNull(
                updatedAt, "updatedAt cannot be null");

        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException(
                    "updatedAt cannot be earlier than createdAt");
        }
    }

    private static String validateId(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be null or blank");
        }

        if (!value.equals(value.trim())) {
            throw new IllegalArgumentException(
                    fieldName + " cannot contain surrounding whitespace");
        }

        return value;
    }

    private static void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException(
                    "rating must be between 1 and 5");
        }
    }

    private static String validateComment(String comment) {
        if (comment == null) {
            throw new IllegalArgumentException(
                    "comment cannot be null");
        }

        String trimmed = comment.trim();

        if (trimmed.length() < 10 || trimmed.length() > 1000) {
            throw new IllegalArgumentException(
                    "comment must contain 10 to 1000 characters");
        }

        return trimmed;
    }

    public String getId() {
        return id;
    }

    public String getStudentId() {
        return studentId;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean updateContent(
            int newRating,
            String newComment,
            LocalDateTime changeTime) {

        validateRating(newRating);
        String trimmedComment = validateComment(newComment);

        Objects.requireNonNull(
                changeTime, "changeTime cannot be null");

        if (changeTime.isBefore(createdAt)
                || changeTime.isBefore(updatedAt)) {
            throw new IllegalArgumentException(
                    "changeTime cannot be earlier than existing timestamps");
        }

        boolean changed = this.rating != newRating
                || !this.comment.equals(trimmedComment);

        if (changed) {
            this.rating = newRating;
            this.comment = trimmedComment;
            this.updatedAt = changeTime;
        }

        return changed;
    }
}