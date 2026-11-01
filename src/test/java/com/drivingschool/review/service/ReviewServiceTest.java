package com.drivingschool.review.service;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.review.model.Review;
import com.drivingschool.review.model.ReviewStatus;
import com.drivingschool.review.repository.ReviewRepository;
import com.drivingschool.student.api.StudentLookup;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private AuthSessionService authSessionService;

    @Mock
    private StudentLookup studentLookup;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ReviewService reviewService;

    private User student;
    private User admin;

    @BeforeEach
    void setUp() {
        student = mock(User.class);

        lenient().when(student.getId())
                .thenReturn("S001");

        lenient().when(student.getRole())
                .thenReturn(Role.STUDENT);

        admin = mock(User.class);

        lenient().when(admin.getId())
                .thenReturn("A001");

        lenient().when(admin.getRole())
                .thenReturn(Role.ADMIN);
    }

    // 1. Create Review

    @Test
    void createReviewSuccessfully() {

        when(authSessionService.requireLogin(request))
                .thenReturn(student);

        when(studentLookup.exists("S001"))
                .thenReturn(true);

        when(reviewRepository.readAllStrict())
                .thenReturn(List.of());

        when(reviewRepository.nextId("R"))
                .thenReturn("R001");

        Review result = reviewService.createReview(
                request, 5, "Very good driving school"
        );

        assertNotNull(result);
        assertEquals("R001", result.getId());
        assertEquals("S001", result.getStudentId());
        assertEquals(5, result.getRating());
        assertEquals(
                ReviewStatus.PENDING,
                result.getStatus()
        );

        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createReviewWhenStudentAlreadyReviewed() {

        when(authSessionService.requireLogin(request))
                .thenReturn(student);

        when(studentLookup.exists("S001"))
                .thenReturn(true);

        Review existingReview = new Review(
                "R001",
                "S001",
                4,
                "Good driving school",
                ReviewStatus.APPROVED,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(reviewRepository.readAllStrict())
                .thenReturn(List.of(existingReview));

        assertThrows(
                DuplicateRecordException.class,
                () -> reviewService.createReview(
                        request, 5, "Another review comment"
                )
        );

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void adminCannotCreateReview() {

        when(authSessionService.requireLogin(request))
                .thenReturn(admin);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reviewService.createReview(
                        request, 5, "Very good driving school"
                )
        );

        assertEquals(
                HttpStatus.FORBIDDEN,
                exception.getStatusCode()
        );
    }

    // 2. Get Visible Reviews

    @Test
    void adminCanViewAllReviews() {

        when(authSessionService.requireLogin(request))
                .thenReturn(admin);

        Review review1 = new Review(
                "R001", "S001", 5,
                "Excellent driving school",
                ReviewStatus.APPROVED,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Review review2 = new Review(
                "R002", "S002", 4,
                "Good experience here",
                ReviewStatus.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(reviewRepository.readAllStrict())
                .thenReturn(List.of(review1, review2));

        List<Review> result =
                reviewService.getVisibleReviews(request);

        assertEquals(2, result.size());
    }

    @Test
    void studentCanViewApprovedReviewsAndOwnReviews() {

        when(authSessionService.requireLogin(request))
                .thenReturn(student);

        Review approvedReview = new Review(
                "R001", "S002", 5,
                "Excellent driving school",
                ReviewStatus.APPROVED,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Review ownPendingReview = new Review(
                "R002", "S001", 4,
                "Good experience here",
                ReviewStatus.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Review hiddenOtherReview = new Review(
                "R003", "S003", 2,
                "Not a good experience",
                ReviewStatus.HIDDEN,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(reviewRepository.readAllStrict())
                .thenReturn(List.of(
                        approvedReview,
                        ownPendingReview,
                        hiddenOtherReview
                ));

        List<Review> result =
                reviewService.getVisibleReviews(request);

        assertEquals(2, result.size());
        assertTrue(result.contains(approvedReview));
        assertTrue(result.contains(ownPendingReview));
        assertFalse(result.contains(hiddenOtherReview));
    }

    // 3. Get Visible Review By ID

    @Test
    void getVisibleReviewByIdSuccessfully() {

        when(authSessionService.requireLogin(request))
                .thenReturn(student);

        Review review = new Review(
                "R001", "S002", 5,
                "Excellent driving school",
                ReviewStatus.APPROVED,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(reviewRepository.findByIdStrict("R001"))
                .thenReturn(Optional.of(review));

        Optional<Review> result =
                reviewService.getVisibleReviewById(
                        request, "R001"
                );

        assertTrue(result.isPresent());
        assertEquals("R001", result.get().getId());
    }

    @Test
    void studentCannotViewAnotherStudentsHiddenReview() {

        when(authSessionService.requireLogin(request))
                .thenReturn(student);

        Review review = new Review(
                "R001", "S002", 2,
                "Not a good experience",
                ReviewStatus.HIDDEN,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(reviewRepository.findByIdStrict("R001"))
                .thenReturn(Optional.of(review));

        Optional<Review> result =
                reviewService.getVisibleReviewById(
                        request, "R001"
                );

        assertTrue(result.isEmpty());
    }

    // 4. Update Own Review

    @Test
    void studentCanUpdateOwnReview() {

        when(authSessionService.requireLogin(request))
                .thenReturn(student);

        when(studentLookup.exists("S001"))
                .thenReturn(true);

        Review review = new Review(
                "R001", "S001", 3,
                "Good driving school",
                ReviewStatus.APPROVED,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );

        when(reviewRepository.findByIdStrict("R001"))
                .thenReturn(Optional.of(review));

        Review result = reviewService.updateOwnReview(
                request,
                "R001",
                5,
                "Excellent driving school"
        );

        assertEquals(5, result.getRating());
        assertEquals(
                "Excellent driving school",
                result.getComment()
        );
        assertEquals(
                ReviewStatus.PENDING,
                result.getStatus()
        );

        verify(reviewRepository).update(review);
    }

    // 5. Delete Own Review

    @Test
    void studentCanDeleteOwnReview() {

        when(authSessionService.requireLogin(request))
                .thenReturn(student);

        when(studentLookup.exists("S001"))
                .thenReturn(true);

        Review review = new Review(
                "R001", "S001", 4,
                "Good driving school",
                ReviewStatus.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(reviewRepository.findByIdStrict("R001"))
                .thenReturn(Optional.of(review));

        assertDoesNotThrow(
                () -> reviewService.deleteOwnReview(
                        request, "R001"
                )
        );

        verify(reviewRepository).delete("R001");
    }

    // 6. Approve Review

    @Test
    void adminCanApproveReview() {

        Review review = new Review(
                "R001", "S001", 4,
                "Good driving school",
                ReviewStatus.PENDING,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );

        when(reviewRepository.findByIdStrict("R001"))
                .thenReturn(Optional.of(review));

        Review result = reviewService.approveReview(
                request, "R001"
        );

        assertEquals(
                ReviewStatus.APPROVED,
                result.getStatus()
        );

        verify(authSessionService).requireAdmin(request);
        verify(reviewRepository).update(review);
    }

    // 7. Hide Review

    @Test
    void adminCanHideReview() {

        Review review = new Review(
                "R001", "S001", 4,
                "Good driving school",
                ReviewStatus.PENDING,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );

        when(reviewRepository.findByIdStrict("R001"))
                .thenReturn(Optional.of(review));

        Review result = reviewService.hideReview(
                request, "R001"
        );

        assertEquals(
                ReviewStatus.HIDDEN,
                result.getStatus()
        );

        verify(authSessionService).requireAdmin(request);
        verify(reviewRepository).update(review);
    }

}