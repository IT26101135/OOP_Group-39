/**
============================================================
MEMBER   : IT26101137 - [Name]
MODULE   : Reviews and Reports
FILE     : ReviewModeration.java
WHAT IT DOES (simple words): Keeps reviews and reports details clear and safe for the school.
REAL-LIFE EXAMPLE: Like a feedback book and a school manager counting its records.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for reviews; reports READ saved data
============================================================
*/
package com.drivingschool.review.service;
import com.drivingschool.review.model.Review;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

// SOLID: I - Only the admin controller needs moderation operations.
public interface ReviewModeration {
    // CRUD: READ   - finds or lists reviews
    // Lists feedback permitted for the signed-in role.
    List<Review> getVisibleReviews(HttpServletRequest request);
    // CRUD: UPDATE - changes an existing review
    // Allows an administrator to publish the selected feedback.
    Review approveReview(HttpServletRequest request, String reviewId);
    // CRUD: UPDATE - changes an existing review
    // Allows an administrator to hide feedback from the public page.
    Review hideReview(HttpServletRequest request, String reviewId);
    // CRUD: DELETE - removes a review
    // Allows an administrator to remove the selected feedback.
    void deleteReview(HttpServletRequest request, String reviewId);
}
