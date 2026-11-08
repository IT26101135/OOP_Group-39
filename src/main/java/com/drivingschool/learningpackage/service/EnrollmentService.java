// Module owner: IT26101136
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

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollments;
    private final LearningPackageRepository packages;
    private final UserRepository users;
    private final AuthSessionService auth;
    private final Clock clock;

    public EnrollmentService(EnrollmentRepository enrollments,
                             LearningPackageRepository packages,
                             UserRepository users,
                             AuthSessionService auth,
                             Clock clock) {
        this.enrollments = enrollments;
        this.packages = packages;
        this.users = users;
        this.auth = auth;
        this.clock = clock;
    }

    public Enrollment enroll(HttpServletRequest request, String packageId) {
        return AccountWriteCoordinator.execute(() -> {
            Student student = requireStudent(request);
            if (student.getActivePackageId() != null) {
                throw new DuplicateRecordException("You already have an active package.");
            }
            LearningPackage selected = packages.findByIdStrict(packageId)
                    .orElseThrow(() -> new RecordNotFoundException("Package not found."));
            if (!selected.isActive()) throw new ValidationException("Package is inactive.");
            LocalDateTime enrolledAt = LocalDateTime.now(clock);
            Enrollment enrollment = new Enrollment(enrollments.nextId("E"),
                    student.getId(), selected.getId(), enrolledAt,
                    selected.getPrice(), "PAID");
            student.setActivePackageId(selected.getId());
            student.setLessonsRemaining(selected.getLessonCount());
            student.setActiveEnrollmentId(enrollment.getId());
            enrollments.save(enrollment);
            users.update(student);
            return enrollment;
        });
    }

    public List<Enrollment> ownEnrollments(HttpServletRequest request) {
        Student student = requireStudent(request);
        return enrollments.readAllStrict().stream()
                .filter(enrollment -> enrollment.getStudentId().equals(student.getId()))
                .toList();
    }

    private Student requireStudent(HttpServletRequest request) {
        User user = auth.requireLogin(request);
        if (user.getRole() != Role.STUDENT || !(user instanceof Student student)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Student access is required.");
        }
        return student;
    }
}
