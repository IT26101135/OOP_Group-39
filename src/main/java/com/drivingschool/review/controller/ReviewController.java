// Module owner: IT26101137
package com.drivingschool.review.controller;

import com.drivingschool.auth.annotation.RequireLogin;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.review.model.Review;
import com.drivingschool.review.service.ReviewService;
import com.drivingschool.booking.service.BookingService;
import com.drivingschool.booking.model.BookingStatus;
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
@RequireLogin
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final AuthSessionService authSessionService;
    private final CsrfTokenService csrfTokenService;
    private final BookingService bookingService;

    public ReviewController(ReviewService reviewService,
                            AuthSessionService authSessionService,
                            CsrfTokenService csrfTokenService,
                            BookingService bookingService) {
        this.reviewService = reviewService;
        this.authSessionService = authSessionService;
        this.csrfTokenService = csrfTokenService;
        this.bookingService = bookingService;
    }

    @GetMapping
    public String list(HttpServletRequest request, HttpServletResponse response,
                       Model model) {
        response.setHeader("Cache-Control", "no-store");
        User user = authSessionService.requireLogin(request);
        List<Review> reviews = reviewService.getVisibleReviews(request);
        model.addAttribute("reviews", reviews);
        model.addAttribute("currentUserId", user.getId());
        if (user.getRole() == com.drivingschool.auth.model.Role.STUDENT) {
            model.addAttribute("completedBookings", bookingService.getOwnBookings(request).stream()
                    .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                    .filter(b -> reviews.stream().noneMatch(r -> b.getId().equals(r.getBookingId())))
                    .toList());
        }
        addCsrf(request, model);
        return "review/list";
    }

    @PostMapping
    public String create(HttpServletRequest request,
                         @RequestParam int rating,
                         @RequestParam String bookingId,
                         @RequestParam String comment,
                         RedirectAttributes redirectAttributes) {
        reviewService.createReview(request, bookingId, rating, comment);
        redirectAttributes.addFlashAttribute("success", "Review submitted for approval.");
        return "redirect:/reviews";
    }

    @PostMapping("/update")
    public String update(HttpServletRequest request,
                         @RequestParam String reviewId,
                         @RequestParam int rating,
                         @RequestParam String comment,
                         RedirectAttributes redirectAttributes) {
        reviewService.updateOwnReview(request, reviewId, rating, comment);
        redirectAttributes.addFlashAttribute("success", "Review updated and returned for approval.");
        return "redirect:/reviews";
    }

    @PostMapping("/delete")
    public String delete(HttpServletRequest request,
                         @RequestParam String reviewId,
                         RedirectAttributes redirectAttributes) {
        reviewService.deleteOwnReview(request, reviewId);
        redirectAttributes.addFlashAttribute("success", "Review deleted.");
        return "redirect:/reviews";
    }

    private void addCsrf(HttpServletRequest request, Model model) {
        model.addAttribute("csrfParameterName", CsrfTokenService.PARAMETER_NAME);
        model.addAttribute("csrfToken", csrfTokenService.getOrCreateToken(request));
    }
}
