// Module owner: IT26101137
package com.drivingschool.review.controller;

import com.drivingschool.auth.annotation.RequireAdmin;
import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.review.model.Review;
import com.drivingschool.review.service.ReviewService;
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

@Controller
@RequireAdmin
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final ReviewService reviewService;
    private final CsrfTokenService csrfTokenService;

    public AdminReviewController(ReviewService reviewService,
                                 CsrfTokenService csrfTokenService) {
        this.reviewService = reviewService;
        this.csrfTokenService = csrfTokenService;
    }

    @GetMapping
    public String list(HttpServletRequest request, HttpServletResponse response,
                       Model model,
                       @RequestParam(required = false) String status) {
        response.setHeader("Cache-Control", "no-store");
        List<Review> reviews = reviewService.getVisibleReviews(request).stream()
                .filter(r -> status == null || status.isBlank() || r.getStatus().name().equalsIgnoreCase(status)).toList();
        model.addAttribute("reviews", reviews);
        model.addAttribute("filterStatus", status);
        model.addAttribute("csrfParameterName", CsrfTokenService.PARAMETER_NAME);
        model.addAttribute("csrfToken", csrfTokenService.getOrCreateToken(request));
        return "admin/reviews";
    }

    @PostMapping("/delete")
    public String delete(HttpServletRequest request, @RequestParam String reviewId,
                         RedirectAttributes flash) {
        reviewService.deleteReview(request, reviewId);
        flash.addFlashAttribute("success", "Review deleted.");
        return "redirect:/admin/reviews";
    }

    @PostMapping("/approve")
    public String approve(HttpServletRequest request,
                          @RequestParam String reviewId,
                          RedirectAttributes redirectAttributes) {
        reviewService.approveReview(request, reviewId);
        redirectAttributes.addFlashAttribute("success", "Review approved.");
        return "redirect:/admin/reviews";
    }

    @PostMapping("/hide")
    public String hide(HttpServletRequest request,
                       @RequestParam String reviewId,
                       RedirectAttributes redirectAttributes) {
        reviewService.hideReview(request, reviewId);
        redirectAttributes.addFlashAttribute("success", "Review hidden.");
        return "redirect:/admin/reviews";
    }
}
