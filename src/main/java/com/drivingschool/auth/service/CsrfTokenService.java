/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : CsrfTokenService.java
WHAT IT DOES (simple words): Applies the rules for csrf token.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
// SOLID: S - This class is responsible for application rules.
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
    // Shares the or create token through a public method while the field stays private.
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
    // Handles is valid for this class without changing another class's job.
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

    // Handles has valid format for this class without changing another class's job.
    private boolean hasValidFormat(String token) {
        return token != null
                && token.matches("[A-Za-z0-9_-]{43}");
    }
}