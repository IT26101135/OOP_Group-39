/**
============================================================
MEMBER   : IT26101137 - [Name]
MODULE   : Reviews and Reports
FILE     : ReviewVisibilityTest.java
WHAT IT DOES (simple words): Checks feedback permissions and report numbers with temporary data.
REAL-LIFE EXAMPLE: Rehearsing the school manager's checks before a demonstration.
OOP USED : Service interfaces, encapsulation and exception handling
CRUD     : READ report totals; CREATE, READ, UPDATE, DELETE review records
============================================================
*/
package com.drivingschool.review.service;

import com.drivingschool.auth.model.*;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.review.api.ReviewStore;
import com.drivingschool.review.model.*;
import com.drivingschool.student.api.StudentLookup;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewVisibilityTest {
    // The public catalog must not leak pending or hidden feedback.
    @Test
    void publicCatalogContainsApprovedFeedbackOnly() {
        ReviewStore store = mock(ReviewStore.class);
        Review approved = review("R001", "I001", ReviewStatus.APPROVED);
        when(store.readAllStrict()).thenReturn(List.of(approved, review("R002", "I001", ReviewStatus.HIDDEN), review("R003", "I001", ReviewStatus.PENDING)));
        ReviewOperations service = new ReviewService(store, mock(AuthSessionService.class), mock(StudentLookup.class), Clock.systemUTC());
        assertEquals(List.of(approved), service.getApprovedReviews());
    }

    // The ID lookup must apply the instructor-list rule even to another instructor's approved review.
    @Test
    void instructorCannotLookUpAnotherInstructorsFeedback() {
        ReviewStore store = mock(ReviewStore.class);
        AuthSessionService sessions = mock(AuthSessionService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        User instructor = mock(User.class);
        when(instructor.getRole()).thenReturn(Role.INSTRUCTOR);
        when(instructor.getId()).thenReturn("I001");
        when(sessions.requireLogin(request)).thenReturn(instructor);
        Review own = review("R001", "I001", ReviewStatus.HIDDEN);
        Review other = review("R002", "I002", ReviewStatus.APPROVED);
        when(store.readAllStrict()).thenReturn(List.of(own, other));
        when(store.findByIdStrict("R001")).thenReturn(Optional.of(own));
        when(store.findByIdStrict("R002")).thenReturn(Optional.of(other));
        ReviewService service = new ReviewService(store, sessions, mock(StudentLookup.class), Clock.systemUTC());
        assertEquals(List.of(own), service.getVisibleReviews(request));
        assertEquals(Optional.of(own), service.getVisibleReviewById(request, "R001"));
        assertTrue(service.getVisibleReviewById(request, "R002").isEmpty());
    }

    // Builds valid feedback with an explicit instructor link and moderation state.
    private Review review(String id, String instructorId, ReviewStatus status) {
        LocalDateTime time = LocalDateTime.of(2026, 1, 1, 9, 0);
        return new Review(id, "S001", "B001", instructorId, 4, "Helpful driving lesson", status, time, time);
    }
}
