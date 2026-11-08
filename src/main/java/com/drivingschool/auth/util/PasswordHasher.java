/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : PasswordHasher.java
WHAT IT DOES (simple words): Supports password hasher for the account or course workflow.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.auth.util;

import com.drivingschool.common.exception.ValidationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public final class PasswordHasher {

    private static final int MIN_PASSWORD_CHARACTERS = 8;
    private static final int MAX_PASSWORD_BYTES = 72;
    private static final int BCRYPT_STRENGTH = 12;

    private final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder(BCRYPT_STRENGTH);

    // Handles hash for this class without changing another class's job.
    public String hash(String rawPassword) {
        validateNewPassword(rawPassword);
        return encoder.encode(rawPassword);
    }

    // Handles matches for this class without changing another class's job.
    public boolean matches(String rawPassword, String storedHash) {
        if (rawPassword == null
                || rawPassword.isBlank()
                || exceedsByteLimit(rawPassword)
                || storedHash == null
                || storedHash.isBlank()) {
            return false;
        }

        try {
            return encoder.matches(rawPassword, storedHash);
        } catch (IllegalArgumentException exception) {
            // A malformed hash must never authenticate a user.
            return false;
        }
    }

    // Stops the operation if validate new password rules are not met.
    private void validateNewPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new ValidationException("Password is required.");
        }

        int characterCount = rawPassword.codePointCount(
                0, rawPassword.length()
        );

        if (characterCount < MIN_PASSWORD_CHARACTERS) {
            throw new ValidationException(
                    "Password must contain at least 8 characters."
            );
        }

        if (!rawPassword.matches("(?s).*\\p{L}.*")
                || !rawPassword.matches("(?s).*\\d.*")) {
            throw new ValidationException(
                    "Password must contain at least one letter and one number.");
        }

        if (exceedsByteLimit(rawPassword)) {
            throw new ValidationException(
                    "Password must not exceed 72 UTF-8 bytes."
            );
        }
    }

    // Handles exceeds byte limit for this class without changing another class's job.
    private boolean exceedsByteLimit(String rawPassword) {
        return rawPassword.getBytes(StandardCharsets.UTF_8).length
                > MAX_PASSWORD_BYTES;
    }
}
