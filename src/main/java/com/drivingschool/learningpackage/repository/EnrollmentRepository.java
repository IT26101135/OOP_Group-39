/**
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages and enrollment
FILE     : EnrollmentRepository.java
WHAT IT DOES (simple words): Keeps enrollment records in files.
REAL-LIFE EXAMPLE: A course folder keeps the learning plan and enrollment details together.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.learningpackage.repository;

import com.drivingschool.common.repository.FileRepository;
import com.drivingschool.learningpackage.model.Enrollment;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.List;

@Repository
// SOLID: S - This class is responsible for saving and reading files.
public class EnrollmentRepository extends FileRepository<Enrollment> {
    // Receives the helpers this class needs so each job stays separate.
    public EnrollmentRepository(@Value("${app.data-directory:./data}") String directory,
                                ObjectMapper mapper) {
        super(Path.of(directory), "enrollments.txt", Enrollment.class, mapper);
    }
    // OOP: INHERITANCE - This drawer reuses FileRepository's safe read and write rules.
    // SOLID: L - It keeps the parent's validation, missing-record and storage behaviour.
    // OOP: POLYMORPHISM - The shared save/read contract handles this module's record type.
    // CRUD: CREATE - adds a new enrollment and saves it
    @Override
    // Passes a new account to the shared safe file writer.
    public void save(Enrollment record) { super.save(record); }

    // CRUD: READ   - finds or lists enrollment records
    @Override
    // Reads all accounts, refusing damaged rows before making safety decisions.
    public List<Enrollment> readAllStrict() { return super.readAllStrict(); }

    // CRUD: UPDATE - changes an existing enrollment
    @Override
    // Saves valid changes while preserving the identity of the existing record.
    public void update(Enrollment record) { super.update(record); }

    // CRUD: DELETE - removes a enrollment
    @Override
    // Checks the operation rules before removing the selected record.
    public void delete(String recordId) { super.delete(recordId); }
}
