/**
============================================================
MEMBER   : IT26101137 - [Name]
MODULE   : Reviews and Reports
FILE     : AdminReviewController.java
WHAT IT DOES (simple words): Keeps reviews and reports details clear and safe for the school.
REAL-LIFE EXAMPLE: Like a feedback book and a school manager counting its records.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for reviews; reports READ saved data
============================================================
*/
package com.drivingschool.review.controller;

import com.drivingschool.auth.annotation.RequireAdmin;
import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.review.model.Review;
import com.drivingschool.review.service.ReviewModeration;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.ArrayList;

@Controller
@RequireAdmin
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final ReviewModeration reviewService;
    private final CsrfTokenService csrfTokenService;

    // Receives the details this class needs and keeps them ready for its own job.
    public AdminReviewController(ReviewModeration reviewService,
                                 CsrfTokenService csrfTokenService) {
        this.reviewService = reviewService;
        this.csrfTokenService = csrfTokenService;
    }

    // Prepares role-filtered reviews and safe form choices for the page.
    // CRUD: READ   - finds or lists reviews
    @GetMapping
    public String list(HttpServletRequest request, HttpServletResponse response,
                       Model model,
                       @RequestParam(required = false) String status) {
        response.setHeader("Cache-Control", "no-store");
        List<Review> reviews = new ArrayList<>();
        for (Review review : reviewService.getVisibleReviews(request)) {
            if (status == null || status.isBlank() || review.getStatus().name().equalsIgnoreCase(status)) {
                reviews.add(review);
            }
        }
        model.addAttribute("reviews", reviews);
        model.addAttribute("filterStatus", status);
        model.addAttribute("csrfParameterName", CsrfTokenService.PARAMETER_NAME);
        model.addAttribute("csrfToken", csrfTokenService.getOrCreateToken(request));
        return "admin/reviews";
    }

    // Removes the chosen record after service checks have passed.
    // CRUD: DELETE - removes a review
    @PostMapping("/delete")
    public String delete(HttpServletRequest request, @RequestParam String reviewId,
                         RedirectAttributes flash) {
        reviewService.deleteReview(request, reviewId);
        flash.addFlashAttribute("success", "Review deleted.");
        return "redirect:/admin/reviews";
    }

    // Publishes the selected review through the administrator-only service checks.
    // CRUD: UPDATE - changes an existing review
    @PostMapping("/approve")
    public String approve(HttpServletRequest request,
                          @RequestParam String reviewId,
                          RedirectAttributes redirectAttributes) {
        reviewService.approveReview(request, reviewId);
        redirectAttributes.addFlashAttribute("success", "Review approved.");
        return "redirect:/admin/reviews";
    }

    // Hides the selected review through the administrator-only service checks.
    // CRUD: UPDATE - changes an existing review
    @PostMapping("/hide")
    public String hide(HttpServletRequest request,
                       @RequestParam String reviewId,
                       RedirectAttributes redirectAttributes) {
        reviewService.hideReview(request, reviewId);
        redirectAttributes.addFlashAttribute("success", "Review hidden.");
        return "redirect:/admin/reviews";
    }
}
