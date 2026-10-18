package com.drivingschool.review.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReviewTest {

    private final LocalDateTime createdAt =
            LocalDateTime.of(2026, 10, 18, 10, 0);

    private Review createReview(int rating, String comment) {
        return new Review(
                "R001",
                "S001",
                rating,
                comment,
                ReviewStatus.PENDING,
                createdAt,
                createdAt
        );
    }

    @Test
    void createsReviewWithValidData() {
        Review review = createReview(5, "Excellent driving school");

        assertEquals("R001", review.getId());
        assertEquals("S001", review.getStudentId());
        assertEquals(5, review.getRating());
        assertEquals("Excellent driving school", review.getComment());
        assertEquals(ReviewStatus.PENDING, review.getStatus());
        assertEquals(createdAt, review.getCreatedAt());
        assertEquals(createdAt, review.getUpdatedAt());
    }

    @Test
    void acceptsMinimumRating() {
        assertDoesNotThrow(() -> createReview(1, "Good service"));
    }

    @Test
    void acceptsMaximumRating() {
        assertDoesNotThrow(() -> createReview(5, "Good service"));
    }

    @Test
    void rejectsRatingBelowOne() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createReview(0, "Good service")
        );
    }

    @Test
    void rejectsRatingAboveFive() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createReview(6, "Good service")
        );
    }

    @Test
    void trimsComment() {
        Review review = createReview(4, "   Good service   ");

        assertEquals("Good service", review.getComment());
    }

    @Test
    void acceptsCommentWithTenCharacters() {
        assertDoesNotThrow(() -> createReview(4, "1234567890"));
    }

    @Test
    void acceptsCommentWithOneThousandCharacters() {
        assertDoesNotThrow(
                () -> createReview(4, "a".repeat(1000))
        );
    }

    @Test
    void rejectsCommentWithNineCharactersAfterTrimming() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createReview(4, "  123456789  ")
        );
    }

    @Test
    void rejectsCommentWithOneThousandAndOneCharacters() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createReview(4, "a".repeat(1001))
        );
    }

    @Test
    void rejectsNullComment() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createReview(4, null)
        );
    }

    @Test
    void rejectsBlankComment() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createReview(4, "          ")
        );
    }

    @Test
    void rejectsNullStatus() {
        assertThrows(
                NullPointerException.class,
                () -> new Review(
                        "R001", "S001", 4, "Good service",
                        null, createdAt, createdAt
                )
        );
    }

    @Test
    void rejectsBlankId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Review(
                        " ", "S001", 4, "Good service",
                        ReviewStatus.PENDING, createdAt, createdAt
                )
        );
    }

    @Test
    void rejectsBlankStudentId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Review(
                        "R001", " ", 4, "Good service",
                        ReviewStatus.PENDING, createdAt, createdAt
                )
        );
    }

    @Test
    void rejectsNullCreatedAt() {
        assertThrows(
                NullPointerException.class,
                () -> new Review(
                        "R001", "S001", 4, "Good service",
                        ReviewStatus.PENDING, null, createdAt
                )
        );
    }

    @Test
    void rejectsNullUpdatedAt() {
        assertThrows(
                NullPointerException.class,
                () -> new Review(
                        "R001", "S001", 4, "Good service",
                        ReviewStatus.PENDING, createdAt, null
                )
        );
    }

    @Test
    void rejectsUpdatedAtEarlierThanCreatedAt() {
        LocalDateTime earlier = createdAt.minusMinutes(1);

        assertThrows(
                IllegalArgumentException.class,
                () -> new Review(
                        "R001", "S001", 4, "Good service",
                        ReviewStatus.PENDING, createdAt, earlier
                )
        );
    }

    @Test
    void editingContentPreservesCreatedAt() {
        Review review = createReview(4, "Good service");
        LocalDateTime editedAt = createdAt.plusHours(1);

        boolean changed = review.updateContent(
                5, "Excellent driving school", editedAt
        );

        assertTrue(changed);
        assertEquals(createdAt, review.getCreatedAt());
        assertEquals(editedAt, review.getUpdatedAt());
        assertEquals(5, review.getRating());
        assertEquals("Excellent driving school", review.getComment());
    }

    @Test
    void unchangedContentDoesNotChangeTimestamp() {
        Review review = createReview(4, "Good service");
        LocalDateTime later = createdAt.plusHours(1);

        boolean changed = review.updateContent(
                4, "  Good service  ", later
        );

        assertFalse(changed);
        assertEquals(createdAt, review.getUpdatedAt());
    }

    @Test
    void rejectsEditTimeEarlierThanUpdatedAt() {
        Review review = createReview(4, "Good service");

        assertThrows(
                IllegalArgumentException.class,
                () -> review.updateContent(
                        5,
                        "Excellent driving school",
                        createdAt.minusMinutes(1)
                )
        );
    }
}