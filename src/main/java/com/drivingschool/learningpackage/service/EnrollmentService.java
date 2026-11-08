/**
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages and enrollment
FILE     : EnrollmentService.java
WHAT IT DOES (simple words): Applies the rules for enrollment.
REAL-LIFE EXAMPLE: A course folder keeps the learning plan and enrollment details together.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.learningpackage.service;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.auth.util.AccountWriteCoordinator;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.learningpackage.model.Enrollment;
import com.drivingschool.learningpackage.model.LearningPackage;
import com.drivingschool.learningpackage.repository.EnrollmentRepository;
import com.drivingschool.learningpackage.repository.LearningPackageRepository;
import com.drivingschool.student.model.Student;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.repository.BookingRepository;

@Service
// SOLID: S - This class is responsible for application rules.
public class EnrollmentService implements EnrollmentOperations {

    private final EnrollmentRepository enrollments;
    private final LearningPackageRepository packages;
    private final UserRepository users;
    private final AuthSessionService auth;
    private final Clock clock;
    private final BookingRepository bookings;

    // Receives the helpers this class needs so each job stays separate.
    public EnrollmentService(EnrollmentRepository enrollments,
                             LearningPackageRepository packages,
                             UserRepository users,
                             AuthSessionService auth,
                             Clock clock, BookingRepository bookings) {
        this.enrollments = enrollments;
        this.packages = packages;
        this.users = users;
        this.auth = auth;
        this.clock = clock;
        this.bookings = bookings;
    }

    // Handles enroll for this class without changing another class's job.
    // CRUD: CREATE - adds a new enrollment and saves it
    public Enrollment enroll(HttpServletRequest request, String packageId) {
        // The shared cashier lock prevents simultaneous changes to the same account.
        return AccountWriteCoordinator.execute(() -> {
            // Step 1: Reload the signed-in student before changing their enrollment.
            Student student = requireStudent(request);
            if (student.getActivePackageId() != null) {
                throw new DuplicateRecordException("You already have an active package.");
            }
            // Step 2: Check the selected course is still offered.
            LearningPackage selected = packages.findByIdStrict(packageId)
                    .orElseThrow(() -> new RecordNotFoundException("Package not found."));
            if (!selected.isActive()) throw new ValidationException("Package is inactive.");
            LocalDateTime enrolledAt = LocalDateTime.now(clock);
            Enrollment enrollment = new Enrollment(enrollments.nextId("E"),
                    student.getId(), selected.getId(), enrolledAt,
                    selected.getPrice(), "PAID");
            enrollment.setValidUntil(enrolledAt.toLocalDate().plusDays(selected.getValidityDays()));
            student.setActivePackageId(selected.getId());
            student.setLessonsRemaining(selected.getLessonCount());
            student.setActiveEnrollmentId(enrollment.getId());
            // Step 3: Save the simulated receipt and the student lesson balance.
            enrollments.save(enrollment);
            users.update(student);
            return enrollment;
        });
    }

    // Handles own enrollments for this class without changing another class's job.
    // CRUD: READ   - finds or lists enrollments
    public List<Enrollment> ownEnrollments(HttpServletRequest request) {
        Student student = requireStudent(request);
        List<Enrollment> ownRecords = new ArrayList<>();
        for (Enrollment enrollment : enrollments.readAllStrict()) {
            if (enrollment.getStudentId().equals(student.getId())) ownRecords.add(enrollment);
        }
        return ownRecords;
    }

    // Handles update learning goal for this class without changing another class's job.
    // CRUD: UPDATE - changes an existing enrollment
    public Enrollment updateLearningGoal(HttpServletRequest request, String enrollmentId,
                                          String learningGoal) {
        // The shared cashier lock prevents simultaneous changes to the same account.
        return AccountWriteCoordinator.execute(() -> {
            // Step 1: Only the owner can change their learning goal.
            Student student = requireStudent(request);
            Enrollment enrollment = requireOwnEnrollment(student, enrollmentId);
            // Step 2: Validate the note; payment and lesson balances never change here.
            enrollment.setLearningGoal(learningGoal);
            enrollments.update(enrollment);
            return enrollment;
        });
    }

    // Handles delete own enrollment for this class without changing another class's job.
    // CRUD: DELETE - removes a enrollment
    public void deleteOwnEnrollment(HttpServletRequest request, String enrollmentId) {
        AccountWriteCoordinator.execute(() -> {
            // Step 1: Check ownership before considering deletion.
            Student student = requireStudent(request);
            Enrollment enrollment = requireOwnEnrollment(student, enrollmentId);
            // Step 2: Keep active learning, paid receipts and all booking history intact.
            ensureDeletable(student, enrollment);
            enrollments.delete(enrollmentId);
            return null;
        });
    }

    // Refuses access unless the current account meets the require own enrollment rule.
    private Enrollment requireOwnEnrollment(Student student, String enrollmentId) {
        if (enrollmentId == null || !enrollmentId.matches("E(?!000)[0-9]{3}")) {
            throw new ValidationException("Enrollment ID must be E001 to E999.");
        }
        Optional<Enrollment> found = enrollments.findByIdStrict(enrollmentId);
        if (found.isEmpty()) throw new RecordNotFoundException("Enrollment not found.");
        Enrollment enrollment = found.get();
        if (!enrollment.getStudentId().equals(student.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This enrollment belongs to another student.");
        }
        return enrollment;
    }

    // Stops the operation if ensure deletable rules are not met.
    // OOP: EXCEPTION HANDLING - ValidationException explains why protected records must stay.
    private void ensureDeletable(Student student, Enrollment enrollment) {
        if (enrollment.getId().equals(student.getActiveEnrollmentId())) {
            throw new ValidationException("Active enrollments cannot be deleted.");
        }
        if (!"UNPAID".equalsIgnoreCase(enrollment.getPaymentStatus())) {
            throw new ValidationException("Paid enrollment receipts must be kept for your records.");
        }
        for (Booking booking : bookings.readAllStrict()) {
            if (enrollment.getId().equals(booking.getEnrollmentId())) {
                throw new ValidationException("Enrollment cannot be deleted while booking history is attached.");
            }
        }
    }

    // Refuses access unless the current account meets the require student rule.
    private Student requireStudent(HttpServletRequest request) {
        User user = auth.requireLogin(request);
        if (user.getRole() != Role.STUDENT || !(user instanceof Student student)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Student access is required.");
        }
        return student;
    }
}
