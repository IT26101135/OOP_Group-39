/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Shared foundation and student accounts
FILE     : StudentAccountCrudTest.java
WHAT IT DOES (simple words): Checks student account CRUD and protected history using temporary files.
REAL-LIFE EXAMPLE: A reception desk starts the school day and helps people find the right page.
OOP USED : Shared setup, inheritance or exception presentation as explained below
CRUD     : Supports account operations; does not add a new storage operation
============================================================
*/
package com.drivingschool.student.service;

import com.drivingschool.auth.model.AccountStatus;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.service.AuthService;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.auth.util.PasswordHasher;
import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.learningpackage.model.Enrollment;
import com.drivingschool.learningpackage.repository.EnrollmentRepository;
import com.drivingschool.review.model.Review;
import com.drivingschool.review.model.ReviewStatus;
import com.drivingschool.review.repository.ReviewRepository;
import com.drivingschool.student.model.Student;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpServletRequest;
import java.nio.file.Path;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class StudentAccountCrudTest {
    @TempDir Path folder;
    private UserRepository users;
    private BookingRepository bookings;
    private ReviewRepository reviews;
    private EnrollmentRepository enrollments;
    private AuthService registration;
    private StudentProfileService profiles;
    private StudentAccountService accounts;
    private MockHttpServletRequest request;
    private Student student;
    private final LocalDateTime lessonTime = LocalDateTime.of(2026, 1, 1, 9, 0);
    private static final String PASSWORD = "TestingPass123!";

    // Creates a separate active account and normal login session for each test.
    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        users = new UserRepository(folder.toString(), mapper);
        bookings = new BookingRepository(folder.toString(), mapper);
        reviews = new ReviewRepository(folder.toString(), mapper);
        enrollments = new EnrollmentRepository(folder.toString(), mapper);
        registration = new AuthService(users, new PasswordHasher());
        AuthSessionService sessions = new AuthSessionService(registration, users);
        student = registration.registerStudent("Learner", "learner@example.test", "0771234567", PASSWORD);
        student.setStatus(AccountStatus.ACTIVE);
        users.update(student);
        request = new MockHttpServletRequest();
        assertTrue(sessions.login(request, student.getEmail(), PASSWORD));
        profiles = new StudentProfileService(users, sessions);
        accounts = new StudentAccountService(sessions, users, bookings, reviews, enrollments);
    }

    // Registration, own-profile reading, editing and unreferenced deletion use the same saved account.
    @Test
    void supportsStudentAccountCrudWithRealFiles() {
        assertEquals("Learner", profiles.getOwnProfile(request).getName());
        profiles.updateOwnProfile(request, "Updated Learner", "updated@example.test", "0777654321");
        UserRepository reopened = new UserRepository(folder.toString(), new ObjectMapper().findAndRegisterModules());
        assertEquals("Updated Learner", reopened.findByIdStrict(student.getId()).orElseThrow().getName());
        assertEquals("updated@example.test", profiles.getOwnProfile(request).getEmail());
        accounts.deleteOwnAccount(request);
        assertTrue(reopened.findByIdStrict(student.getId()).isEmpty());
    }

    // A student's enrollment receipts are retained when account deletion is requested.
    @Test
    void keepsAccountWithEnrollmentHistory() {
        enrollments.save(new Enrollment("E001", student.getId(), "P001", lessonTime, new BigDecimal("1000.00"), "PAID"));
        assertThrows(ValidationException.class, () -> accounts.deleteOwnAccount(request));
        assertTrue(users.findByIdStrict(student.getId()).isPresent());
    }

    // Even a cancelled booking still explains the student's learning history.
    @Test
    void keepsAccountWithBookingHistory() {
        bookings.save(new Booking("B001", student.getId(), "E001", lessonTime, lessonTime.plusHours(1),
                null, null, BookingStatus.CANCELLED, lessonTime.minusDays(1)));
        assertThrows(ValidationException.class, () -> accounts.deleteOwnAccount(request));
        assertTrue(users.findByIdStrict(student.getId()).isPresent());
    }

    // Hidden feedback remains attached history rather than becoming an orphan after deletion.
    @Test
    void keepsAccountWithReviewHistory() {
        reviews.save(new Review("R001", student.getId(), 4, "Helpful driving lessons", ReviewStatus.HIDDEN, lessonTime, lessonTime));
        assertThrows(ValidationException.class, () -> accounts.deleteOwnAccount(request));
        assertTrue(users.findByIdStrict(student.getId()).isPresent());
    }

    // Invalid profile changes must leave the saved name and phone number unchanged.
    @Test
    void rejectsInvalidProfileWithoutChangingSavedAccount() {
        assertThrows(ValidationException.class, () -> profiles.updateOwnProfile(request, "Learner", "learner@example.test", "bad"));
        assertEquals("0771234567", profiles.getOwnProfile(request).getPhone());
    }

    // A second registration with the same email must fail without adding another account.
    @Test
    void duplicateRegistrationUsesCustomException() {
        assertThrows(DuplicateRecordException.class, () -> registration.registerStudent("Another Learner",
                "learner@example.test", "0771234567", PASSWORD));
        assertEquals(1, users.readAllStrict().size());
    }
}
