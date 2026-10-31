package com.drivingschool.auth.controller;

import com.drivingschool.auth.form.LoginForm;
import com.drivingschool.auth.form.RegistrationForm;
import com.drivingschool.auth.service.AuthService;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.auth.service.CsrfTokenService;
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
public class AuthController {

    private final AuthService authService;
    private final AuthSessionService authSessionService;
    private final CsrfTokenService csrfTokenService;

    public AuthController(
            AuthService authService,
            AuthSessionService authSessionService,
            CsrfTokenService csrfTokenService) {

        this.authService = authService;
        this.authSessionService = authSessionService;
        this.csrfTokenService = csrfTokenService;
    }

    @InitBinder("registrationForm")
    public void configureRegistrationBinding(WebDataBinder binder) {
        binder.setAllowedFields(
                "name", "email", "phone",
                "password", "confirmPassword"
        );
    }

    @InitBinder("loginForm")
    public void configureLoginBinding(WebDataBinder binder) {
        binder.setAllowedFields("email", "password");
    }

    @GetMapping("/register")
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
            boolean loggedIn = authSessionService.login(
                    request,
                    form.getEmail(),
                    form.getPassword()
            );

            if (loggedIn) {
                return "redirect:/";
            }

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            model.addAttribute(
                    "formError",
                    "Email or password is incorrect."
            );

            // Failed login invalidates the old session.
            // Generate a token for the replacement form session.
            return loginPage(request, model);
        } finally {
            form.clearPassword();
        }
    }

    @PostMapping("/logout")
    public String logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        response.setHeader("Cache-Control", "no-store");
        authSessionService.logout(request);
        return "redirect:/login?loggedOut";
    }

    private String registrationPage(
            HttpServletRequest request,
            Model model) {

        addCsrfAttributes(request, model);
        return "auth/register";
    }

    private String loginPage(
            HttpServletRequest request,
            Model model) {

        addCsrfAttributes(request, model);
        return "auth/login";
    }

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