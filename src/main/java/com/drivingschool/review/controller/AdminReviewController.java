package com.drivingschool.review.controller;

import com.drivingschool.auth.annotation.RequireAdmin;
import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.review.service.ReviewService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequireAdmin
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final ReviewService reviewService;
    private final CsrfTokenService csrfTokenService;

    public AdminReviewController(
            ReviewService reviewService,
            CsrfTokenService csrfTokenService) {

        this.reviewService = reviewService;
        this.csrfTokenService = csrfTokenService;
    }

    // Display all reviews for administrators
    @GetMapping
    public String showAllReviews(
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        response.setHeader("Cache-Control", "no-store");

        model.addAttribute(
                "reviews",
                reviewService.getVisibleReviews(request)
        );

        addCsrfAttributes(request, model);

        return "review/admin-list";
    }

    // Approve a review
    @PostMapping("/{id}/approve")
    public String approveReview(
            @PathVariable String id,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        reviewService.approveReview(request, id);

        redirectAttributes.addFlashAttribute(
                "moderationSuccess",
                "Review approved successfully."
        );

        return "redirect:/admin/reviews";
    }

    // Hide a review
    @PostMapping("/{id}/hide")
    public String hideReview(
            @PathVariable String id,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        reviewService.hideReview(request, id);

        redirectAttributes.addFlashAttribute(
                "moderationSuccess",
                "Review hidden successfully."
        );

        return "redirect:/admin/reviews";
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