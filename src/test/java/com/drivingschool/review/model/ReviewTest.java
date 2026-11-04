package com.drivingschool.review.model;

import com.drivingschool.common.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

    private ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private Review createReviewWithStatus(ReviewStatus status) {
        return new Review(
                "R001",
                "S001",
                5,
                "Excellent driving school",
                status,
                createdAt,
                createdAt.plusHours(1)
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
                ValidationException.class,
                () -> createReview(0, "Good service")
        );
    }

    @Test
    void rejectsRatingAboveFive() {
        assertThrows(
                ValidationException.class,
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
                ValidationException.class,
                () -> createReview(4, "  123456789  ")
        );
    }

    @Test
    void rejectsCommentWithOneThousandAndOneCharacters() {
        assertThrows(
                ValidationException.class,
                () -> createReview(4, "a".repeat(1001))
        );
    }

    @Test
    void rejectsNullComment() {
        assertThrows(
                ValidationException.class,
                () -> createReview(4, null)
        );
    }

    @Test
    void rejectsBlankComment() {
        assertThrows(
                ValidationException.class,
                () -> createReview(4, "          ")
        );
    }

    @Test
    void rejectsNullStatus() {
        assertThrows(
                ValidationException.class,
                () -> new Review(
                        "R001", "S001", 4, "Good service",
                        null, createdAt, createdAt
                )
        );
    }

    @Test
    void rejectsNullCreatedAt() {
        assertThrows(
                ValidationException.class,
                () -> new Review(
                        "R001", "S001", 4, "Good service",
                        ReviewStatus.PENDING, null, createdAt
                )
        );
    }

    @Test
    void rejectsNullUpdatedAt() {
        assertThrows(
                ValidationException.class,
                () -> new Review(
                        "R001", "S001", 4, "Good service",
                        ReviewStatus.PENDING, createdAt, null
                )
        );
    }

    @Test
    void acceptsValidReviewIdAndStudentIdFormats() {
        assertDoesNotThrow(() -> new Review(
                "R001", "S001", 4, "Good service",
                ReviewStatus.PENDING, createdAt, createdAt
        ));

        assertDoesNotThrow(() -> new Review(
                "R999", "S999", 4, "Good service",
                ReviewStatus.PENDING, createdAt, createdAt
        ));
    }

    @Test
    void rejectsInvalidReviewIds() {
        String[] invalidIds = {"R000", "R01", "R1000", "abc", " R001"};

        for (String id : invalidIds) {
            assertThrows(
                    ValidationException.class,
                    () -> new Review(
                            id, "S001", 4, "Good service",
                            ReviewStatus.PENDING, createdAt, createdAt
                    ),
                    "Expected invalid review ID to be rejected: " + id
            );
        }
    }

    @Test
    void rejectsInvalidStudentIds() {
        String[] invalidIds = {"S000", "S01", "S1000", "abc", "V001", " S001"};

        for (String studentId : invalidIds) {
            assertThrows(
                    ValidationException.class,
                    () -> new Review(
                            "R001", studentId, 4, "Good service",
                            ReviewStatus.PENDING, createdAt, createdAt
                    ),
                    "Expected invalid student ID to be rejected: " + studentId
            );
        }
    }

    @Test
    void rejectsUpdatedAtEarlierThanCreatedAt() {
        LocalDateTime earlier = createdAt.minusMinutes(1);

        assertThrows(
                ValidationException.class,
                () -> new Review(
                        "R001", "S001", 4, "Good service",
                        ReviewStatus.PENDING, createdAt, earlier
                )
        );
    }

    @Test
    void jsonRoundTripPreservesAllSevenFields() throws Exception {
        ObjectMapper mapper = objectMapper();
        Review original = createReviewWithStatus(ReviewStatus.PENDING);

        String json = mapper.writeValueAsString(original);
        Review restored = mapper.readValue(json, Review.class);

        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getStudentId(), restored.getStudentId());
        assertEquals(original.getRating(), restored.getRating());
        assertEquals(original.getComment(), restored.getComment());
        assertEquals(original.getStatus(), restored.getStatus());
        assertEquals(original.getCreatedAt(), restored.getCreatedAt());
        assertEquals(original.getUpdatedAt(), restored.getUpdatedAt());
    }

    @Test
    void jsonCanReconstructApprovedAndHiddenReviews() throws Exception {
        ObjectMapper mapper = objectMapper();

        for (ReviewStatus status : new ReviewStatus[]{
                ReviewStatus.APPROVED, ReviewStatus.HIDDEN
        }) {
            Review original = createReviewWithStatus(status);
            String json = mapper.writeValueAsString(original);
            Review restored = mapper.readValue(json, Review.class);

            assertEquals(status, restored.getStatus());
            assertEquals(original.getId(), restored.getId());
            assertEquals(original.getCreatedAt(), restored.getCreatedAt());
            assertEquals(original.getUpdatedAt(), restored.getUpdatedAt());
        }
    }

    @Test
    void jsonDeserializationRejectsInvalidStoredReview() throws Exception {
        ObjectMapper mapper = objectMapper();

        String invalidJson = """
                {
                  "id": "R000",
                  "studentId": "S001",
                  "rating": 4,
                  "comment": "Good service",
                  "status": "PENDING",
                  "createdAt": "2026-10-18T10:00:00",
                  "updatedAt": "2026-10-18T10:00:00"
                }
                """;

        assertThrows(
                JsonMappingException.class,
                () -> mapper.readValue(invalidJson, Review.class)
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
    void ratingOnlyUpdateChangesRating() {
        Review review = createReview(4, "Good service");
        LocalDateTime editedAt = createdAt.plusHours(1);

        boolean changed = review.updateContent(5, "Good service", editedAt);

        assertTrue(changed);
        assertEquals(5, review.getRating());
        assertEquals("Good service", review.getComment());
        assertEquals(editedAt, review.getUpdatedAt());
    }

    @Test
    void commentOnlyUpdateChangesComment() {
        Review review = createReview(4, "Good service");
        LocalDateTime editedAt = createdAt.plusHours(1);

        boolean changed = review.updateContent(
                4, "Excellent driving school", editedAt
        );

        assertTrue(changed);
        assertEquals(4, review.getRating());
        assertEquals("Excellent driving school", review.getComment());
        assertEquals(editedAt, review.getUpdatedAt());
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
    void approvingReviewUpdatesStatusAndTimestamp() {
        Review review = createReview(4, "Good service");
        LocalDateTime approvedAt = createdAt.plusHours(1);

        boolean changed = review.updateStatus(
                ReviewStatus.APPROVED,
                approvedAt
        );

        assertTrue(changed);
        assertEquals(ReviewStatus.APPROVED, review.getStatus());
        assertEquals(createdAt, review.getCreatedAt());
        assertEquals(approvedAt, review.getUpdatedAt());
    }

    @Test
    void approvingAlreadyApprovedReviewDoesNotChangeTimestamp() {
        Review review = createReviewWithStatus(ReviewStatus.APPROVED);
        LocalDateTime originalUpdatedAt = review.getUpdatedAt();

        boolean changed = review.updateStatus(
                ReviewStatus.APPROVED,
                createdAt.plusHours(2)
        );

        assertFalse(changed);
        assertEquals(ReviewStatus.APPROVED, review.getStatus());
        assertEquals(originalUpdatedAt, review.getUpdatedAt());
    }

    @Test
    void invalidRatingUpdateLeavesStateUnchanged() {
        assertInvalidUpdateLeavesStateUnchanged(
                0, "Excellent driving school", createdAt.plusHours(1)
        );
    }

    @Test
    void invalidCommentUpdateLeavesStateUnchanged() {
        assertInvalidUpdateLeavesStateUnchanged(
                5, "short", createdAt.plusHours(1)
        );
    }

    @Test
    void nullChangeTimeLeavesStateUnchanged() {
        assertInvalidUpdateLeavesStateUnchanged(
                5, "Excellent driving school", null
        );
    }

    @Test
    void backwardsTimestampAfterLastEditLeavesStateUnchanged() {
        Review review = createReview(4, "Good service");
        LocalDateTime firstEdit = createdAt.plusHours(2);
        LocalDateTime backwardsTime = createdAt.plusHours(1);

        review.updateContent(5, "Excellent driving school", firstEdit);

        assertThrows(
                ValidationException.class,
                () -> review.updateContent(
                        3, "Needs improvement in lessons", backwardsTime
                )
        );

        assertEquals(5, review.getRating());
        assertEquals("Excellent driving school", review.getComment());
        assertEquals(ReviewStatus.PENDING, review.getStatus());
        assertEquals(createdAt, review.getCreatedAt());
        assertEquals(firstEdit, review.getUpdatedAt());
    }

    private void assertInvalidUpdateLeavesStateUnchanged(
            int newRating,
            String newComment,
            LocalDateTime changeTime) {

        Review review = createReview(4, "Good service");

        int originalRating = review.getRating();
        String originalComment = review.getComment();
        ReviewStatus originalStatus = review.getStatus();
        LocalDateTime originalCreatedAt = review.getCreatedAt();
        LocalDateTime originalUpdatedAt = review.getUpdatedAt();

        assertThrows(
                ValidationException.class,
                () -> review.updateContent(
                        newRating, newComment, changeTime
                )
        );

        assertEquals(originalRating, review.getRating());
        assertEquals(originalComment, review.getComment());
        assertEquals(originalStatus, review.getStatus());
        assertEquals(originalCreatedAt, review.getCreatedAt());
        assertEquals(originalUpdatedAt, review.getUpdatedAt());
    }

    @Test
    void serializesAndDeserializesTimestampsAsIsoStrings() throws Exception {
        Review original = createReview(5, "Excellent driving school");
        String json = objectMapper().writeValueAsString(original);

        com.fasterxml.jackson.databind.JsonNode node =
                objectMapper().readTree(json);
        assertTrue(node.get("createdAt").isTextual());
        assertTrue(node.get("updatedAt").isTextual());
        assertEquals(createdAt, LocalDateTime.parse(node.get("createdAt").asText()));
        assertEquals(createdAt, LocalDateTime.parse(node.get("updatedAt").asText()));

        Review restored = objectMapper().readValue(json, Review.class);
        assertEquals(original.getCreatedAt(), restored.getCreatedAt());
        assertEquals(original.getUpdatedAt(), restored.getUpdatedAt());
    }

    @Test
    void editingApprovedReviewResetsStatusToPending() {
        Review review = createReviewWithStatus(ReviewStatus.APPROVED);
        assertTrue(review.updateContent(4, "Helpful and friendly instructors", createdAt.plusHours(2)));
        assertEquals(ReviewStatus.PENDING, review.getStatus());
    }

    @Test
    void editingHiddenReviewResetsStatusToPending() {
        Review review = createReviewWithStatus(ReviewStatus.HIDDEN);
        assertTrue(review.updateContent(4, "Helpful and friendly instructors", createdAt.plusHours(2)));
        assertEquals(ReviewStatus.PENDING, review.getStatus());
    }

    @Test
    void unchangedApprovedReviewContentPreservesStatusAndTimestamp() {
        Review review = createReviewWithStatus(ReviewStatus.APPROVED);
        LocalDateTime before = review.getUpdatedAt();
        assertFalse(review.updateContent(5, "Excellent driving school", createdAt.plusHours(2)));
        assertEquals(ReviewStatus.APPROVED, review.getStatus());
        assertEquals(before, review.getUpdatedAt());
    }

    @Test
    void unchangedHiddenReviewContentPreservesStatusAndTimestamp() {
        Review review = createReviewWithStatus(ReviewStatus.HIDDEN);
        LocalDateTime before = review.getUpdatedAt();
        assertFalse(review.updateContent(5, "Excellent driving school", createdAt.plusHours(2)));
        assertEquals(ReviewStatus.HIDDEN, review.getStatus());
        assertEquals(before, review.getUpdatedAt());
    }
}