package com.drivingschool.auth.service;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class AuthSessionService {

    private static final String USER_ID_ATTRIBUTE =
            "drivespark.auth.userId";

    private static final int SESSION_TIMEOUT_SECONDS = 30 * 60;

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthSessionService(
            AuthService authService,
            UserRepository userRepository) {

        this.authService = authService;
        this.userRepository = userRepository;
    }

    public boolean login(
            HttpServletRequest request,
            String email,
            String rawPassword) {

        Optional<User> authenticated =
                authService.authenticate(email, rawPassword);

        // Remove any previous authenticated or anonymous session.
        logout(request);

        if (authenticated.isEmpty()) {
            return false;
        }

        // A new session receives a new identifier.
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
        session.setAttribute(
                USER_ID_ATTRIBUTE,
                authenticated.get().getId()
        );

        return true;
    }

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
                || !userId.matches("[SA](?!000)[0-9]{3}")) {

            session.invalidate();
            return Optional.empty();
        }

        // Reload the account instead of trusting a stored User or role.
        // StorageException deliberately propagates to the caller.
        Optional<User> currentUser =
                userRepository.findByIdStrict(userId);

        if (currentUser.isEmpty()) {
            // An account deleted since login must lose access.
            session.invalidate();
        }

        return currentUser;
    }

    public boolean isLoggedIn(HttpServletRequest request) {
        return getCurrentUser(request).isPresent();
    }

    public boolean isAdmin(HttpServletRequest request) {
        return getCurrentUser(request)
                .map(user -> user.getRole() == Role.ADMIN)
                .orElse(false);
    }

    public User requireLogin(HttpServletRequest request) {
        return getCurrentUser(request)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Please log in to continue."
                ));
    }

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

    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }
    }
}