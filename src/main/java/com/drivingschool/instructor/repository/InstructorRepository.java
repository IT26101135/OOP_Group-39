/**
============================================================
MEMBER   : IT26101133 - [Name]
MODULE   : Instructors and bubble sort
FILE     : InstructorRepository.java
WHAT IT DOES (simple words): Saves and reads instructor records.
REAL-LIFE EXAMPLE: A teacher diary keeps teaching hours, lesson notes and profile details organised.
OOP USED : Inheritance (reuses FileRepository)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/

package com.drivingschool.instructor.repository;

import com.drivingschool.common.repository.FileRepository;
import com.drivingschool.instructor.model.Instructor;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.List;

// SOLID: S - This class is responsible for file storage.
public class InstructorRepository extends FileRepository<Instructor> {

    // Receives the helpers or validates the details needed to create this object.
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
    // OOP: INHERITANCE - This filing drawer reuses FileRepository's safe storage.
    // SOLID: L - Each override keeps the parent's validation and storage promises.
    @Override
    // Adds a new record using the shared safe file writer.
    // CRUD: CREATE - adds a new instructor profile and saves it
    public void save(Instructor record) { super.save(record); }

    @Override
    // Reads the complete filing drawer, refusing damaged data before safety decisions.
    // CRUD: READ   - finds or lists instructor profile
    public List<Instructor> readAllStrict() { return super.readAllStrict(); }

    @Override
    // Replaces the selected record while keeping its permanent ID.
    // CRUD: UPDATE - changes an existing instructor profile
    public void update(Instructor record) { super.update(record); }

    @Override
    // Removes a record only after the service has checked its rules.
    // CRUD: DELETE - removes a instructor profile
    public void delete(String recordId) { super.delete(recordId); }
}
