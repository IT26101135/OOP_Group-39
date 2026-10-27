package com.drivingschool.auth.service;

import com.drivingschool.auth.model.User;
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
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final String dummyPasswordHash;

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

    public Student registerStudent(
            String name,
            String email,
            String phone,
            String rawPassword) {

        return AccountWriteCoordinator.execute(() -> {
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

            userRepository.save(student);
            return student;
        });
    }

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

        String hashToCheck = account
                .map(User::getPasswordHash)
                .orElse(dummyPasswordHash);

        boolean passwordMatches =
                passwordHasher.matches(rawPassword, hashToCheck);

        if (account.isPresent() && passwordMatches) {
            return account;
        }

        return Optional.empty();
    }

    private String normalizeRegistrationEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email is required.");
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

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