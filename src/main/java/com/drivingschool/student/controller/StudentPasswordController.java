// Module owner: IT26101135
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
public class StudentPasswordController {

    private final AuthSessionService auth;
    private final PasswordManagementService passwords;
    private final CsrfTokenService csrf;

    public StudentPasswordController(AuthSessionService auth,
                                     PasswordManagementService passwords,
                                     CsrfTokenService csrf) {
        this.auth = auth;
        this.passwords = passwords;
        this.csrf = csrf;
    }

    @GetMapping
    public String form(HttpServletRequest request, HttpServletResponse response,
                       Model model) {
        requireStudent(request);
        response.setHeader("Cache-Control", "no-store");
        model.addAttribute("csrfParameterName", CsrfTokenService.PARAMETER_NAME);
        model.addAttribute("csrfToken", csrf.getOrCreateToken(request));
        return "student/password";
    }

    @PostMapping
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

    private void requireStudent(HttpServletRequest request) {
        if (auth.requireLogin(request).getRole() != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Student access is required.");
        }
    }
}
