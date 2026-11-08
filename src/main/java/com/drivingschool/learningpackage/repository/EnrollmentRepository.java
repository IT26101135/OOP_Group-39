// Module owner: IT26101136
package com.drivingschool.learningpackage.repository;

import com.drivingschool.common.repository.FileRepository;
import com.drivingschool.learningpackage.model.Enrollment;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;

@Repository
public class EnrollmentRepository extends FileRepository<Enrollment> {
    public EnrollmentRepository(@Value("${app.data-directory:./data}") String directory,
                                ObjectMapper mapper) {
        super(Path.of(directory), "enrollments.txt", Enrollment.class, mapper);
    }
}
