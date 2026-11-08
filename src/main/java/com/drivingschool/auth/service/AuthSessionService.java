/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : AuthSessionService.java
WHAT IT DOES (simple words): Applies the rules for auth session.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.auth.service;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.model.AccountStatus;
import com.drivingschool.auth.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
// SOLID: S - This class is responsible for application rules.
public class AuthSessionService {

    private static final String USER_ID_ATTRIBUTE =
            "drivespark.auth.userId";

    private static final int SESSION_TIMEOUT_SECONDS = 30 * 60;

    private final AuthService authService;
    private final UserRepository userRepository;

    // Receives the helpers this class needs so each job stays separate.
    public AuthSessionService(
            AuthService authService,
            UserRepository userRepository) {

        this.authService = authService;
        this.userRepository = userRepository;
    }

    // Checks credentials before opening an account session.
    public boolean login(
            HttpServletRequest request,
            String email,
            String rawPassword) {

        return loginWithResult(request, email, rawPassword)
                == LoginResult.SUCCESS;
    }

    // Checks the password and account status before creating a fresh session.
    public LoginResult loginWithResult(
            HttpServletRequest request,
            String email,
            String rawPassword) {

        Optional<User> authenticated =
                authService.authenticate(email, rawPassword);

        // Remove any previous authenticated or anonymous session.
        logout(request);

        if (authenticated.isEmpty()) {
            return LoginResult.INVALID_CREDENTIALS;
        }
        if (authenticated.get().getStatus() == AccountStatus.PENDING) {
            return LoginResult.PENDING_APPROVAL;
        }
        if (authenticated.get().getStatus() == AccountStatus.SUSPENDED) {
            return LoginResult.SUSPENDED;
        }

        // A new session receives a new identifier.
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
        session.setAttribute(
                USER_ID_ATTRIBUTE,
                authenticated.get().getId()
        );

        return LoginResult.SUCCESS;
    }

    // Reloads the account so deleted or suspended users lose access promptly.
    public Optional<User> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return Optional.empty();
        }

        Object storedId = session.getAttribute(USER_ID_ATTRIBUTE);

        // An anonymous session may exist for other application features.
        if (storedId == null) {
            return Optional.empty();
        }

        if (!(storedId instanceof String userId)
                || !userId.matches("[SIA](?!000)[0-9]{3}")) {

            session.invalidate();
            return Optional.empty();
        }

        // Reload the account instead of trusting a stored User or role.
        // StorageException deliberately propagates to the caller.
        Optional<User> currentUser =
                userRepository.findByIdStrict(userId);

        if (currentUser.isPresent()
                && currentUser.get().getStatus() != AccountStatus.ACTIVE) {
            session.invalidate();
            return Optional.empty();
        }

        if (currentUser.isEmpty()) {
            // An account deleted since login must lose access.
            session.invalidate();
        }

        return currentUser;
    }

    // Handles is logged in for this class without changing another class's job.
    public boolean isLoggedIn(HttpServletRequest request) {
        return getCurrentUser(request).isPresent();
    }

    // Handles is admin for this class without changing another class's job.
    public boolean isAdmin(HttpServletRequest request) {
        Optional<User> user = getCurrentUser(request);
        return user.isPresent() && user.get().getRole() == Role.ADMIN;
    }

    // Refuses access unless the current account meets the require login rule.
    public User requireLogin(HttpServletRequest request) {
        return getCurrentUser(request)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Please log in to continue."
                ));
    }

    // Refuses access unless the current account meets the require admin rule.
    public User requireAdmin(HttpServletRequest request) {
        User user = requireLogin(request);

        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Administrator access is required."
            );
        }

        return user;
    }

    // Closes the session so this browser no longer acts as the signed-in user.
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }
    }
}
