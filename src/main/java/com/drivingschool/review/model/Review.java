package com.drivingschool.review.model;

import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.common.model.Identifiable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class Review implements Identifiable {

    private final String id;
    private final String studentId;

    private int rating;
    private String comment;
    private ReviewStatus status;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @JsonCreator
    public Review(
            @JsonProperty("id") String id,
            @JsonProperty("studentId") String studentId,
            @JsonProperty("rating") int rating,
            @JsonProperty("comment") String comment,
            @JsonProperty("status") ReviewStatus status,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("updatedAt") LocalDateTime updatedAt) {

        this.id = validateId(id, "R");
        this.studentId = validateId(studentId, "S");
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

    private static void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new ValidationException(
                    "rating must be between 1 and 5");
        }
    }

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

    private static <T> T requireValue(T value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " cannot be null");
        }

        return value;
    }

    @Override
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
        requireValue(changeTime, "changeTime");

        if (changeTime.isBefore(createdAt)
                || changeTime.isBefore(updatedAt)) {
            throw new ValidationException(
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