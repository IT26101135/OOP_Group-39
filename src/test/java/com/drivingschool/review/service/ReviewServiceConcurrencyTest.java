package com.drivingschool.review.service;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.review.model.Review;
import com.drivingschool.review.model.ReviewStatus;
import com.drivingschool.review.repository.ReviewRepository;
import com.drivingschool.student.api.StudentLookup;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewServiceConcurrencyTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void twoServiceInstancesAllowOnlyOneConcurrentSubmission() throws Exception {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        ReviewRepository repositoryOne =
                new ReviewRepository(temporaryDirectory.toString(), mapper);
        ReviewRepository repositoryTwo =
                new ReviewRepository(temporaryDirectory.toString(), mapper);

        AuthSessionService auth = mock(AuthSessionService.class);
        StudentLookup students = mock(StudentLookup.class);

        HttpServletRequest requestOne = mock(HttpServletRequest.class);
        HttpServletRequest requestTwo = mock(HttpServletRequest.class);

        User student = mock(User.class);
        when(student.getId()).thenReturn("S001");
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(auth.requireLogin(any())).thenReturn(student);
        when(students.exists("S001")).thenReturn(true);

        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-10-18T04:30:00Z"),
                ZoneId.of("Asia/Colombo")
        );

        ReviewService serviceOne =
                new ReviewService(repositoryOne, auth, students, fixedClock);
        ReviewService serviceTwo =
                new ReviewService(repositoryTwo, auth, students, fixedClock);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Callable<Boolean> submitOne =
                    () -> submit(serviceOne, requestOne, ready, start);
            Callable<Boolean> submitTwo =
                    () -> submit(serviceTwo, requestTwo, ready, start);

            Future<Boolean> first = executor.submit(submitOne);
            Future<Boolean> second = executor.submit(submitTwo);

            assertTrue(ready.await(5, TimeUnit.SECONDS),
                    "Workers did not become ready");

            start.countDown();

            int successes = (first.get(10, TimeUnit.SECONDS) ? 1 : 0)
                    + (second.get(10, TimeUnit.SECONDS) ? 1 : 0);

            assertEquals(1, successes);
            assertEquals(1, repositoryOne.readAllStrict().size());

        } finally {
            start.countDown();
            executor.shutdownNow();

            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS),
                    "Executor did not terminate");
        }
    }

    @Test
    void concurrentEditingAndApprovalPreserveBothChanges() throws Exception {

        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        ReviewRepository repositoryOne =
                new ReviewRepository(temporaryDirectory.toString(), mapper);

        ReviewRepository repositoryTwo =
                new ReviewRepository(temporaryDirectory.toString(), mapper);

        AuthSessionService auth = mock(AuthSessionService.class);
        StudentLookup students = mock(StudentLookup.class);

        HttpServletRequest studentRequest =
                mock(HttpServletRequest.class);

        HttpServletRequest adminRequest =
                mock(HttpServletRequest.class);

        User student = mock(User.class);
        when(student.getId()).thenReturn("S001");
        when(student.getRole()).thenReturn(Role.STUDENT);

        User admin = mock(User.class);
        when(admin.getId()).thenReturn("A001");
        when(admin.getRole()).thenReturn(Role.ADMIN);

        when(students.exists("S001")).thenReturn(true);

        CountDownLatch updateEntered = new CountDownLatch(1);
        CountDownLatch releaseUpdate = new CountDownLatch(1);
        CountDownLatch adminStarted = new CountDownLatch(1);

        when(auth.requireLogin(studentRequest)).thenAnswer(invocation -> {
            updateEntered.countDown();

            if (!releaseUpdate.await(5, TimeUnit.SECONDS)) {
                throw new TimeoutException("Update was not released");
            }

            return student;
        });

        when(auth.requireAdmin(adminRequest)).thenReturn(admin);

        LocalDateTime createdAt =
                LocalDateTime.of(2026, 10, 18, 9, 0);

        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-10-18T04:30:00Z"),
                ZoneId.of("Asia/Colombo")
        );

        Review originalReview = new Review(
                "R001",
                "S001",
                3,
                "Good driving school",
                ReviewStatus.PENDING,
                createdAt,
                createdAt
        );

        repositoryOne.save(originalReview);

        ReviewService studentService =
                new ReviewService(repositoryOne, auth, students, fixedClock);

        ReviewService adminService =
                new ReviewService(repositoryTwo, auth, students, fixedClock);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Review> updateFuture = executor.submit(() ->
                    studentService.updateOwnReview(
                            studentRequest,
                            "R001",
                            5,
                            "Excellent driving school"
                    )
            );

            assertTrue(updateEntered.await(5, TimeUnit.SECONDS),
                    "Student update did not start");

            Future<Review> approvalFuture = executor.submit(() -> {
                adminStarted.countDown();
                return adminService.approveReview(adminRequest, "R001");
            });

            assertTrue(adminStarted.await(5, TimeUnit.SECONDS),
                    "Admin approval did not start");

            releaseUpdate.countDown();

            updateFuture.get(10, TimeUnit.SECONDS);
            approvalFuture.get(10, TimeUnit.SECONDS);

            Review savedReview = repositoryOne.findByIdStrict("R001")
                    .orElseThrow();

            assertEquals(5, savedReview.getRating());
            assertEquals("Excellent driving school",
                    savedReview.getComment());
            assertEquals(ReviewStatus.APPROVED, savedReview.getStatus());
            assertEquals(createdAt, savedReview.getCreatedAt());

            assertEquals(
                    LocalDateTime.of(2026, 10, 18, 10, 0),
                    savedReview.getUpdatedAt()
            );

        } finally {
            releaseUpdate.countDown();
            executor.shutdownNow();

            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS),
                    "Executor did not terminate");
        }
    }

    private boolean submit(
            ReviewService service,
            HttpServletRequest request,
            CountDownLatch ready,
            CountDownLatch start) throws Exception {

        ready.countDown();

        if (!start.await(5, TimeUnit.SECONDS)) {
            throw new TimeoutException("Start signal was not received");
        }

        try {
            service.createReview(
                    request,
                    5,
                    "Excellent driving school"
            );
            return true;

        } catch (DuplicateRecordException expected) {
            return false;
        }
    }
}