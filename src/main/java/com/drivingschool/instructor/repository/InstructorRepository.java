
package com.drivingschool.instructor.repository;

import com.drivingschool.common.repository.FileRepository;
import com.drivingschool.instructor.model.Instructor;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;

public class InstructorRepository extends FileRepository<Instructor> {

    public InstructorRepository(
            Path dataDirectory,
            ObjectMapper objectMapper) {

        super(
                dataDirectory,
                "instructors.txt",
                Instructor.class,
                objectMapper
        );
    }
}
