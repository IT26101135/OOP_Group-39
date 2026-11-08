// Module owner: IT26101135
package com.drivingschool.auth.service;

import com.drivingschool.auth.model.User;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.util.AccountWriteCoordinator;
import com.drivingschool.auth.util.PasswordHasher;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.common.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasswordManagementService {

    private record ResetToken(String value, Instant expiresAt) { }

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final AuthSessionService auth;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, ResetToken> resetTokens = new ConcurrentHashMap<>();

    public PasswordManagementService(UserRepository users,
                                     PasswordHasher hasher,
                                     AuthSessionService auth,
                                     Clock clock) {
        this.users = users;
        this.hasher = hasher;
        this.auth = auth;
        this.clock = clock;
    }

    public void changeOwnPassword(HttpServletRequest request, String currentPassword,
                                  String newPassword) {
        User user = auth.requireLogin(request);
        if (!hasher.matches(currentPassword, user.getPasswordHash())) {
            throw new ValidationException("Current password is incorrect.");
        }
        user.setPasswordHash(hasher.hash(newPassword));
        AccountWriteCoordinator.execute(() -> {
            users.update(user);
            return null;
        });
    }

    /** Local demo flow: returns a one-time code instead of sending email. */
    public String createResetToken(String email) {
        if (email == null || email.isBlank()) return null;
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        boolean known = users.readAllStrict().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(normalized));
        if (!known) return null;
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        resetTokens.put(normalized, new ResetToken(token,
                Instant.now(clock).plus(Duration.ofMinutes(20))));
        return token;
    }

    public void resetPassword(String email, String token, String newPassword) {
        if (email == null || token == null) {
            throw new ValidationException("A valid reset code is required.");
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        ResetToken saved = resetTokens.get(normalized);
        if (saved == null || !saved.expiresAt().isAfter(Instant.now(clock))
                || !java.security.MessageDigest.isEqual(
                saved.value().getBytes(java.nio.charset.StandardCharsets.UTF_8),
                token.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            throw new ValidationException("Reset code is invalid or expired.");
        }
        AccountWriteCoordinator.execute(() -> {
            User user = users.readAllStrict().stream()
                    .filter(candidate -> candidate.getEmail().equalsIgnoreCase(normalized))
                    .findFirst()
                    .orElseThrow(() -> new RecordNotFoundException("Account not found."));
            user.setPasswordHash(hasher.hash(newPassword));
            users.update(user);
            resetTokens.remove(normalized, saved);
            return null;
        });
    }
}
