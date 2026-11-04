package com.drivingschool.learningpackage.service;

import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.learningpackage.model.LearningPackage;
import com.drivingschool.learningpackage.repository.LearningPackageRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class LearningPackageService {

    private static final Object PACKAGE_WRITE_LOCK = new Object();

    private final LearningPackageRepository repository;

    public LearningPackageService(LearningPackageRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException(
                    "Learning package repository is required.");
        }

        this.repository = repository;
    }

    public LearningPackage createPackage(
            String name,
            String description,
            BigDecimal price,
            int lessonCount,
            int validityDays,
            boolean active) {

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

            repository.save(newPackage);

            return newPackage;
        }
    }

    public List<LearningPackage> listPackages() {
        return repository.readAllStrict();
    }

    public Optional<LearningPackage> findPackageById(String id) {
        validatePackageId(id);
        return repository.findByIdStrict(id);
    }

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

    private static void validatePackageId(String id) {
        if (id == null || !id.matches("P(?!000)[0-9]{3}")) {
            throw new ValidationException(
                    "Package ID must be between P001 and P999.");
        }
    }

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

    private static String normalizeName(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
