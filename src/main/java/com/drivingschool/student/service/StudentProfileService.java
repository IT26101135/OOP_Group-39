package com.drivingschool.student.service;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.auth.util.AccountWriteCoordinator;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.StorageException;
import com.drivingschool.student.dto.StudentProfile;
import com.drivingschool.student.model.Student;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class StudentProfileService {
    private final UserRepository userRepository;
    private final AuthSessionService authSessionService;

    public StudentProfileService(UserRepository userRepository,
                                 AuthSessionService authSessionService) {
        this.userRepository = userRepository;
        this.authSessionService = authSessionService;
    }

    public StudentProfile getOwnProfile(HttpServletRequest request) {
        return toProfile(requireStudent(request));
    }

    public StudentProfile updateOwnProfile(HttpServletRequest request,
                                           String name, String email, String phone) {
        return AccountWriteCoordinator.execute(() -> {
            // Reload identity INSIDE the shared account-write operation.
            Student current = requireStudent(request);

            // Validate a replacement before modifying any existing object.
            // Reuse the model's trimming, email normalization and validation.
            Student replacement = new Student(current.getId(), name, email,
                    current.getPasswordHash(), phone);

            List<User> users = userRepository.readAllStrict();
            ensureUniqueStoredEmails(users);
            for (User user : users) {
                if (!user.getId().equals(current.getId())
                        && user.getEmail().equalsIgnoreCase(replacement.getEmail())) {
                    throw new DuplicateRecordException(
                            "An account with this email already exists.");
                }
            }

            // A normalized no-op still performs the strict integrity check.
            if (current.getName().equals(replacement.getName())
                    && current.getEmail().equals(replacement.getEmail())
                    && current.getPhone().equals(replacement.getPhone())) {
                return toProfile(current);
            }

            userRepository.update(replacement);
            return toProfile(replacement);
        });
    }

    private Student requireStudent(HttpServletRequest request) {
        User user = authSessionService.requireLogin(request);
        if (user.getRole() != Role.STUDENT || !(user instanceof Student student)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Student access is required.");
        }
        return student;
    }

    private void ensureUniqueStoredEmails(List<User> users) {
        Set<String> emails = new HashSet<>();
        for (User user : users) {
            if (!emails.add(user.getEmail().toLowerCase(Locale.ROOT))) {
                throw new StorageException("Account data requires repair.",
                        new IOException("Duplicate account email detected."));
            }
        }
    }

    private StudentProfile toProfile(Student student) {
        return new StudentProfile(student.getId(), student.getName(),
                student.getEmail(), student.getPhone());
    }
}
