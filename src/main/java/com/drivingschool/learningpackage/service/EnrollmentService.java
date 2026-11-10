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
import com.drivingschool.auth.model.AccountStatus;
import com.drivingschool.booking.model.BookingStatus;
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
public class EnrollmentService implements EnrollmentOperations, EnrollmentAdministration {

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
            ensureCanEnroll(student);
            // Step 2: Check the selected course is still offered.
            LearningPackage selected = packages.findByIdStrict(packageId)
                    .orElseThrow(() -> new RecordNotFoundException("Package not found."));
            if (!selected.isActive()) throw new ValidationException("Package is inactive.");
            LocalDateTime enrolledAt = LocalDateTime.now(clock);
            Enrollment enrollment = new Enrollment(enrollments.nextId("E"),
                    student.getId(), selected.getId(), enrolledAt,
                    selected.getPrice(), "UNPAID");
            // Step 3: Save the request only; payment confirmation will unlock lessons and validity.
            enrollments.save(enrollment);
            return enrollment;
        });
    }

    // Keeps one payment request per student, even when two forms arrive together.
    private void ensureCanEnroll(Student student) {
        for (Enrollment enrollment : enrollments.readAllStrict()) {
            if (enrollment.getStudentId().equals(student.getId())
                    && "AWAITING_PAYMENT".equals(enrollment.getStatus())) {
                throw new DuplicateRecordException("You already have an enrollment awaiting payment. "
                        + "Open its payment instructions below before requesting another package.");
            }
        }
        String problem = renewalProblem(student);
        if (problem != null) throw new DuplicateRecordException(problem);
    }

    // A new paid course may replace an expired or used-up course, but not an open lesson.
    private String renewalProblem(Student student) {
        boolean previousCourse = student.getActivePackageId() != null;
        for (Enrollment receipt : enrollments.readAllStrict()) {
            if (student.getId().equals(receipt.getStudentId()) && receipt.isPaid()) previousCourse = true;
        }
        // Keep first-payment handling separate from renewal, including older unpaid requests.
        if (!previousCourse) return null;
        for (Booking booking : bookings.readAllStrict()) {
            if (student.getId().equals(booking.getStudentId())
                    && (booking.getStatus() == BookingStatus.PENDING
                    || booking.getStatus() == BookingStatus.CONFIRMED)) {
                return "Resolve your pending or confirmed lessons before choosing another package. "
                        + "Open My lessons or ask us for help.";
            }
        }
        if (student.getActivePackageId() == null || student.getLessonsRemaining() == 0) return null;
        if (student.getActiveEnrollmentId() != null) {
            Optional<Enrollment> current = enrollments.findByIdStrict(student.getActiveEnrollmentId());
            if (current.isPresent()) {
                Enrollment receipt = current.get();
                if (receipt.isPaid() && receipt.getStudentId().equals(student.getId())
                        && receipt.getPackageId().equals(student.getActivePackageId())
                        && receipt.getValidUntil() != null
                        && receipt.getValidUntil().isBefore(LocalDateTime.now(clock).toLocalDate())) return null;
            }
        }
        return "You still have lessons in your current package. Use them before choosing another package.";
    }

    // Finds one owned receipt for payment instructions without revealing another student's details.
    // CRUD: READ   - finds or lists enrollments
    public Enrollment ownEnrollment(HttpServletRequest request, String enrollmentId) {
        return requireOwnEnrollment(requireStudent(request), enrollmentId);
    }

    // Uses the saved package name even if the school later stops offering it.
    // CRUD: READ   - finds or lists packages
    public String packageName(String packageId) {
        Optional<LearningPackage> selected = packages.findByIdStrict(packageId);
        if (selected.isPresent()) return selected.get().getName();
        return "Package no longer listed";
    }

    // Shows the administrator only requests that still need a payment decision.
    // CRUD: READ   - finds or lists enrollments
    public List<Enrollment> awaitingPayments(HttpServletRequest request) {
        auth.requireAdmin(request);
        List<Enrollment> waiting = new ArrayList<>();
        for (Enrollment enrollment : enrollments.readAllStrict()) {
            if ("AWAITING_PAYMENT".equals(enrollment.getStatus())) waiting.add(enrollment);
        }
        return waiting;
    }

    // Looks up a display name only after checking administrator access.
    // CRUD: READ   - finds or lists student accounts
    public String studentName(HttpServletRequest request, String studentId) {
        auth.requireAdmin(request);
        Optional<User> found = users.findByIdStrict(studentId);
        if (found.isPresent()) return found.get().getName();
        return "Student name unavailable";
    }

    // Confirms payment once; the cashier lock keeps two admins from granting lessons twice.
    // CRUD: UPDATE - changes an existing enrollment
    public Enrollment markPaid(HttpServletRequest request, String enrollmentId) {
        auth.requireAdmin(request);
        return AccountWriteCoordinator.execute(() -> {
            // Step 1: Reload the request inside the lock, then recheck its people and course.
            Enrollment enrollment = requireAwaiting(enrollmentId);
            Student student = paymentStudent(enrollment.getStudentId());
            LearningPackage selected = payablePackage(enrollment.getPackageId());
            // Step 2: Start validity today; the saved request price stays unchanged.
            enrollment.setStatus("PAID");
            enrollment.setValidUntil(LocalDateTime.now(clock).toLocalDate().plusDays(selected.getValidityDays()));
            // Step 3: Prepare the balance first; reads cannot use it until the paid receipt is saved.
            activateLessons(student, enrollment, selected);
            enrollments.update(enrollment);
            return enrollment;
        });
    }

    // Cancels only an unpaid request and retires any old pending lesson requests linked to it.
    // CRUD: UPDATE - changes an existing enrollment
    public Enrollment cancelRequest(HttpServletRequest request, String enrollmentId, String reason) {
        auth.requireAdmin(request);
        return AccountWriteCoordinator.execute(() -> {
            // Step 1: Recheck the state after any earlier administrator has finished.
            Enrollment enrollment = requireAwaiting(enrollmentId);
            enrollment.setCancellationReason(reason);
            enrollment.setStatus("CANCELLED");
            // Step 2: Keep the cancelled receipt and clearly reject its old pending requests.
            enrollments.update(enrollment);
            rejectPendingRequests(enrollment);
            return enrollment;
        });
    }

    // Explains a double click or a competing admin action without changing the handled receipt.
    private Enrollment requireAwaiting(String enrollmentId) {
        Enrollment enrollment = enrollments.findByIdStrict(enrollmentId)
                .orElseThrow(() -> new RecordNotFoundException("Enrollment not found."));
        if (!"AWAITING_PAYMENT".equals(enrollment.getStatus())) {
            throw new ValidationException("This request was already handled (" + enrollment.getStatusLabel()
                    + "). Paid enrollments cannot be cancelled or confirmed again.");
        }
        return enrollment;
    }

    // Refuses to grant lessons to a suspended, pending or already-enrolled student.
    private Student paymentStudent(String studentId) {
        User account = users.findByIdStrict(studentId)
                .orElseThrow(() -> new RecordNotFoundException("Student account not found."));
        if (!(account instanceof Student)) throw new ValidationException("This request needs a student account.");
        if (account.getStatus() == AccountStatus.SUSPENDED) {
            throw new ValidationException("This account is suspended. Review the account before confirming payment.");
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new ValidationException("Approve the student account before confirming payment.");
        }
        Student student = (Student) account;
        String problem = renewalProblem(student);
        if (problem != null) throw new ValidationException(problem);
        return student;
    }

    // Rechecks the course at confirmation time instead of trusting the earlier enrollment form.
    private LearningPackage payablePackage(String packageId) {
        LearningPackage selected = packages.findByIdStrict(packageId)
                .orElseThrow(() -> new RecordNotFoundException("Package not found."));
        if (!selected.isActive()) throw new ValidationException("This package is no longer available. Review the request before confirming payment.");
        return selected;
    }

    // Saves the new lesson tickets; other account and booking writers share the same lock.
    private void activateLessons(Student student, Enrollment enrollment, LearningPackage selected) {
        student.setActivePackageId(selected.getId());
        student.setLessonsRemaining(selected.getLessonCount());
        student.setActiveEnrollmentId(enrollment.getId());
        users.update(student);
    }

    // Clears invalid legacy requests from the queue while keeping their reason in history.
    private void rejectPendingRequests(Enrollment enrollment) {
        for (Booking booking : bookings.readAllStrict()) {
            if (booking.getEnrollmentId().equals(enrollment.getId()) && booking.getStatus() == BookingStatus.PENDING) {
                booking.reject("Enrollment " + enrollment.getReferenceCode()
                        + " was cancelled before payment. See My enrollments.");
                bookings.update(booking);
            }
        }
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
            throw new ValidationException("We could not identify this package request. Return to your packages and try again.");
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
        if ("CANCELLED".equals(enrollment.getStatus())) {
            throw new ValidationException("Cancelled requests are kept in your payment history.");
        }
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
