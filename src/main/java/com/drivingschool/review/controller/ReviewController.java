package com.drivingschool.review.controller;

import com.drivingschool.auth.annotation.RequireLogin;
import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.review.form.ReviewForm;
import com.drivingschool.review.model.Review;
import com.drivingschool.review.service.ReviewService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequireLogin
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final AuthSessionService authSessionService;
    private final CsrfTokenService csrfTokenService;

    public ReviewController(
            ReviewService reviewService,
            AuthSessionService authSessionService,
            CsrfTokenService csrfTokenService) {

        this.reviewService = reviewService;
        this.authSessionService = authSessionService;
        this.csrfTokenService = csrfTokenService;
    }

    @InitBinder("reviewForm")
    public void configureBinding(WebDataBinder binder) {
        binder.setAllowedFields("rating", "comment");
        binder.setAutoGrowNestedPaths(false);
    }

    // Display approved reviews and the student's own reviews
    @GetMapping
    public String showReviews(
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        response.setHeader("Cache-Control", "no-store");

        User student = requireStudent(request);

        model.addAttribute(
                "reviews",
                reviewService.getVisibleReviews(request)
        );

        model.addAttribute("currentStudentId", student.getId());

        addCsrfAttributes(request, model);

        return "review/list";
    }

    // Display create review form
    @GetMapping("/create")
    public String showCreateForm(
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        response.setHeader("Cache-Control", "no-store");

        User student = requireStudent(request);

        boolean alreadyReviewed = reviewService.getVisibleReviews(request)
                .stream()
                .anyMatch(review ->
                        review.getStudentId().equals(student.getId()));

        if (alreadyReviewed) {
            return "redirect:/reviews";
        }

        model.addAttribute("reviewForm", new ReviewForm());

        return createPage(request, model);
    }

    // Create a new review
    @PostMapping("/create")
    public String createReview(
            @ModelAttribute("reviewForm") ReviewForm form,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model,
            RedirectAttributes redirectAttributes) {

        response.setHeader("Cache-Control", "no-store");

        requireStudent(request);

        if (bindingResult.hasErrors()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute("formError",
                    "Please enter a valid rating and comment.");
            return createPage(request, model);
        }

        try {
            reviewService.createReview(
                    request,
                    form.getRating(),
                    form.getComment()
            );

            redirectAttributes.addFlashAttribute(
                    "reviewSubmitted", true);

            return "redirect:/reviews";

        } catch (ValidationException | DuplicateRecordException exception) {

            response.setStatus(
                    exception instanceof DuplicateRecordException
                            ? HttpServletResponse.SC_CONFLICT
                            : HttpServletResponse.SC_BAD_REQUEST
            );

            model.addAttribute("formError", exception.getMessage());

            return createPage(request, model);
        }
    }

    // Display edit form
    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable String id,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        response.setHeader("Cache-Control", "no-store");

        Review review = requireOwnReview(request, id);

        ReviewForm form = new ReviewForm();
        form.setRating(review.getRating());
        form.setComment(review.getComment());

        model.addAttribute("reviewForm", form);
        model.addAttribute("reviewId", review.getId());

        return editPage(request, model);
    }

    // Update own review
    @PostMapping("/{id}/edit")
    public String updateReview(
            @PathVariable String id,
            @ModelAttribute("reviewForm") ReviewForm form,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model,
            RedirectAttributes redirectAttributes) {

        response.setHeader("Cache-Control", "no-store");

        requireOwnReview(request, id);

        if (bindingResult.hasErrors()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute("reviewId", id);
            model.addAttribute("formError",
                    "Please enter a valid rating and comment.");
            return editPage(request, model);
        }

        try {
            reviewService.updateOwnReview(
                    request,
                    id,
                    form.getRating(),
                    form.getComment()
            );

            redirectAttributes.addFlashAttribute(
                    "reviewUpdated", true);

            return "redirect:/reviews";

        } catch (ValidationException exception) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute("reviewId", id);
            model.addAttribute("formError", exception.getMessage());

            return editPage(request, model);
        }
    }

    // Delete own review
    @PostMapping("/{id}/delete")
    public String deleteReview(
            @PathVariable String id,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {

        response.setHeader("Cache-Control", "no-store");

        requireOwnReview(request, id);

        reviewService.deleteOwnReview(request, id);

        redirectAttributes.addFlashAttribute(
                "reviewDeleted", true);

        return "redirect:/reviews";
    }

    private User requireStudent(HttpServletRequest request) {

        User user = authSessionService.requireLogin(request);

        if (user.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only students can access this page."
            );
        }

        return user;
    }

    private Review requireOwnReview(
            HttpServletRequest request,
            String reviewId) {

        User student = requireStudent(request);

        Review review = reviewService
                .getVisibleReviewById(request, reviewId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Review not found."
                ));

        if (!review.getStudentId().equals(student.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Review not found."
            );
        }

        return review;
    }

    private String createPage(
            HttpServletRequest request,
            Model model) {

        addCsrfAttributes(request, model);
        return "review/create";
    }

    private String editPage(
            HttpServletRequest request,
            Model model) {

        addCsrfAttributes(request, model);
        return "review/edit";
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
