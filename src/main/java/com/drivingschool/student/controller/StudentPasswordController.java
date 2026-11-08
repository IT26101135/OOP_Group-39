/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : StudentPasswordController.java
WHAT IT DOES (simple words): Prepares pages and accepts forms for student password.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.student.controller;

import com.drivingschool.auth.annotation.RequireLogin;
import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.auth.service.PasswordManagementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequireLogin
@RequestMapping("/student/password")
// SOLID: S - This class is responsible for web requests and pages.
public class StudentPasswordController {

    private final AuthSessionService auth;
    private final PasswordManagementService passwords;
    private final CsrfTokenService csrf;

    // Receives the helpers this class needs so each job stays separate.
    public StudentPasswordController(AuthSessionService auth,
                                     PasswordManagementService passwords,
                                     CsrfTokenService csrf) {
        this.auth = auth;
        this.passwords = passwords;
        this.csrf = csrf;
    }

    @GetMapping
    // Handles form for this class without changing another class's job.
    public String form(HttpServletRequest request, HttpServletResponse response,
                       Model model) {
        requireStudent(request);
        response.setHeader("Cache-Control", "no-store");
        model.addAttribute("csrfParameterName", CsrfTokenService.PARAMETER_NAME);
        model.addAttribute("csrfToken", csrf.getOrCreateToken(request));
        return "student/password";
    }

    @PostMapping
    // Handles change for this class without changing another class's job.
    // CRUD: UPDATE - changes an existing student account
    public String change(HttpServletRequest request,
                         @RequestParam String currentPassword,
                         @RequestParam String password,
                         @RequestParam String confirmPassword,
                         RedirectAttributes flash) {
        requireStudent(request);
        if (!password.equals(confirmPassword)) {
            throw new com.drivingschool.common.exception.ValidationException(
                    "Passwords do not match.");
        }
        passwords.changeOwnPassword(request, currentPassword, password);
        flash.addFlashAttribute("passwordChanged", true);
        return "redirect:/student/profile";
    }

    // Refuses access unless the current account meets the require student rule.
    private void requireStudent(HttpServletRequest request) {
        if (auth.requireLogin(request).getRole() != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Student access is required.");
        }
    }
}
