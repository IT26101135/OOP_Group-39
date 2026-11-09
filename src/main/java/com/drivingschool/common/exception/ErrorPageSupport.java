/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Shared foundation
FILE     : ErrorPageSupport.java
WHAT IT DOES (simple words): Explains errors and gives each user a safe next step.
REAL-LIFE EXAMPLE: A receptionist directs a visitor back to the right desk.
OOP USED : Encapsulation and exception handling
CRUD     : Supports errors during CREATE, READ, UPDATE and DELETE
============================================================
*/
package com.drivingschool.common.exception;

import com.drivingschool.auth.model.User;
import com.drivingschool.auth.service.AuthSessionService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import java.util.Optional;

@Component
// SOLID: S - This helper only prepares safe error-page information.
public class ErrorPageSupport {
    private final AuthSessionService sessions;

    // Receive the existing session helper instead of guessing a role from an account ID.
    public ErrorPageSupport(AuthSessionService sessions) {
        this.sessions = sessions;
    }

    // Keep all errors on the existing view, including the access-control 403 view.
    public ModelAndView prepare(HttpServletRequest request, HttpStatus status, String title, String message) {
        ModelAndView page = new ModelAndView("common/error");
        page.setStatus(status);
        page.addObject("statusCode", status.value());
        page.addObject("title", title);
        page.addObject("message", message);
        // Step 1: Reload the current account through the existing session rules.
        Optional<User> account = currentAccount(request);
        String role = "PUBLIC";
        if (account.isPresent()) role = account.get().getRole().name();
        page.addObject("navRole", role);
        // Step 2: Offer the role's own destination and, for validation, the original form.
        page.addObject("recoveryUrl", dashboard(role));
        if (status == HttpStatus.BAD_REQUEST) page.addObject("retryUrl", formPath(request));
        return page;
    }

    // A storage error must still show a safe page if account lookup also fails.
    private Optional<User> currentAccount(HttpServletRequest request) {
        try {
            return sessions.getCurrentUser(request);
        } catch (StorageException exception) {
            return Optional.empty();
        }
    }

    // Give each role a destination it can actually open.
    private String dashboard(String role) {
        if (role.equals("ADMIN")) return "/admin/dashboard";
        if (role.equals("INSTRUCTOR")) return "/instructor/dashboard";
        if (role.equals("STUDENT")) return "/student/profile";
        return "/login";
    }

    // Only known local GET forms may be used; never trust an external Referer address.
    private String formPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        Object original = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        if (original instanceof String) path = (String) original;
        if (path.matches("/admin/vehicles/V[0-9]+/edit")) return path;
        if (path.startsWith("/admin/vehicles")) return "/admin/vehicles/create";
        if (path.equals("/reset-password")) return "/forgot-password";
        if (path.startsWith("/student/password")) return "/student/password";
        if (path.startsWith("/student/profile")) return "/student/profile/edit";
        if (path.startsWith("/instructor/availability") || path.startsWith("/instructor/leave")) return "/instructor/availability";
        if (path.startsWith("/instructor/")) return "/instructor/dashboard";
        return matchingForm(path);
    }

    // Each module has one existing GET page where its forms can be corrected.
    private String matchingForm(String path) {
        String[] forms = {"/admin/packages", "/admin/instructors", "/admin/students",
                "/admin/bookings", "/admin/enrollments", "/admin/reviews", "/bookings",
                "/enrollments", "/reviews", "/contact", "/login", "/register", "/forgot-password", "/packages", "/instructors"};
        for (String form : forms) {
            if (path.startsWith(form)) return form;
        }
        return null;
    }
}
