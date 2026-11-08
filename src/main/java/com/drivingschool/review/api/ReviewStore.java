/**
============================================================
MEMBER   : IT26101137 - [Name]
MODULE   : Reviews and Reports
FILE     : ReviewStore.java
WHAT IT DOES (simple words): Keeps reviews and reports details clear and safe for the school.
REAL-LIFE EXAMPLE: Like a feedback book and a school manager counting its records.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for reviews; reports READ saved data
============================================================
*/
package com.drivingschool.review.api;

import com.drivingschool.review.model.Review;

import java.util.List;
import java.util.Optional;

/** The review storage operations used by the review service. */
public interface ReviewStore {

    // CRUD: READ   - finds or lists reviews
    // Reads the full file and refuses damaged data before making decisions.
    List<Review> readAllStrict();

    // CRUD: READ   - finds or lists reviews
    // Looks up one saved record without hiding file damage.
    Optional<Review> findByIdStrict(String id);

    // Asks storage for the next unused permanent record ID.
    String nextId(String prefix);

    // CRUD: CREATE - adds a new review and saves it
    // Adds the new record after service validation.
    void save(Review review);

    // CRUD: UPDATE - changes an existing review
    // Replaces the saved record after service validation.
    void update(Review review);

    // CRUD: DELETE - removes a review
    // Removes the saved record after service validation.
    void delete(String id);
}
