/**
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages and enrollment
FILE     : LearningPackageRepository.java
WHAT IT DOES (simple words): Keeps learning package records in files.
REAL-LIFE EXAMPLE: A course folder keeps the learning plan and enrollment details together.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.learningpackage.repository;

import com.drivingschool.common.repository.FileRepository;
import com.drivingschool.learningpackage.model.LearningPackage;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.List;

// SOLID: S - This class is responsible for saving and reading files.
public class LearningPackageRepository
        extends FileRepository<LearningPackage> {

    // Receives the helpers this class needs so each job stays separate.
    public LearningPackageRepository(
            Path dataDirectory,
            ObjectMapper objectMapper) {

        super(
                dataDirectory,
                "packages.txt",
                LearningPackage.class,
                objectMapper
        );
    }
    // OOP: INHERITANCE - This drawer reuses FileRepository's safe read and write rules.
    // SOLID: L - It keeps the parent's validation, missing-record and storage behaviour.
    // OOP: POLYMORPHISM - The shared save/read contract handles this module's record type.
    // CRUD: CREATE - adds a new package and saves it
    @Override
    // Passes a new account to the shared safe file writer.
    public void save(LearningPackage record) { super.save(record); }

    // CRUD: READ   - finds or lists package records
    @Override
    // Reads all accounts, refusing damaged rows before making safety decisions.
    public List<LearningPackage> readAllStrict() { return super.readAllStrict(); }

    // CRUD: UPDATE - changes an existing package
    @Override
    // Saves valid changes while preserving the identity of the existing record.
    public void update(LearningPackage record) { super.update(record); }

    // CRUD: DELETE - removes a package
    @Override
    // Checks the operation rules before removing the selected record.
    public void delete(String recordId) { super.delete(recordId); }
}
