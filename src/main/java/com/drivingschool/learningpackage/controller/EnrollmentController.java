// Module owner: IT26101136
package com.drivingschool.learningpackage.controller;

import com.drivingschool.auth.annotation.RequireLogin;
import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.learningpackage.service.EnrollmentService;
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
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final AuthSessionService auth;
    private final CsrfTokenService csrf;

    public EnrollmentController(EnrollmentService enrollmentService,
                                AuthSessionService auth,
                                CsrfTokenService csrf) {
        this.enrollmentService = enrollmentService;
        this.auth = auth;
        this.csrf = csrf;
    }

    @GetMapping
    public String list(HttpServletRequest request, HttpServletResponse response,
                       Model model) {
        requireStudent(request);
        response.setHeader("Cache-Control", "no-store");
        model.addAttribute("enrollments", enrollmentService.ownEnrollments(request));
        model.addAttribute("csrfParameterName", CsrfTokenService.PARAMETER_NAME);
        model.addAttribute("csrfToken", csrf.getOrCreateToken(request));
        return "student/enrollments";
    }

    @PostMapping
    public String enroll(HttpServletRequest request,
                         @RequestParam String packageId,
                         RedirectAttributes flash) {
        requireStudent(request);
        enrollmentService.enroll(request, packageId);
        flash.addFlashAttribute("enrolled", true);
        return "redirect:/enrollments";
    }

    private void requireStudent(HttpServletRequest request) {
        if (auth.requireLogin(request).getRole() != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Student access is required.");
        }
    }
}
