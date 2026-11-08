// Module owner: IT26101133
package com.drivingschool.instructor.repository;

import com.drivingschool.common.repository.FileRepository;
import com.drivingschool.instructor.model.LessonRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import java.nio.file.Path;

@Repository
public class LessonRecordRepository extends FileRepository<LessonRecord> {
    public LessonRecordRepository(@Value("${app.data-directory:./data}") String dir, ObjectMapper mapper) {
        super(Path.of(dir), "lesson-records.txt", LessonRecord.class, mapper);
    }
}
