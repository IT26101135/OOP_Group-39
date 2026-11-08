// Module owner: IT26101133
package com.drivingschool.instructor.repository;

import com.drivingschool.common.repository.FileRepository;
import com.drivingschool.instructor.model.Availability;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import java.nio.file.Path;

@Repository
public class AvailabilityRepository extends FileRepository<Availability> {
    public AvailabilityRepository(@Value("${app.data-directory:./data}") String dir, ObjectMapper mapper) {
        super(Path.of(dir), "availability.txt", Availability.class, mapper);
    }
}
