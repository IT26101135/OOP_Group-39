
package com.drivingschool.instructor.service;

import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.instructor.model.Instructor;
import com.drivingschool.instructor.repository.InstructorRepository;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class InstructorService {

    private final InstructorRepository repository;
    private final InstructorSorter sorter;

    public InstructorService(InstructorRepository repository) {
        this.repository = Objects.requireNonNull(
                repository, "repository");
        this.sorter = new InstructorSorter();
    }

    public synchronized Instructor createInstructor(
            String name,
            String email,
            String phone,
            int yearsOfExperience) {

        // Validate fields before allocating an ID.
        Instructor validated = new Instructor(
                "I001",
                name,
                email,
                phone,
                yearsOfExperience,
                true
        );

        ensureEmailAvailable(validated.getEmail(), null);

        String id = repository.nextId("I");

        Instructor instructor = new Instructor(
                id,
                validated.getName(),
                validated.getEmail(),
                validated.getPhone(),
                validated.getYearsOfExperience(),
                true
        );

        repository.save(instructor);
        return instructor;
    }

    public List<Instructor> getAllInstructors() {
        return repository.readAll();
    }

    public Instructor getInstructorById(String id) {
        return repository.findByIdStrict(id)
                .orElseThrow(() -> new RecordNotFoundException(
                        "Instructor not found: " + id));
    }

    public synchronized Instructor updateInstructor(
            String id,
            String name,
            String email,
            String phone,
            int yearsOfExperience) {

        Instructor existing = getInstructorById(id);

        // Create a validated replacement before updating storage.
        Instructor updated = new Instructor(
                existing.getId(),
                name,
                email,
                phone,
                yearsOfExperience,
                existing.isActive()
        );

        ensureEmailAvailable(
                updated.getEmail(),
                existing.getId()
        );

        repository.update(updated);
        return updated;
    }

    public List<Instructor> getInstructorsSortedByExperience() {
        return sorter.sortByExperience(repository.readAll());
    }

    private void ensureEmailAvailable(
            String email,
            String excludedInstructorId) {

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        for (Instructor instructor : repository.readAllStrict()) {

            if (instructor.getId().equals(excludedInstructorId)) {
                continue;
            }

            String storedEmail = instructor.getEmail()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            if (storedEmail.equals(normalizedEmail)) {
                throw new DuplicateRecordException(
                        "Instructor email already exists.");
            }
        }
    }
}
