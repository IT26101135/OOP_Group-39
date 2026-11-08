/**
============================================================
MEMBER   : IT26101137 - [Name]
MODULE   : Reviews and Reports
FILE     : ReviewController.java
WHAT IT DOES (simple words): Keeps reviews and reports details clear and safe for the school.
REAL-LIFE EXAMPLE: Like a feedback book and a school manager counting its records.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for reviews; reports READ saved data
============================================================
*/
package com.drivingschool.review.controller;

import com.drivingschool.auth.annotation.RequireLogin;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.review.model.Review;
import com.drivingschool.review.service.ReviewOperations;
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
import java.util.ArrayList;
import com.drivingschool.booking.model.Booking;

@Controller
@RequireLogin
@RequestMapping("/reviews")
public class ReviewController {

    // SOLID: D - Receive the feedback interface instead of constructing its implementation.
    private final ReviewOperations reviewService;
    private final AuthSessionService authSessionService;
    private final CsrfTokenService csrfTokenService;
    private final BookingService bookingService;

    // Receives the details this class needs and keeps them ready for its own job.
    public ReviewController(ReviewOperations reviewService,
                            AuthSessionService authSessionService,
                            CsrfTokenService csrfTokenService,
                            BookingService bookingService) {
        this.reviewService = reviewService;
        this.authSessionService = authSessionService;
        this.csrfTokenService = csrfTokenService;
        this.bookingService = bookingService;
    }

    // Prepares role-filtered reviews and safe form choices for the page.
    // CRUD: READ   - finds or lists reviews
    @GetMapping
    public String list(HttpServletRequest request, HttpServletResponse response,
                       Model model) {
        response.setHeader("Cache-Control", "no-store");
        User user = authSessionService.requireLogin(request);
        List<Review> reviews = reviewService.getVisibleReviews(request);
        model.addAttribute("reviews", reviews);
        model.addAttribute("currentUserId", user.getId());
        if (user.getRole() == com.drivingschool.auth.model.Role.STUDENT) {
            model.addAttribute("completedBookings", unreviewedLessons(request, reviews));
        }
        addCsrf(request, model);
        return "review/list";
    }

    // Accepts a completed-lesson review and lets the service check ownership and validity.
    // CRUD: CREATE - adds a new review and saves it
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

    // Replaces an existing record after service checks have passed.
    // CRUD: UPDATE - changes an existing review
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

    // Removes the chosen record after service checks have passed.
    // CRUD: DELETE - removes a review
    @PostMapping("/delete")
    public String delete(HttpServletRequest request,
                         @RequestParam String reviewId,
                         RedirectAttributes redirectAttributes) {
        reviewService.deleteOwnReview(request, reviewId);
        redirectAttributes.addFlashAttribute("success", "Review deleted.");
        return "redirect:/reviews";
    }

    // Adds the session token used to protect feedback forms.
    private void addCsrf(HttpServletRequest request, Model model) {
        model.addAttribute("csrfParameterName", CsrfTokenService.PARAMETER_NAME);
        model.addAttribute("csrfToken", csrfTokenService.getOrCreateToken(request));
    }

    // Offers completed lessons only when no review already refers to their ID.
    private List<Booking> unreviewedLessons(HttpServletRequest request, List<Review> reviews) {
        List<Booking> available = new ArrayList<>();
        for (Booking booking : bookingService.getOwnBookings(request)) {
            if (booking.getStatus() == BookingStatus.COMPLETED && !hasReview(booking.getId(), reviews)) {
                available.add(booking);
            }
        }
        return available;
    }

    // Includes hidden feedback so hiding a review never permits a duplicate submission.
    private boolean hasReview(String bookingId, List<Review> reviews) {
        for (Review review : reviews) {
            if (bookingId.equals(review.getBookingId())) {
                return true;
            }
        }
        return false;
    }
}
