package com.drivingschool.review.repository;

import com.drivingschool.common.repository.FileRepository;
import com.drivingschool.review.model.Review;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;

@Repository
public class ReviewRepository extends FileRepository<Review> {

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
}