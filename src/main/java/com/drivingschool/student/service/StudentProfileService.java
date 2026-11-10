/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : StudentProfileService.java
WHAT IT DOES (simple words): Applies the rules for student profile.
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
import java.time.LocalDate;

@Service
// SOLID: S - This class is responsible for application rules.
public class StudentProfileService implements com.drivingschool.student.api.StudentProfiles {
    private final UserRepository userRepository;
    private final AuthSessionService authSessionService;

    // Receives the helpers this class needs so each job stays separate.
    public StudentProfileService(UserRepository userRepository,
                                 AuthSessionService authSessionService) {
        this.userRepository = userRepository;
        this.authSessionService = authSessionService;
    }

    // Returns only the signed-in student details that the page is allowed to show.
    // CRUD: READ   - finds or lists student profiles
    public StudentProfile getOwnProfile(HttpServletRequest request) {
        return toProfile(requireStudent(request));
    }

    // Validates a replacement profile before saving so invalid changes leave old details safe.
    // CRUD: UPDATE - changes an existing student profile
    public StudentProfile updateOwnProfile(HttpServletRequest request,
                                           String name, String email, String phone) {
        Student current = requireStudent(request);
        return updateOwnProfile(request, name, email, phone, current.getNic(),
                current.getDateOfBirth(), current.getAddress(),
                current.getLicenseTypeWanted(), current.getEmergencyContact());
    }

    // Validates a replacement profile before saving so invalid changes leave old details safe.
    // CRUD: UPDATE - changes an existing student profile
    public StudentProfile updateOwnProfile(HttpServletRequest request,
                                           String name, String email, String phone,
                                           String nic, LocalDate dateOfBirth,
                                           String address, String licenseTypeWanted,
                                           String emergencyContact) {
        // The callback runs the complete change while other account writers wait.
        return AccountWriteCoordinator.execute(() -> {
            // Step 1: Reload the signed-in student inside the account lock.
            Student current = requireStudent(request);
            Student replacement = replacementProfile(current, name, email, phone,
                    nic, dateOfBirth, address, licenseTypeWanted, emergencyContact);
            // Step 2: Check all saved emails before changing the filing cabinet.
            List<User> users = userRepository.readAllStrict();
            ensureUniqueStoredEmails(users);
            ensureAvailableEmail(users, current.getId(), replacement.getEmail());
            // Step 3: Leave an unchanged profile alone, otherwise save the validated copy.
            if (unchanged(current, replacement)) return toProfile(current);
            userRepository.update(replacement);
            return toProfile(replacement);
        });
    }

    // Builds a checked copy; account status, password and lesson balances stay intact.
    private Student replacementProfile(Student current, String name, String email,
            String phone, String nic, LocalDate dateOfBirth, String address,
            String licenseTypeWanted, String emergencyContact) {
        Student replacement = new Student(current.getId(), name, email,
                current.getPasswordHash(), phone);
        replacement.setStatus(current.getStatus());
        replacement.setCreatedAt(current.getCreatedAt());
        replacement.setNic(nic);
        replacement.setDateOfBirth(dateOfBirth);
        replacement.setAddress(address);
        replacement.setLicenseTypeWanted(storedLessonType(licenseTypeWanted));
        replacement.setEmergencyContact(emergencyContact);
        replacement.setActivePackageId(current.getActivePackageId());
        replacement.setLessonsRemaining(current.getLessonsRemaining());
        replacement.setActiveEnrollmentId(current.getActiveEnrollmentId());
        return replacement;
    }

    private String storedLessonType(String value) {
        if (value == null) return null;
        return switch (value.trim().toUpperCase(Locale.ROOT)) {
            case "MANUAL", "MANUAL CAR" -> "MANUAL";
            case "AUTOMATIC", "AUTOMATIC CAR" -> "AUTOMATIC";
            case "MOTORCYCLE" -> "MOTORCYCLE";
            case "REFRESHER" -> "REFRESHER";
            default -> value.trim();
        };
    }

    // OOP: EXCEPTION HANDLING - A duplicate email becomes a friendly conflict message.
    // Stops a student from taking the email address of another existing account.
    private void ensureAvailableEmail(List<User> users, String studentId, String email) {
        for (User user : users) {
            if (!user.getId().equals(studentId) && user.getEmail().equalsIgnoreCase(email)) {
                throw new DuplicateRecordException("An account with this email already exists.");
            }
        }
    }

    // Avoids rewriting the file when every editable detail is already the same.
    private boolean unchanged(Student current, Student replacement) {
        return current.getName().equals(replacement.getName())
                    && current.getEmail().equals(replacement.getEmail())
                    && current.getPhone().equals(replacement.getPhone())
                    && java.util.Objects.equals(current.getNic(), replacement.getNic())
                    && java.util.Objects.equals(current.getDateOfBirth(), replacement.getDateOfBirth())
                    && java.util.Objects.equals(current.getAddress(), replacement.getAddress())
                    && java.util.Objects.equals(current.getLicenseTypeWanted(), replacement.getLicenseTypeWanted())
                    && java.util.Objects.equals(current.getEmergencyContact(), replacement.getEmergencyContact());
    }

    // Refuses access unless the current account meets the require student rule.
    private Student requireStudent(HttpServletRequest request) {
        User user = authSessionService.requireLogin(request);
        if (user.getRole() != Role.STUDENT || !(user instanceof Student student)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Student access is required.");
        }
        return student;
    }

    // Stops the operation if ensure unique stored emails rules are not met.
    private void ensureUniqueStoredEmails(List<User> users) {
        Set<String> emails = new HashSet<>();
        for (User user : users) {
            if (!emails.add(user.getEmail().toLowerCase(Locale.ROOT))) {
                throw new StorageException("Account data requires repair.",
                        new IOException("Duplicate account email detected."));
            }
        }
    }

    // Handles to profile for this class without changing another class's job.
    private StudentProfile toProfile(Student student) {
        return new StudentProfile(student.getId(), student.getName(),
                student.getEmail(), student.getPhone(), student.getNic(),
                student.getDateOfBirth(), student.getAddress(),
                student.getLicenseTypeWanted(), student.getEmergencyContact(),
                student.getActivePackageId(), student.getLessonsRemaining());
    }
}
