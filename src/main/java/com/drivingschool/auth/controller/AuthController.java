/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : AuthController.java
WHAT IT DOES (simple words): Prepares pages and accepts forms for auth.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.auth.controller;

import com.drivingschool.auth.form.LoginForm;
import com.drivingschool.auth.form.RegistrationForm;
import com.drivingschool.auth.service.AuthService;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.auth.service.LoginResult;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
// SOLID: S - This class is responsible for web requests and pages.
public class AuthController {

    private final AuthService authService;
    private final AuthSessionService authSessionService;
    private final CsrfTokenService csrfTokenService;

    // Receives the helpers this class needs so each job stays separate.
    public AuthController(
            AuthService authService,
            AuthSessionService authSessionService,
            CsrfTokenService csrfTokenService) {

        this.authService = authService;
        this.authSessionService = authSessionService;
        this.csrfTokenService = csrfTokenService;
    }

    @InitBinder("registrationForm")
    // Accepts only registration fields, protecting internal account settings.
    public void configureRegistrationBinding(WebDataBinder binder) {
        binder.setAllowedFields(
                "name", "email", "phone",
                "password", "confirmPassword"
        );
    }

    @InitBinder("loginForm")
    // Accepts only login fields from the browser.
    public void configureLoginBinding(WebDataBinder binder) {
        binder.setAllowedFields("email", "password");
    }

    @GetMapping("/register")
    // Prepares the registration page and its existing form details.
    public String showRegistration(
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        response.setHeader("Cache-Control", "no-store");

        if (authSessionService.isLoggedIn(request)) {
            return "redirect:/";
        }

        model.addAttribute("registrationForm", new RegistrationForm());
        return registrationPage(request, model);
    }

    @PostMapping("/register")
    // Accepts the registration form and explains any information that needs correcting.
    // CRUD: CREATE - adds a new student account and saves it
    public String register(
            @ModelAttribute("registrationForm") RegistrationForm form,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        response.setHeader("Cache-Control", "no-store");

        if (authSessionService.isLoggedIn(request)) {
            form.clearPasswords();
            return "redirect:/";
        }

        try {
            if (form.getPassword() == null
                    || form.getPassword().isBlank()) {
                throw new ValidationException("Password is required.");
            }

            if (!form.getPassword().equals(form.getConfirmPassword())) {
                throw new ValidationException("Passwords do not match.");
            }

            authService.registerStudent(
                    form.getName(),
                    form.getEmail(),
                    form.getPhone(),
                    form.getPassword()
            );

            return "redirect:/login?registered";
        } catch (ValidationException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute("formError", exception.getMessage());
            return registrationPage(request, model);
        } catch (DuplicateRecordException exception) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            model.addAttribute("formError", exception.getMessage());
            return registrationPage(request, model);
        } finally {
            form.clearPasswords();
        }
    }

    @GetMapping("/login")
    // Prepares the login page and its existing form details.
    // CRUD: READ   - finds or lists student accounts
    public String showLogin(
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        response.setHeader("Cache-Control", "no-store");

        if (authSessionService.isLoggedIn(request)) {
            return "redirect:/";
        }

        model.addAttribute("loginForm", new LoginForm());
        return loginPage(request, model);
    }

    @PostMapping("/login")
    // Checks credentials before opening an account session.
    public String login(
            @ModelAttribute("loginForm") LoginForm form,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        response.setHeader("Cache-Control", "no-store");

        if (authSessionService.isLoggedIn(request)) {
            form.clearPassword();
            return "redirect:/";
        }

        try {
            LoginResult result = authSessionService.loginWithResult(
                    request,
                    form.getEmail(),
                    form.getPassword()
            );

            // Keeps controller tests and older integrations that stub the
            // original boolean API compatible; the real service returns a result.
            if (result == null) {
                if (authSessionService.login(request, form.getEmail(), form.getPassword())) {
                    return "redirect:/";
                }
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                model.addAttribute("formError", "Email or password is incorrect.");
                return loginPage(request, model);
            }

            if (result == LoginResult.SUCCESS) {
                return "redirect:" + authSessionService
                        .getCurrentUser(request).orElseThrow().getDashboardPath();
            }

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            String message = "Email or password is incorrect.";
            if (result == LoginResult.PENDING_APPROVAL) {
                message = "Your account is waiting for administrator approval.";
            } else if (result == LoginResult.SUSPENDED) {
                message = "This account is suspended. Contact the school administrator.";
            }
            model.addAttribute("formError", message);

            // Failed login invalidates the old session.
            // Generate a token for the replacement form session.
            return loginPage(request, model);
        } finally {
            form.clearPassword();
        }
    }

    @PostMapping("/logout")
    // Closes the session so this browser no longer acts as the signed-in user.
    public String logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        response.setHeader("Cache-Control", "no-store");
        authSessionService.logout(request);
        return "redirect:/login?loggedOut";
    }

    // Handles registration page for this class without changing another class's job.
    private String registrationPage(
            HttpServletRequest request,
            Model model) {

        addCsrfAttributes(request, model);
        return "auth/register";
    }

    // Handles login page for this class without changing another class's job.
    private String loginPage(
            HttpServletRequest request,
            Model model) {

        addCsrfAttributes(request, model);
        return "auth/login";
    }

    // Handles add csrf attributes for this class without changing another class's job.
    private void addCsrfAttributes(
            HttpServletRequest request,
            Model model) {

        model.addAttribute(
                "csrfParameterName",
                CsrfTokenService.PARAMETER_NAME
        );
        model.addAttribute(
                "csrfToken",
                csrfTokenService.getOrCreateToken(request)
        );
    }
}
