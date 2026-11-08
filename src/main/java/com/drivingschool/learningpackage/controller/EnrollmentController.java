/**
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages and enrollment
FILE     : EnrollmentController.java
WHAT IT DOES (simple words): Prepares pages and accepts forms for enrollment.
REAL-LIFE EXAMPLE: A course folder keeps the learning plan and enrollment details together.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.learningpackage.controller;

import com.drivingschool.auth.annotation.RequireLogin;
import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.learningpackage.service.EnrollmentOperations;
import com.drivingschool.learningpackage.model.Enrollment;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
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
// SOLID: S - This class is responsible for web requests and pages.
public class EnrollmentController {

    // SOLID: D - Enrollment pages use the small operations contract via constructor injection.
    private final EnrollmentOperations enrollmentService;
    private final AuthSessionService auth;
    private final CsrfTokenService csrf;
    private final Clock clock;

    // Receives the helpers this class needs so each job stays separate.
    public EnrollmentController(EnrollmentOperations enrollmentService,
                                AuthSessionService auth,
                                CsrfTokenService csrf, Clock clock) {
        this.enrollmentService = enrollmentService;
        this.auth = auth;
        this.csrf = csrf;
        this.clock = clock;
    }

    @GetMapping
    // Handles list for this class without changing another class's job.
    // CRUD: READ   - finds or lists enrollments
    public String list(HttpServletRequest request, HttpServletResponse response,
                       Model model) {
        requireStudent(request);
        response.setHeader("Cache-Control", "no-store");
        List<Enrollment> enrollments = enrollmentService.ownEnrollments(request);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("expiryLabels", expiryLabels(enrollments));
        model.addAttribute("csrfParameterName", CsrfTokenService.PARAMETER_NAME);
        model.addAttribute("csrfToken", csrf.getOrCreateToken(request));
        return "student/enrollments";
    }

    @PostMapping
    // Handles enroll for this class without changing another class's job.
    // CRUD: CREATE - adds a new enrollment and saves it
    public String enroll(HttpServletRequest request,
                         @RequestParam String packageId,
                         RedirectAttributes flash) {
        requireStudent(request);
        enrollmentService.enroll(request, packageId);
        flash.addFlashAttribute("enrolled", true);
        return "redirect:/enrollments";
    }

    @PostMapping("/update")
    // Saves valid changes while preserving the identity of the existing record.
    // CRUD: UPDATE - changes an existing enrollment
    public String update(HttpServletRequest request, @RequestParam String enrollmentId,
                         @RequestParam String learningGoal, RedirectAttributes flash) {
        enrollmentService.updateLearningGoal(request, enrollmentId, learningGoal);
        flash.addFlashAttribute("success", "Your learning goal was saved.");
        return "redirect:/enrollments";
    }

    @PostMapping("/delete")
    // Checks the operation rules before removing the selected record.
    // CRUD: DELETE - removes a enrollment
    public String delete(HttpServletRequest request, @RequestParam String enrollmentId,
                         RedirectAttributes flash) {
        enrollmentService.deleteOwnEnrollment(request, enrollmentId);
        flash.addFlashAttribute("success", "Unused unpaid enrollment removed.");
        return "redirect:/enrollments";
    }

    private Map<String, String> expiryLabels(List<Enrollment> enrollments) {
        Map<String, String> labels = new LinkedHashMap<>();
        LocalDate today = LocalDate.now(clock);
        for (Enrollment enrollment : enrollments) {
            LocalDate validUntil = enrollment.getValidUntil();
            if (validUntil == null) labels.put(enrollment.getId(), "Expiry not recorded");
            else if (validUntil.isBefore(today)) labels.put(enrollment.getId(), "Expired");
            else if (!validUntil.isAfter(today.plusDays(14))) labels.put(enrollment.getId(), "Expires within 14 days");
            else labels.put(enrollment.getId(), "Within validity period");
        }
        return labels;
    }

    // Refuses access unless the current account meets the require student rule.
    private void requireStudent(HttpServletRequest request) {
        if (auth.requireLogin(request).getRole() != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Student access is required.");
        }
    }
}
