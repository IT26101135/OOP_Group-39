// Module owner: IT26101135
package com.drivingschool.student.service;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.review.repository.ReviewRepository;
import com.drivingschool.learningpackage.repository.EnrollmentRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class StudentAccountService {

    private final AuthSessionService auth;
    private final UserRepository users;
    private final BookingRepository bookings;
    private final ReviewRepository reviews;
    private final EnrollmentRepository enrollments;

    public StudentAccountService(AuthSessionService auth,
                                 UserRepository users,
                                 BookingRepository bookings,
                                 ReviewRepository reviews,
                                 EnrollmentRepository enrollments) {
        this.auth = auth;
        this.users = users;
        this.bookings = bookings;
        this.reviews = reviews;
        this.enrollments = enrollments;
    }

    public void deleteOwnAccount(HttpServletRequest request) {
        User user = auth.requireLogin(request);
        if (user.getRole() != Role.STUDENT) {
            throw new ValidationException("Only student accounts can be deleted here.");
        }
        boolean hasBookings = bookings.readAllStrict().stream()
                .anyMatch(booking -> booking.getStudentId().equals(user.getId()));
        boolean hasReviews = reviews.readAllStrict().stream()
                .anyMatch(review -> review.getStudentId().equals(user.getId()));
        boolean hasEnrollments = enrollments.readAllStrict().stream()
                .anyMatch(enrollment -> enrollment.getStudentId().equals(user.getId()));
        if (hasBookings || hasReviews || hasEnrollments) {
            throw new ValidationException(
                    "Account cannot be deleted while lesson or review history is attached.");
        }
        users.delete(user.getId());
    }
}
