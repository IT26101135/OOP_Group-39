/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : AdminSeedService.java
WHAT IT DOES (simple words): Applies the rules for admin seed.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.auth.service;

import com.drivingschool.auth.model.Admin;
import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.util.AccountWriteCoordinator;
import com.drivingschool.auth.util.PasswordHasher;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
// SOLID: S - This class is responsible for application rules.
public class AdminSeedService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    // Receives the helpers this class needs so each job stays separate.
    public AdminSeedService(
            UserRepository userRepository,
            PasswordHasher passwordHasher) {

        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    /**
     * Returns true when an administrator is created.
     * Returns false when an administrator already exists.
     */
    // Handles seed if needed for this class without changing another class's job.
    public boolean seedIfNeeded(
            String name,
            String email,
            String rawPassword) {

        return AccountWriteCoordinator.execute(() -> {
            List<User> users = userRepository.readAllStrict();

            boolean adminExists = users.stream()
                    .anyMatch(user -> user.getRole() == Role.ADMIN);

            if (adminExists) {
                return false;
            }

            if (email == null || email.isBlank()) {
                throw new ValidationException(
                        "Administrator seed email is required."
                );
            }

            String normalizedEmail =
                    email.trim().toLowerCase(Locale.ROOT);

            boolean emailExists = users.stream()
                    .anyMatch(user -> user.getEmail()
                            .equalsIgnoreCase(normalizedEmail));

            if (emailExists) {
                throw new DuplicateRecordException(
                        "Administrator seed email is already in use."
                );
            }

            String passwordHash = passwordHasher.hash(rawPassword);
            String id = userRepository.nextId("A");

            Admin admin = new Admin(
                    id,
                    name,
                    normalizedEmail,
                    passwordHash
            );

            userRepository.save(admin);
            return true;
        });
    }
}