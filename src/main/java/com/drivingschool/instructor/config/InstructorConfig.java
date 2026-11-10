/**
============================================================
MEMBER   : IT26101133 - [Name]
MODULE   : Instructors and bubble sort
FILE     : InstructorConfig.java
WHAT IT DOES (simple words): Connects the helpers used by instructor config.
REAL-LIFE EXAMPLE: A teacher diary keeps teaching hours, lesson notes and profile details organised.
OOP USED : Encapsulation (private fields and public methods)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/

package com.drivingschool.instructor.config;

import com.drivingschool.instructor.repository.InstructorRepository;
import com.drivingschool.instructor.service.InstructorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class InstructorConfig {

    @Bean
    // Keeps the instructor repository step in one place for this module.
    public InstructorRepository instructorRepository(
            @Value("${app.data-directory:./data}") String dataDirectory,
            ObjectMapper objectMapper) {

        return new InstructorRepository(
                Path.of(dataDirectory),
                objectMapper
        );
    }

    @Bean
    // Keeps the instructor service step in one place for this module.
    public InstructorService instructorService(
            InstructorRepository instructorRepository) {

        return new InstructorService(instructorRepository);
    }

}
