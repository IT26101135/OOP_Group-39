/**
 * ============================================================
 * MEMBER   : IT26101134 - [Name]
 * MODULE   : Bookings and scheduling
 * FILE     : PaidEnrollmentGuard.java
 * WHAT IT DOES (simple words): Checks the saved receipt before lessons can be promised.
 * REAL-LIFE EXAMPLE: Reception checks paid lesson tickets before reserving a driving slot.
 * OOP USED : Encapsulation and exception handling
 * CRUD     : READ enrollment, student and package records
 * ============================================================
 */
package com.drivingschool.booking.service;

import com.drivingschool.auth.model.AccountStatus;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.learningpackage.model.Enrollment;
import com.drivingschool.learningpackage.model.LearningPackage;
import com.drivingschool.learningpackage.repository.EnrollmentRepository;
import com.drivingschool.learningpackage.repository.LearningPackageRepository;
import com.drivingschool.student.model.Student;
import org.springframework.stereotype.Service;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

@Service
// SOLID: S - One helper explains whether a saved payment permits booking.
public class PaidEnrollmentGuard {
    private final EnrollmentRepository enrollments;
    private final UserRepository users;
    private final LearningPackageRepository packages;
    private final Clock clock;
    private static final String REQUIRED = "A Paid enrollment with lessons remaining is required. See My enrollments.";

    // Receives the existing filing drawers and school clock.
    public PaidEnrollmentGuard(EnrollmentRepository enrollments, UserRepository users,
                               LearningPackageRepository packages, Clock clock) {
        this.enrollments = enrollments;
        this.users = users;
        this.packages = packages;
        this.clock = clock;
    }

    // OOP: EXCEPTION HANDLING - A plain validation message gives the student a useful next step.
    // Refuses a booking write when the saved receipt cannot supply paid lessons.
    // CRUD: READ   - finds or lists enrollments
    public void requirePaid(String studentId, String enrollmentId) {
        String problem = problemFor(studentId, enrollmentId);
        if (problem != null) throw new ValidationException(problem);
    }

    // Gives queue processing a reason it can save rather than crashing on an invalid request.
    // CRUD: READ   - finds or lists enrollments
    public String problemFor(String studentId, String enrollmentId) {
        // Step 1: Read the receipt itself, rather than trusting an old account balance or form.
        if (enrollmentId == null) return REQUIRED;
        Optional<Enrollment> found = enrollments.findByIdStrict(enrollmentId);
        if (found.isEmpty()) return REQUIRED;
        Enrollment enrollment = found.get();
        if (!enrollment.getStudentId().equals(studentId)) return REQUIRED;
        if (!enrollment.isPaid()) return "Enrollment " + enrollment.getReferenceCode() + " is "
                + enrollment.getStatusLabel() + ". Only Paid enrollments can book lessons. See My enrollments.";
        // Step 2: Recheck the owner, their remaining tickets and the package's current availability.
        Optional<User> account = users.findByIdStrict(studentId);
        if (account.isEmpty() || !(account.get() instanceof Student)) return REQUIRED;
        Student student = (Student) account.get();
        if (student.getStatus() != AccountStatus.ACTIVE) return "The student account must be Active. See My enrollments.";
        if (!matchesBalance(student, enrollment)) return REQUIRED;
        return packageProblem(enrollment);
    }

    // Makes sure the lesson tickets belong to this exact paid receipt and course.
    private boolean matchesBalance(Student student, Enrollment enrollment) {
        return student.getLessonsRemaining() > 0 && enrollment.getId().equals(student.getActiveEnrollmentId())
                && enrollment.getPackageId().equals(student.getActivePackageId());
    }

    // Old paid receipts may have no expiry; new receipts must still be within their validity.
    private String packageProblem(Enrollment enrollment) {
        Optional<LearningPackage> selected = packages.findByIdStrict(enrollment.getPackageId());
        if (selected.isEmpty() || !selected.get().isActive()) return "The learning package is inactive. See My enrollments.";
        if (enrollment.getValidUntil() != null && enrollment.getValidUntil().isBefore(LocalDate.now(clock))) {
            return "The paid enrollment has expired. See My enrollments.";
        }
        return null;
    }
}
