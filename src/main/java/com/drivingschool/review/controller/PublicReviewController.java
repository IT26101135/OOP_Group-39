// Module owner: IT26101137
package com.drivingschool.review.controller;

import com.drivingschool.review.model.ReviewStatus;
import com.drivingschool.review.repository.ReviewRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/public/reviews")
public class PublicReviewController {

    private final ReviewRepository reviews;

    public PublicReviewController(ReviewRepository reviews) {
        this.reviews = reviews;
    }

    @GetMapping
    public String approved(HttpServletResponse response, Model model) {
        response.setHeader("Cache-Control", "no-store");
        model.addAttribute("reviews", reviews.readAllStrict().stream()
                .filter(review -> review.getStatus() == ReviewStatus.APPROVED)
                .toList());
        return "public/reviews";
    }
}
