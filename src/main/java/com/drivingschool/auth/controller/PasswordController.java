/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : PasswordController.java
WHAT IT DOES (simple words): Prepares pages and accepts forms for password.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.auth.controller;

import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.auth.service.PasswordManagementService;
import com.drivingschool.common.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
// SOLID: S - This class is responsible for web requests and pages.
public class PasswordController {

    private final PasswordManagementService passwords;
    private final CsrfTokenService csrf;

    // Receives the helpers this class needs so each job stays separate.
    public PasswordController(PasswordManagementService passwords,
                              CsrfTokenService csrf) {
        this.passwords = passwords;
        this.csrf = csrf;
    }

    @GetMapping("/forgot-password")
    // Handles forgot page for this class without changing another class's job.
    public String forgotPage(HttpServletRequest request, Model model) {
        addCsrf(request, model);
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    // Handles request reset for this class without changing another class's job.
    public String requestReset(HttpServletRequest request,
                               @RequestParam String email, Model model) {
        String token = passwords.createResetToken(email);
        model.addAttribute("resetRequested", true);
        // Development simulation in place of an email delivery service.
        if (token != null) model.addAttribute("demoResetToken", token);
        model.addAttribute("resetEmail", email);
        addCsrf(request, model);
        return "auth/forgot-password";
    }

    @PostMapping("/reset-password")
    // Handles reset password for this class without changing another class's job.
    public String resetPassword(HttpServletRequest request,
                                @RequestParam String email,
                                @RequestParam String token,
                                @RequestParam String password,
                                @RequestParam String confirmPassword,
                                RedirectAttributes flash) {
        try {
            if (!password.equals(confirmPassword)) {
                throw new ValidationException("Passwords do not match.");
            }
            passwords.resetPassword(email, token, password);
            flash.addFlashAttribute("passwordReset", true);
            return "redirect:/login";
        } finally {
            password = null;
            confirmPassword = null;
        }
    }

    // Handles add csrf for this class without changing another class's job.
    private void addCsrf(HttpServletRequest request, Model model) {
        model.addAttribute("csrfParameterName", CsrfTokenService.PARAMETER_NAME);
        model.addAttribute("csrfToken", csrf.getOrCreateToken(request));
    }
}
