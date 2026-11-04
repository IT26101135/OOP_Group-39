package com.drivingschool.learningpackage.config;

import com.drivingschool.learningpackage.repository.LearningPackageRepository;
import com.drivingschool.learningpackage.service.LearningPackageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class LearningPackageConfig {

    @Bean
    public LearningPackageRepository learningPackageRepository(
            @Value("${app.data-directory:./data}") String dataDirectory,
            ObjectMapper objectMapper) {

        return new LearningPackageRepository(
                Path.of(dataDirectory),
                objectMapper
        );
    }

    @Bean
    public LearningPackageService learningPackageService(
            LearningPackageRepository learningPackageRepository) {

        return new LearningPackageService(learningPackageRepository);
    }
}
