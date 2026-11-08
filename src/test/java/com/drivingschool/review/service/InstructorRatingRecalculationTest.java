// Module owner: IT26101137
package com.drivingschool.review.service;

import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.instructor.model.Instructor;
import com.drivingschool.instructor.repository.InstructorRepository;
import com.drivingschool.review.api.ReviewStore;
import com.drivingschool.review.model.*;
import com.drivingschool.student.api.StudentLookup;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InstructorRatingRecalculationTest {
    @Test void approveHideAndDeleteRecalculateFromApprovedOnly() {
        ReviewStore store = mock(ReviewStore.class);
        List<Review> rows = new ArrayList<>();
        LocalDateTime old = LocalDateTime.parse("2029-01-01T10:00");
        Review approved = new Review("R001", "S001", "B001", "I001", 3, "Approved sample review.", ReviewStatus.APPROVED, old, old);
        Review pending = new Review("R002", "S002", "B002", "I001", 5, "Pending sample review.", ReviewStatus.PENDING, old, old);
        Review hidden = new Review("R003", "S003", "B003", "I001", 1, "Hidden sample review.", ReviewStatus.HIDDEN, old, old);
        rows.addAll(List.of(approved, pending, hidden));
        when(store.findByIdStrict(anyString())).thenAnswer(inv -> rows.stream().filter(r -> r.getId().equals(inv.getArgument(0))).findFirst());
        when(store.readAllStrict()).thenAnswer(inv -> List.copyOf(rows));
        doAnswer(inv -> { rows.removeIf(r -> r.getId().equals(inv.getArgument(0))); return null; }).when(store).delete(anyString());
        AuthSessionService auth = mock(AuthSessionService.class);
        InstructorRepository instructors = mock(InstructorRepository.class);
        Instructor profile = new Instructor("I001", "Teacher One", "teacher@example.test", "0771234567", 10, true);
        when(instructors.findByIdStrict("I001")).thenReturn(Optional.of(profile));
        ReviewService service = new ReviewService(store, auth, mock(StudentLookup.class),
                Clock.fixed(Instant.parse("2030-01-01T08:00:00Z"), ZoneOffset.UTC));
        ReflectionTestUtils.setField(service, "instructors", instructors);
        HttpServletRequest request = mock(HttpServletRequest.class);

        service.approveReview(request, "R002");
        assertEquals(4.0, profile.getAverageRating());
        service.hideReview(request, "R001");
        assertEquals(5.0, profile.getAverageRating());
        service.deleteReview(request, "R002");
        assertEquals(0.0, profile.getAverageRating());
        verify(instructors, times(3)).update(profile);
    }
}
