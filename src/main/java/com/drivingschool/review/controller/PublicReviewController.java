/**
============================================================
MEMBER   : IT26101137 - [Name]
MODULE   : Reviews and Reports
FILE     : PublicReviewController.java
WHAT IT DOES (simple words): Keeps reviews and reports details clear and safe for the school.
REAL-LIFE EXAMPLE: Like a feedback book and a school manager counting its records.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for reviews; reports READ saved data
============================================================
*/
package com.drivingschool.review.controller;

import com.drivingschool.review.service.ReviewOperations;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/public/reviews")
public class PublicReviewController {

    private final ReviewOperations reviews;

    // Receives the details this class needs and keeps them ready for its own job.
    public PublicReviewController(ReviewOperations reviews) {
        this.reviews = reviews;
    }

    // Loads approved feedback only for the public review page.
    // CRUD: READ   - finds or lists reviews
    @GetMapping
    public String approved(HttpServletResponse response, Model model) {
        response.setHeader("Cache-Control", "no-store");
        model.addAttribute("reviews", reviews.getApprovedReviews());
        return "public/reviews";
    }
}
