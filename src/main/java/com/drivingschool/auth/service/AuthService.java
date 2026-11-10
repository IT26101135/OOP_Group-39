/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : AuthService.java
WHAT IT DOES (simple words): Applies the rules for auth.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.auth.service;

import com.drivingschool.auth.model.User;
import com.drivingschool.auth.model.AccountStatus;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.util.PasswordHasher;
import com.drivingschool.auth.util.AccountWriteCoordinator;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.StorageException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.student.model.Student;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
// SOLID: S - This class is responsible for application rules.
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final String dummyPasswordHash;

    // Receives the helpers this class needs so each job stays separate.
    public AuthService(
            UserRepository userRepository,
            PasswordHasher passwordHasher) {

        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;

        // Used for password checking when the email does not exist.
        // This reduces the obvious timing difference between an unknown
        // email and an existing account with an incorrect password.
        this.dummyPasswordHash =
                passwordHasher.hash(UUID.randomUUID().toString());
    }

    // Checks the new student details and lets them sign in straight away.
    // CRUD: CREATE - adds a new student account and saves it
    public Student registerStudent(
            String name,
            String email,
            String phone,
            String rawPassword) {

        return AccountWriteCoordinator.execute(() -> {
            // Step 1: Check the email is available before preparing the account.
            String normalizedEmail = normalizeRegistrationEmail(email);

            List<User> users = userRepository.readAllStrict();

            if (findUniqueEmail(users, normalizedEmail).isPresent()) {
                throw new DuplicateRecordException(
                        "An account with this email already exists."
                );
            }

            String passwordHash = passwordHasher.hash(rawPassword);
            String id = userRepository.nextId("S");

            Student student = new Student(
                    id,
                    name,
                    normalizedEmail,
                    passwordHash,
                    phone
            );
            // Step 2: Student registration is active; admin-controlled statuses still exist.
            student.setStatus(AccountStatus.ACTIVE);

            // Step 3: Save the account with its hashed password.
            userRepository.save(student);
            return student;
        });
    }

    // Checks the saved password hash without revealing whether an email is registered.
    // CRUD: READ   - finds or lists student accounts
    public Optional<User> authenticate(
            String email,
            String rawPassword) {

        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        // Do not convert storage failures into "incorrect credentials".
        List<User> users = userRepository.readAllStrict();

        Optional<User> account =
                findUniqueEmail(users, normalizedEmail);

        String hashToCheck = dummyPasswordHash;
        if (account.isPresent()) {
            hashToCheck = account.get().getPasswordHash();
        }

        boolean passwordMatches =
                passwordHasher.matches(rawPassword, hashToCheck);

        if (account.isPresent() && passwordMatches) {
            return account;
        }

        return Optional.empty();
    }

    // Handles normalize registration email for this class without changing another class's job.
    private String normalizeRegistrationEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email is required.");
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    // Finds one email match and reports damaged storage if two accounts share it.
    private Optional<User> findUniqueEmail(
            List<User> users,
            String normalizedEmail) {

        User match = null;

        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(normalizedEmail)) {
                if (match != null) {
                    throw new StorageException(
                            "Account data requires repair.",
                            new IOException(
                                    "Duplicate account email detected."
                            )
                    );
                }

                match = user;
            }
        }

        return Optional.ofNullable(match);
    }
}
