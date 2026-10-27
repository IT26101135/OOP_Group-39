package com.drivingschool.review.repository;

import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.common.repository.FileRepository;
import com.drivingschool.review.model.Review;
import com.drivingschool.review.model.ReviewStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReviewRepositoryTest {

    private static final Path SPRING_TEMP_DIR;

    static {
        try {
            SPRING_TEMP_DIR = Files.createTempDirectory("review-spring-test-");
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("app.data-directory", () -> SPRING_TEMP_DIR.toString());
    }

    @TempDir
    Path tempDir;

    private ReviewRepository repository;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        repository = new ReviewRepository(
                tempDir.toString(),
                objectMapper
        );
    }

    @Autowired
    private ReviewRepository springRepository;

    @Autowired
    private ObjectMapper springObjectMapper;

    @Test
    void springContextProvidesReviewRepository() {
        assertNotNull(springRepository);
    }

    private Review createReview(
            String id,
            ReviewStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        return new Review(
                id,
                "S001",
                5,
                "Excellent driving school",
                status,
                createdAt,
                updatedAt
        );
    }

    @Test
    void saveAndReloadPreservesAllFieldsAndTimestamps() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 10, 1, 10, 30);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 10, 2, 11, 45);

        Review review = createReview(
                "R001", ReviewStatus.APPROVED, createdAt, updatedAt);

        repository.save(review);

        ReviewRepository reloaded = new ReviewRepository(
                tempDir.toString(), objectMapper);

        Review actual = reloaded.findByIdStrict("R001").orElseThrow();

        assertEquals("R001", actual.getId());
        assertEquals("S001", actual.getStudentId());
        assertEquals(5, actual.getRating());
        assertEquals("Excellent driving school", actual.getComment());
        assertEquals(ReviewStatus.APPROVED, actual.getStatus());
        assertEquals(createdAt, actual.getCreatedAt());
        assertEquals(updatedAt, actual.getUpdatedAt());
    }

    @Test
    void savesOneJsonObjectPerLine() throws IOException {
        LocalDateTime time = LocalDateTime.of(2026, 10, 1, 10, 30);

        repository.save(createReview(
                "R001", ReviewStatus.PENDING, time, time));
        repository.save(createReview(
                "R002", ReviewStatus.HIDDEN, time, time));

        List<String> lines = Files.readAllLines(
                tempDir.resolve("reviews.txt"));

        assertEquals(2, lines.size());
        assertTrue(lines.stream().allMatch(line -> {
            try {
                return objectMapper.readTree(line).isObject();
            } catch (IOException e) {
                return false;
            }
        }));
    }

    @Test
    void updatePersistsChangedReview() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 10, 1, 10, 30);
        Review review = createReview(
                "R001", ReviewStatus.PENDING, createdAt, createdAt);

        repository.save(review);

        LocalDateTime updatedAt = createdAt.plusDays(1);
        review.updateContent(4, "Very good driving school", updatedAt);
        repository.update(review);

        ReviewRepository reloadedRepository =
                new ReviewRepository(tempDir.toString(), objectMapper);

        Review actual = reloadedRepository.findByIdStrict("R001").orElseThrow();

        assertEquals(4, actual.getRating());
        assertEquals("Very good driving school", actual.getComment());
        assertEquals(createdAt, actual.getCreatedAt());
        assertEquals(updatedAt, actual.getUpdatedAt());
    }

    @Test
    void deleteRemovesReview() {
        LocalDateTime time = LocalDateTime.of(2026, 10, 1, 10, 30);
        repository.save(createReview(
                "R001", ReviewStatus.PENDING, time, time));

        repository.delete("R001");

        ReviewRepository reloadedRepository =
                new ReviewRepository(tempDir.toString(), objectMapper);

        assertTrue(reloadedRepository.findByIdStrict("R001").isEmpty());
    }

    @Test
    void updateAndDeleteMissingIdsThrowRecordNotFoundException() {
        LocalDateTime time = LocalDateTime.of(2026, 10, 1, 10, 30);
        Review missing = createReview(
                "R001", ReviewStatus.PENDING, time, time);

        assertThrows(
                RecordNotFoundException.class,
                () -> repository.update(missing));

        assertThrows(
                RecordNotFoundException.class,
                () -> repository.delete("R001"));
    }

    @Test
    void saveRejectsDuplicateId() {
        LocalDateTime time = LocalDateTime.of(2026, 10, 1, 10, 30);
        Review review = createReview(
                "R001", ReviewStatus.PENDING, time, time);

        repository.save(review);

        assertThrows(
                DuplicateRecordException.class,
                () -> repository.save(review));
    }

    @Test
    void nextIdAllocatesPersistentReviewIds() {
        assertEquals("R001", repository.nextId("R"));

        ReviewRepository reloaded = new ReviewRepository(
                tempDir.toString(), objectMapper);

        assertEquals("R002", reloaded.nextId("R"));
    }

    @Test
    void allReviewStatusesCanBeSavedAndReloaded() {
        LocalDateTime time = LocalDateTime.of(2026, 10, 1, 10, 30);

        repository.save(createReview("R001", ReviewStatus.PENDING, time, time));
        repository.save(createReview("R002", ReviewStatus.APPROVED, time, time));
        repository.save(createReview("R003", ReviewStatus.HIDDEN, time, time));

        List<Review> reviews = repository.readAllStrict();

        assertEquals(3, reviews.size());
        assertEquals(ReviewStatus.PENDING, reviews.get(0).getStatus());
        assertEquals(ReviewStatus.APPROVED, reviews.get(1).getStatus());
        assertEquals(ReviewStatus.HIDDEN, reviews.get(2).getStatus());
    }

    @Test
    void deletedReviewIdIsNotReused() {
        LocalDateTime time = LocalDateTime.of(2026, 10, 1, 10, 30);

        String firstId = repository.nextId("R");
        assertEquals("R001", firstId);

        repository.save(createReview(
                firstId, ReviewStatus.PENDING, time, time));

        repository.delete(firstId);

        assertEquals("R002", repository.nextId("R"));
    }

    @Test
    void springConfiguredMapperPersistsReviewInTemporaryDirectory() {
        LocalDateTime createdAt =
                LocalDateTime.of(2026, 10, 1, 10, 30);

        LocalDateTime updatedAt = createdAt.plusDays(1);

        Review review = createReview(
                "R100",
                ReviewStatus.APPROVED,
                createdAt,
                updatedAt
        );

        ReviewRepository testRepository = new ReviewRepository(
                tempDir.toString(),
                springObjectMapper
        );

        testRepository.save(review);

        String jsonLine;
        try {
            jsonLine = Files.readAllLines(
                    tempDir.resolve("reviews.txt")).get(0);
        } catch (IOException e) {
            throw new AssertionError(e);
        }

        JsonNode json;
        try {
            json = objectMapper.readTree(jsonLine);
        } catch (IOException e) {
            throw new AssertionError(e);
        }

        assertTrue(json.get("createdAt").isTextual());
        assertTrue(json.get("updatedAt").isTextual());

        assertEquals(createdAt,
                LocalDateTime.parse(json.get("createdAt").asText()));
        assertEquals(updatedAt,
                LocalDateTime.parse(json.get("updatedAt").asText()));

        Review actual = testRepository
                .findByIdStrict("R100")
                .orElseThrow();

        assertEquals(ReviewStatus.APPROVED, actual.getStatus());
        assertEquals(createdAt, actual.getCreatedAt());
        assertEquals(updatedAt, actual.getUpdatedAt());
    }
}