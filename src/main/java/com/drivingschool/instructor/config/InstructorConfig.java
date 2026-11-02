
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
    public InstructorRepository instructorRepository(
            @Value("${app.data-directory:./data}") String dataDirectory,
            ObjectMapper objectMapper) {

        return new InstructorRepository(
                Path.of(dataDirectory),
                objectMapper
        );
    }

    @Bean
    public InstructorService instructorService(
            InstructorRepository instructorRepository) {

        return new InstructorService(instructorRepository);
    }
}
