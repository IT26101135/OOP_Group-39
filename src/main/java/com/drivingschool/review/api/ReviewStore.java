// Module owner: IT26101137
package com.drivingschool.review.api;

import com.drivingschool.review.model.Review;

import java.util.List;
import java.util.Optional;

/** The review storage operations used by the review service. */
public interface ReviewStore {

    List<Review> readAllStrict();

    Optional<Review> findByIdStrict(String id);

    String nextId(String prefix);

    void save(Review review);

    void update(Review review);

    void delete(String id);
}
