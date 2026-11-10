/**
 * ============================================================
 * MEMBER   : IT26101136 - [Name]
 * MODULE   : Learning packages and enrollment
 * FILE     : AdminEnrollmentController.java
 * WHAT IT DOES (simple words): Shows waiting payments and accepts admin decisions.
 * REAL-LIFE EXAMPLE: The cashier checks a receipt before issuing lesson tickets.
 * OOP USED : Abstraction and constructor injection through EnrollmentAdministration
 * CRUD     : READ and UPDATE enrollment requests
 * ============================================================
 */
package com.drivingschool.learningpackage.controller;

import com.drivingschool.auth.annotation.RequireAdmin;
import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.learningpackage.model.Enrollment;
import com.drivingschool.learningpackage.service.EnrollmentAdministration;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@Controller
@RequireAdmin
@RequestMapping("/admin/enrollments")
// SOLID: S - This class prepares pages; the service decides payment rules.
public class AdminEnrollmentController {
    // SOLID: D - The controller receives a small admin contract through its constructor.
    private final EnrollmentAdministration payments;
    private final CsrfTokenService csrf;

    // Receives the cashier operations and normal form-token helper.
    public AdminEnrollmentController(EnrollmentAdministration payments, CsrfTokenService csrf) {
        this.payments = payments;
        this.csrf = csrf;
    }

    // Lists waiting receipts with readable names while leaving amounts unchanged.
    // CRUD: READ   - finds or lists enrollments
    @GetMapping
    public String list(HttpServletRequest request, HttpServletResponse response, Model model) {
        List<Enrollment> waiting = payments.awaitingPayments(request);
        Map<String, String> students = new LinkedHashMap<>();
        Map<String, String> packages = new LinkedHashMap<>();
        for (Enrollment enrollment : waiting) {
            students.put(enrollment.getStudentId(), payments.studentName(request, enrollment.getStudentId()));
            packages.put(enrollment.getPackageId(), payments.packageName(enrollment.getPackageId()));
        }
        response.setHeader("Cache-Control", "no-store");
        model.addAttribute("payments", waiting);
        model.addAttribute("studentNames", students);
        model.addAttribute("packageNames", packages);
        model.addAttribute("csrfParameterName", CsrfTokenService.PARAMETER_NAME);
        model.addAttribute("csrfToken", csrf.getOrCreateToken(request));
        return "admin/enrollments";
    }

    // Records confirmation or explains why this receipt can no longer be confirmed.
    // CRUD: UPDATE - changes an existing enrollment
    @PostMapping("/mark-paid")
    public String markPaid(HttpServletRequest request, @RequestParam String enrollmentId,
                           RedirectAttributes flash) {
        try {
            payments.markPaid(request, enrollmentId);
            flash.addFlashAttribute("success", "Payment confirmed. Lessons are now available.");
        } catch (ValidationException exception) {
            flash.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/enrollments";
    }

    // Keeps an unpaid cancellation in history and shows any already-handled explanation.
    // CRUD: UPDATE - changes an existing enrollment
    @PostMapping("/cancel-request")
    public String cancel(HttpServletRequest request, @RequestParam String enrollmentId,
                         @RequestParam(required = false) String reason, RedirectAttributes flash) {
        try {
            payments.cancelRequest(request, enrollmentId, reason);
            flash.addFlashAttribute("success", "Request cancelled. Any pending lesson requests were rejected.");
        } catch (ValidationException exception) {
            flash.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/enrollments";
    }
}
