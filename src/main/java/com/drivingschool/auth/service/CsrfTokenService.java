package com.drivingschool.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class CsrfTokenService {

    public static final String PARAMETER_NAME = "_csrf";

    private static final String SESSION_ATTRIBUTE =
            "drivespark.csrf.token";

    private static final int TOKEN_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Gets the session's token, creating an anonymous session and token
     * when a form needs them.
     */
    public String getOrCreateToken(HttpServletRequest request) {
        HttpSession session = request.getSession(true);

        synchronized (session) {
            Object existing = session.getAttribute(SESSION_ATTRIBUTE);

            if (existing instanceof String token && hasValidFormat(token)) {
                return token;
            }

            byte[] randomBytes = new byte[TOKEN_BYTES];
            secureRandom.nextBytes(randomBytes);

            String token = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(randomBytes);

            session.setAttribute(SESSION_ATTRIBUTE, token);
            return token;
        }
    }

    /**
     * Validates a submitted form token without creating a session.
     * Missing, malformed or duplicate token parameters are rejected.
     */
    public boolean isValid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return false;
        }

        String[] submittedValues =
                request.getParameterValues(PARAMETER_NAME);

        if (submittedValues == null || submittedValues.length != 1) {
            return false;
        }

        String submitted = submittedValues[0];

        if (!hasValidFormat(submitted)) {
            return false;
        }

        synchronized (session) {
            Object stored = session.getAttribute(SESSION_ATTRIBUTE);

            if (!(stored instanceof String expected)
                    || !hasValidFormat(expected)) {
                return false;
            }

            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.US_ASCII),
                    submitted.getBytes(StandardCharsets.US_ASCII)
            );
        }
    }

    private boolean hasValidFormat(String token) {
        return token != null
                && token.matches("[A-Za-z0-9_-]{43}");
    }
}