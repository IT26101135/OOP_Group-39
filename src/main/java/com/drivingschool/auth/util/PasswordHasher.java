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

    public String hash(String rawPassword) {
        validateNewPassword(rawPassword);
        return encoder.encode(rawPassword);
    }

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

        if (exceedsByteLimit(rawPassword)) {
            throw new ValidationException(
                    "Password must not exceed 72 UTF-8 bytes."
            );
        }
    }

    private boolean exceedsByteLimit(String rawPassword) {
        return rawPassword.getBytes(StandardCharsets.UTF_8).length
                > MAX_PASSWORD_BYTES;
    }
}