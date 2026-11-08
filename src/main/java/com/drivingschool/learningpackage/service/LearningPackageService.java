/**
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages and enrollment
FILE     : LearningPackageService.java
WHAT IT DOES (simple words): Applies the rules for learning package.
REAL-LIFE EXAMPLE: A course folder keeps the learning plan and enrollment details together.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.learningpackage.service;

import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.learningpackage.repository.EnrollmentRepository;
import com.drivingschool.learningpackage.model.LearningPackage;
import com.drivingschool.learningpackage.model.PackageType;
import com.drivingschool.learningpackage.repository.LearningPackageRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

// SOLID: S - This class is responsible for application rules.
public class LearningPackageService implements LearningPackageCatalog {

    private static final Object PACKAGE_WRITE_LOCK = new Object();

    private final LearningPackageRepository repository;
    private final BookingRepository bookings;
    private final EnrollmentRepository enrollments;

    // Receives the helpers this class needs so each job stays separate.
    public LearningPackageService(LearningPackageRepository repository) {
        this(repository, null, null);
    }

    // Receives the helpers this class needs so each job stays separate.
    public LearningPackageService(LearningPackageRepository repository,
                                  BookingRepository bookings) {
        this(repository, bookings, null);
    }

    // Receives the helpers this class needs so each job stays separate.
    public LearningPackageService(LearningPackageRepository repository,
                                  BookingRepository bookings,
                                  EnrollmentRepository enrollments) {
        if (repository == null) {
            throw new IllegalArgumentException(
                    "Learning package repository is required.");
        }

        this.repository = repository;
        this.bookings = bookings;
        this.enrollments = enrollments;
    }

    // Handles create package for this class without changing another class's job.
    // CRUD: CREATE - adds a new package and saves it
    public LearningPackage createPackage(
            String name,
            String description,
            BigDecimal price,
            int lessonCount,
            int validityDays,
            boolean active) {

        return createPackage(name, description, price, lessonCount,
                validityDays, active, PackageType.MANUAL, lessonCount);
    }

    // Handles create package for this class without changing another class's job.
    // CRUD: CREATE - adds a new package and saves it
    public LearningPackage createPackage(
            String name,
            String description,
            BigDecimal price,
            int lessonCount,
            int validityDays,
            boolean active,
            PackageType type,
            int durationHours) {

        synchronized (PACKAGE_WRITE_LOCK) {

            // Validate before allocating an ID.
            LearningPackage validatedPackage = new LearningPackage(
                    "P001",
                    name,
                    description,
                    price,
                    lessonCount,
                    validityDays,
                    active
            );
            validatedPackage.setType(type);
            validatedPackage.setDurationHours(durationHours);

            List<LearningPackage> packages =
                    repository.readAllStrict();

            ensureUniqueName(packages, validatedPackage.getName(), null);

            String id = repository.nextId("P");

            LearningPackage newPackage = new LearningPackage(
                    id,
                    validatedPackage.getName(),
                    validatedPackage.getDescription(),
                    validatedPackage.getPrice(),
                    validatedPackage.getLessonCount(),
                    validatedPackage.getValidityDays(),
                    validatedPackage.isActive()
            );
            newPackage.setType(validatedPackage.getType());
            newPackage.setDurationHours(validatedPackage.getDurationHours());

            repository.save(newPackage);

            return newPackage;
        }
    }

    // Handles list packages for this class without changing another class's job.
    // CRUD: READ   - finds or lists packages
    public List<LearningPackage> listPackages() {
        return repository.readAllStrict();
    }

    // Handles find package by id for this class without changing another class's job.
    // CRUD: READ   - finds or lists packages
    public Optional<LearningPackage> findPackageById(String id) {
        validatePackageId(id);
        return repository.findByIdStrict(id);
    }

    // Handles update package metadata for this class without changing another class's job.
    // CRUD: UPDATE - changes an existing package
    public LearningPackage updatePackageMetadata(
            String id,
            String name,
            String description) {

        synchronized (PACKAGE_WRITE_LOCK) {

            validatePackageId(id);

            LearningPackage currentPackage =
                    repository.findByIdStrict(id)
                            .orElseThrow(() ->
                                    new RecordNotFoundException(
                                            "Learning package not found: " + id));

            // Construct a replacement so invalid updates cannot
            // partially modify the existing package.
            LearningPackage updatedPackage = new LearningPackage(
                    id,
                    name,
                    description,
                    currentPackage.getPrice(),
                    currentPackage.getLessonCount(),
                    currentPackage.getValidityDays(),
                    currentPackage.isActive()
            );
            updatedPackage.setType(currentPackage.getType());
            updatedPackage.setDurationHours(currentPackage.getDurationHours());

            List<LearningPackage> packages =
                    repository.readAllStrict();

            ensureUniqueName(
                    packages,
                    updatedPackage.getName(),
                    id
            );

            repository.update(updatedPackage);

            return updatedPackage;
        }
    }

    // Handles update package for this class without changing another class's job.
    // CRUD: UPDATE - changes an existing package
    public LearningPackage updatePackage(String id, String name,
                                         String description, BigDecimal price,
                                         int lessonCount, int validityDays,
                                         PackageType type, int durationHours) {
        synchronized (PACKAGE_WRITE_LOCK) {
            validatePackageId(id);
            LearningPackage current = repository.findByIdStrict(id)
                    .orElseThrow(() -> new RecordNotFoundException(
                            "Learning package not found: " + id));
            LearningPackage replacement = new LearningPackage(id, name,
                    description, price, lessonCount, validityDays,
                    current.isActive());
            replacement.setType(type);
            replacement.setDurationHours(durationHours);
            ensureUniqueName(repository.readAllStrict(), replacement.getName(), id);
            repository.update(replacement);
            return replacement;
        }
    }

    // Stores the package active after the checks required by this record.
    // CRUD: UPDATE - changes an existing package
    public LearningPackage setPackageActive(String id, boolean active) {
        synchronized (PACKAGE_WRITE_LOCK) {
            validatePackageId(id);
            LearningPackage learningPackage = repository.findByIdStrict(id)
                    .orElseThrow(() -> new RecordNotFoundException(
                            "Learning package not found: " + id));
            learningPackage.setActive(active);
            repository.update(learningPackage);
            return learningPackage;
        }
    }

    // Handles delete package for this class without changing another class's job.
    // CRUD: DELETE - removes a package
    public void deletePackage(String id) {
        synchronized (PACKAGE_WRITE_LOCK) {
            validatePackageId(id);
            repository.findByIdStrict(id).orElseThrow(() ->
                    new RecordNotFoundException("Learning package not found: " + id));
            // Older booking files may hold a package ID in the enrollment field.
            // Preserve that compatibility check as well as current enrollment links.
            if (bookings != null && bookings.readAllStrict().stream()
                    .anyMatch(booking -> id.equals(booking.getEnrollmentId()))) {
                throw new ValidationException(
                        "Package cannot be deleted because lesson history refers to it.");
            }
            if (enrollments != null && enrollments.readAllStrict().stream()
                    .anyMatch(enrollment -> id.equals(enrollment.getPackageId()))) {
                throw new ValidationException(
                        "Package cannot be deleted because an enrollment refers to it.");
            }
            repository.delete(id);
        }
    }

    // Stops the operation if validate package id rules are not met.
    private static void validatePackageId(String id) {
        if (id == null || !id.matches("P(?!000)[0-9]{3}")) {
            throw new ValidationException(
                    "Package ID must be between P001 and P999.");
        }
    }

    // Stops the operation if ensure unique name rules are not met.
    private static void ensureUniqueName(
            List<LearningPackage> packages,
            String name,
            String excludedId) {

        String normalizedName = normalizeName(name);

        for (LearningPackage existingPackage : packages) {

            if (excludedId != null
                    && excludedId.equals(existingPackage.getId())) {
                continue;
            }

            if (normalizeName(existingPackage.getName())
                    .equals(normalizedName)) {

                throw new DuplicateRecordException(
                        "A learning package with this name already exists.");
            }
        }
    }

    // Handles normalize name for this class without changing another class's job.
    private static String normalizeName(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
