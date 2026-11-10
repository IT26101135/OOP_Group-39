/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : StudentAccountService.java
WHAT IT DOES (simple words): Applies the rules for student account.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
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
// SOLID: S - This class is responsible for application rules.
public class StudentAccountService {

    private final AuthSessionService auth;
    private final UserRepository users;
    private final BookingRepository bookings;
    private final ReviewRepository reviews;
    private final EnrollmentRepository enrollments;

    // Receives the helpers this class needs so each job stays separate.
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

    // Removes the account only when no learning history depends on it.
    // CRUD: DELETE - removes a student account
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
                    "Your account has payment, lesson or review history that must be kept. You can update your profile or contact us for help.");
        }
        users.delete(user.getId());
    }
}
