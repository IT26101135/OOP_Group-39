/**
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages and enrollment
FILE     : LearningPackageConfig.java
WHAT IT DOES (simple words): Supports learning package config for the account or course workflow.
REAL-LIFE EXAMPLE: A course folder keeps the learning plan and enrollment details together.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.learningpackage.config;

import com.drivingschool.learningpackage.repository.LearningPackageRepository;
import com.drivingschool.learningpackage.service.LearningPackageService;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.learningpackage.repository.EnrollmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class LearningPackageConfig {

    @Bean
    // Handles learning package repository for this class without changing another class's job.
    public LearningPackageRepository learningPackageRepository(
            @Value("${app.data-directory:./data}") String dataDirectory,
            ObjectMapper objectMapper) {

        return new LearningPackageRepository(
                Path.of(dataDirectory),
                objectMapper
        );
    }

    // Spring's ObjectProvider finds optional repositories without starting a dependency loop.
    // Older unit tests can construct the service without those optional history checks.
    @Bean
    // Handles learning package service for this class without changing another class's job.
    public LearningPackageService learningPackageService(
            LearningPackageRepository learningPackageRepository,
            org.springframework.beans.factory.ObjectProvider<BookingRepository> bookings,
            org.springframework.beans.factory.ObjectProvider<EnrollmentRepository> enrollments) {

        return new LearningPackageService(learningPackageRepository,
                bookings.getIfAvailable(), enrollments.getIfAvailable());
    }
}
