/**
============================================================
MEMBER   : IT26101137 - [Name]
MODULE   : Reviews and Reports
FILE     : ReviewOperations.java
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

// OOP: ABSTRACTION - Feedback pages call these tasks without knowing how files are stored.
// SOLID: I - Student and public feedback tasks are separate from moderation tasks.
public interface ReviewOperations {
    // CRUD: READ   - finds or lists reviews
    // Lists feedback permitted for the signed-in role.
    List<Review> getVisibleReviews(HttpServletRequest request);
    // CRUD: READ   - finds or lists reviews
    // Lists approved feedback for the public page.
    List<Review> getApprovedReviews();
    // CRUD: CREATE - adds a new review and saves it
    // Checks ownership and completion before saving pending feedback.
    Review createReview(HttpServletRequest request, String bookingId, int rating, String comment);
    // CRUD: UPDATE - changes an existing review
    // Changes only feedback belonging to the signed-in student.
    Review updateOwnReview(HttpServletRequest request, String reviewId, int rating, String comment);
    // CRUD: DELETE - removes a review
    // Removes only feedback belonging to the signed-in student.
    void deleteOwnReview(HttpServletRequest request, String reviewId);
}
