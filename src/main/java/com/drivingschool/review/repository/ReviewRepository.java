/**
============================================================
MEMBER   : IT26101137 - [Name]
MODULE   : Reviews and Reports
FILE     : ReviewRepository.java
WHAT IT DOES (simple words): Keeps reviews and reports details clear and safe for the school.
REAL-LIFE EXAMPLE: Like a feedback book and a school manager counting its records.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for reviews; reports READ saved data
============================================================
*/
package com.drivingschool.review.repository;

import com.drivingschool.common.repository.FileRepository;
import com.drivingschool.review.api.ReviewStore;
import com.drivingschool.review.model.Review;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;

@Repository
// OOP: INHERITANCE - ReviewRepository reuses the FileRepository storage parent.
// SOLID: O - Select a new record file with a child repository instead of rewriting shared storage.
// SOLID: L - This child keeps the parent storage contract for every caller.
public class ReviewRepository extends FileRepository<Review> implements ReviewStore {

    // Receives the details this class needs and keeps them ready for its own job.
    public ReviewRepository(
            @Value("${app.data-directory:./data}") String dataDirectory,
            ObjectMapper objectMapper) {

        super(
                Path.of(dataDirectory),
                "reviews.txt",
                Review.class,
                objectMapper
        );
    }

    // CRUD: CREATE - adds a new review and saves it
    // OOP: POLYMORPHISM - Review storage overrides the same save operation as other filing drawers.
    // Adds the new record through the shared safe file writer.
    @Override
    public void save(Review review) { super.save(review); }

    // CRUD: READ   - finds or lists reviews
    // Reads all records and refuses damaged data before a safety decision.
    @Override
    public java.util.List<Review> readAllStrict() { return super.readAllStrict(); }

    // CRUD: UPDATE - changes an existing review
    // Replaces an existing record after service checks have passed.
    @Override
    public void update(Review review) { super.update(review); }

    // CRUD: DELETE - removes a review
    // Removes the chosen record after service checks have passed.
    @Override
    public void delete(String id) { super.delete(id); }
}
