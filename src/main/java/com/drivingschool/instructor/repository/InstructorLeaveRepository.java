/**
============================================================
MEMBER   : IT26101133 - [Name]
MODULE   : Instructors and bubble sort
FILE     : InstructorLeaveRepository.java
WHAT IT DOES (simple words): Saves and reads instructor leave records.
REAL-LIFE EXAMPLE: A teacher diary keeps teaching hours, lesson notes and profile details organised.
OOP USED : Inheritance (reuses FileRepository)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/
package com.drivingschool.instructor.repository;

import com.drivingschool.common.repository.FileRepository;
import com.drivingschool.instructor.model.InstructorLeave;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import java.nio.file.Path;
import java.util.List;

@Repository
// SOLID: S - This class is responsible for file storage.
public class InstructorLeaveRepository extends FileRepository<InstructorLeave> {
    // Receives the helpers or validates the details needed to create this object.
    public InstructorLeaveRepository(@Value("${app.data-directory:./data}") String dir, ObjectMapper mapper) {
        super(Path.of(dir), "instructor-leave.txt", InstructorLeave.class, mapper);
    }
    // OOP: INHERITANCE - This filing drawer reuses FileRepository's safe storage.
    // SOLID: L - Each override keeps the parent's validation and storage promises.
    @Override
    // Adds a new record using the shared safe file writer.
    // CRUD: CREATE - adds a new leave record and saves it
    public void save(InstructorLeave record) { super.save(record); }

    @Override
    // Reads the complete filing drawer, refusing damaged data before safety decisions.
    // CRUD: READ   - finds or lists leave record
    public List<InstructorLeave> readAllStrict() { return super.readAllStrict(); }

    @Override
    // Replaces the selected record while keeping its permanent ID.
    // CRUD: UPDATE - changes an existing leave record
    public void update(InstructorLeave record) { super.update(record); }

    @Override
    // Removes a record only after the service has checked its rules.
    // CRUD: DELETE - removes a leave record
    public void delete(String recordId) { super.delete(recordId); }
}
