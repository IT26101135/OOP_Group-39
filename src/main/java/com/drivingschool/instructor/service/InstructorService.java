/**
============================================================
MEMBER   : IT26101133 - [Name]
MODULE   : Instructors and bubble sort
FILE     : InstructorService.java
WHAT IT DOES (simple words): Applies the instructor rules.
REAL-LIFE EXAMPLE: A teacher diary keeps teaching hours, lesson notes and profile details organised.
OOP USED : Encapsulation (private fields and public methods)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/

package com.drivingschool.instructor.service;

import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.instructor.model.Instructor;
import com.drivingschool.instructor.repository.InstructorRepository;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

// SOLID: S - This class is responsible for application rules.
public class InstructorService implements InstructorCatalog {

    private final InstructorRepository repository;
    private final InstructorSorter sorter;

    // Receives the helpers or validates the details needed to create this object.
    public InstructorService(InstructorRepository repository) {
        this.repository = Objects.requireNonNull(
                repository, "repository");
        this.sorter = new InstructorSorter();
    }

    // Checks the teaching profile before saving the new instructor.
    // CRUD: CREATE - adds a new instructor profile and saves it
    public synchronized Instructor createInstructor(
            String name,
            String email,
            String phone,
            int yearsOfExperience) {

        return createInstructor(name, email, phone, yearsOfExperience,
                null, null, null, null, null);
    }

    // Checks the teaching profile before saving the new instructor.
    // CRUD: CREATE - adds a new instructor profile and saves it
    public synchronized Instructor createInstructor(
            String name, String email, String phone, int yearsOfExperience,
            String bio, String licenseNumber, String qualifications,
            String photoPath, List<String> vehicleTypes) {

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
        instructor.setBio(bio);
        instructor.setLicenseNumber(licenseNumber);
        instructor.setQualifications(qualifications);
        instructor.setPhotoPath(photoPath);
        instructor.setVehicleTypes(vehicleTypes);

        repository.save(instructor);
        return instructor;
    }

    // Lists instructor profiles for the management page.
    // CRUD: READ   - finds or lists instructor profiles
    public List<Instructor> getAllInstructors() {
        return repository.readAll();
    }

    // Removes the profile after the account service has checked attached history.
    // CRUD: DELETE - removes a instructor profile
    public void deleteInstructor(String id) {
        repository.delete(id);
    }

    // Changes whether this instructor can receive lessons.
    // CRUD: UPDATE - changes an existing instructor profile
    public synchronized Instructor setInstructorActive(String id, boolean active) {
        Instructor instructor = getInstructorById(id);
        instructor.setActive(active);
        repository.update(instructor);
        return instructor;
    }

    // Finds one profile or gives a friendly missing-record error.
    // CRUD: READ   - finds or lists instructor profiles
    public Instructor getInstructorById(String id) {
        return repository.findByIdStrict(id)
                .orElseThrow(() -> new RecordNotFoundException(
                        "This instructor is no longer listed. Return to the instructor list."));
    }

    // Builds a validated replacement so a bad edit cannot damage the saved profile.
    // CRUD: UPDATE - changes an existing instructor profile
    public synchronized Instructor updateInstructor(
            String id,
            String name,
            String email,
            String phone,
            int yearsOfExperience) {

        Instructor existing = getInstructorById(id);
        return updateInstructor(id, name, email, phone, yearsOfExperience,
                existing.getBio(), existing.getLicenseNumber(),
                existing.getQualifications(), existing.getPhotoPath(),
                existing.getVehicleTypes());
    }

    // Builds a validated replacement so a bad edit cannot damage the saved profile.
    // CRUD: UPDATE - changes an existing instructor profile
    public synchronized Instructor updateInstructor(
            String id, String name, String email, String phone,
            int yearsOfExperience, String bio, String licenseNumber,
            String qualifications, String photoPath, List<String> vehicleTypes) {

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
        updated.setBio(bio);
        updated.setLicenseNumber(licenseNumber);
        updated.setQualifications(qualifications);
        updated.setPhotoPath(photoPath);
        updated.setVehicleTypes(vehicleTypes);
        updated.setAverageRating(existing.getAverageRating());

        ensureEmailAvailable(
                updated.getEmail(),
                existing.getId()
        );

        repository.update(updated);
        return updated;
    }

    // Lets callers read the instructors sorted by experience without exposing the private field.
    public List<Instructor> getInstructorsSortedByExperience() {
        return sorter.sortByExperience(repository.readAll());
    }

    // Passes the chosen ordering rule to the hand-written bubble sort.
    public List<Instructor> sortInstructors(String sortBy, boolean ascending) {
        return sorter.sort(repository.readAll(), sortBy, ascending);
    }

    // Prevents two instructor profiles from sharing the same email address.
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
