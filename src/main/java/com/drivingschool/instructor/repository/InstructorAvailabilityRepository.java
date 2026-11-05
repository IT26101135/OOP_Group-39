package com.drivingschool.instructor.repository;

import com.drivingschool.common.repository.FileRepository;
import com.drivingschool.instructor.model.InstructorAvailability;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;

public class InstructorAvailabilityRepository
        extends FileRepository<InstructorAvailability> {

    public InstructorAvailabilityRepository(
            Path dataDirectory,
            ObjectMapper objectMapper) {

        super(
                dataDirectory,
                "instructor-availability.txt",
                InstructorAvailability.class,
                objectMapper
        );
    }
}
