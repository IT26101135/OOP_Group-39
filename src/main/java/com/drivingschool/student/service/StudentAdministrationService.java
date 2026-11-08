// Module owner: IT26101135
package com.drivingschool.student.service;

import com.drivingschool.auth.model.AccountStatus;
import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.auth.util.AccountWriteCoordinator;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.review.repository.ReviewRepository;
import com.drivingschool.learningpackage.repository.EnrollmentRepository;
import com.drivingschool.student.model.Student;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class StudentAdministrationService {

    private final UserRepository users;
    private final BookingRepository bookings;
    private final ReviewRepository reviews;
    private final AuthSessionService auth;
    private final EnrollmentRepository enrollments;

    public StudentAdministrationService(UserRepository users,
                                        BookingRepository bookings,
                                        ReviewRepository reviews,
                                        EnrollmentRepository enrollments,
                                        AuthSessionService auth) {
        this.users = users;
        this.bookings = bookings;
        this.reviews = reviews;
        this.enrollments = enrollments;
        this.auth = auth;
    }

    public List<Student> search(HttpServletRequest request, String query) {
        auth.requireAdmin(request);
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return users.readAllStrict().stream()
                .filter(Student.class::isInstance)
                .map(Student.class::cast)
                .filter(student -> normalized.isEmpty()
                        || student.getId().toLowerCase(Locale.ROOT).contains(normalized)
                        || student.getName().toLowerCase(Locale.ROOT).contains(normalized)
                        || student.getEmail().toLowerCase(Locale.ROOT).contains(normalized))
                .toList();
    }

    public void setStatus(HttpServletRequest request, String id, AccountStatus status) {
        auth.requireAdmin(request);
        if (status == null) throw new ValidationException("Account status is required.");
        AccountWriteCoordinator.execute(() -> {
            Student student = findStudent(id);
            student.setStatus(status);
            users.update(student);
            return null;
        });
    }

    public void update(HttpServletRequest request, String id, String name,
                       String email, String phone) {
        auth.requireAdmin(request);
        AccountWriteCoordinator.execute(() -> {
            Student current = findStudent(id);
            Student replacement = new Student(id, name, email,
                    current.getPasswordHash(), phone);
            replacement.setStatus(current.getStatus());
            replacement.setCreatedAt(current.getCreatedAt());
            replacement.setNic(current.getNic());
            replacement.setDateOfBirth(current.getDateOfBirth());
            replacement.setAddress(current.getAddress());
            replacement.setLicenseTypeWanted(current.getLicenseTypeWanted());
            replacement.setEmergencyContact(current.getEmergencyContact());
            replacement.setActivePackageId(current.getActivePackageId());
            replacement.setLessonsRemaining(current.getLessonsRemaining());
            replacement.setActiveEnrollmentId(current.getActiveEnrollmentId());
            boolean duplicate = users.readAllStrict().stream()
                    .filter(user -> !user.getId().equals(id))
                    .anyMatch(user -> user.getEmail().equalsIgnoreCase(replacement.getEmail()));
            if (duplicate) throw new DuplicateRecordException("An account with this email already exists.");
            users.update(replacement);
            return null;
        });
    }

    public void delete(HttpServletRequest request, String id) {
        auth.requireAdmin(request);
        AccountWriteCoordinator.execute(() -> {
            findStudent(id);
            boolean hasBookings = bookings.readAllStrict().stream()
                    .anyMatch(booking -> booking.getStudentId().equals(id));
            boolean hasReviews = reviews.readAllStrict().stream()
                    .anyMatch(review -> review.getStudentId().equals(id));
            boolean hasEnrollments = enrollments.readAllStrict().stream()
                    .anyMatch(enrollment -> enrollment.getStudentId().equals(id));
            if (hasBookings || hasReviews || hasEnrollments) {
                throw new ValidationException(
                        "Student cannot be deleted while lesson or review history is attached.");
            }
            users.delete(id);
            return null;
        });
    }

    private Student findStudent(String id) {
        User user = users.findByIdStrict(id)
                .orElseThrow(() -> new RecordNotFoundException("Student not found."));
        if (!(user instanceof Student student) || user.getRole() != Role.STUDENT) {
            throw new ValidationException("The selected account is not a student.");
        }
        return student;
    }
}
