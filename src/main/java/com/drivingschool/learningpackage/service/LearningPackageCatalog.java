/**
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages and enrollment
FILE     : LearningPackageCatalog.java
WHAT IT DOES (simple words): Applies the rules for learning package catalog.
REAL-LIFE EXAMPLE: A course folder keeps the learning plan and enrollment details together.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.learningpackage.service;

import com.drivingschool.learningpackage.model.LearningPackage;
import com.drivingschool.learningpackage.model.PackageType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

// OOP: ABSTRACTION - Pages depend on this promise rather than file details.
// SOLID: I - This contract exposes only operations for one kind of caller.
public interface LearningPackageCatalog {
    // Lists the course choices the page may display.
    List<LearningPackage> listPackages();
    // Looks up a course using its saved ID.
    Optional<LearningPackage> findPackageById(String id);
    // Adds a validated course to the school catalog.
    LearningPackage createPackage(String name, String description, BigDecimal price,
            int lessonCount, int validityDays, boolean active, PackageType type, int durationHours);
    // Changes a course without replacing its identity.
    LearningPackage updatePackage(String id, String name, String description, BigDecimal price,
            int lessonCount, int validityDays, PackageType type, int durationHours);
    // Controls whether a course can be chosen for a new enrollment.
    LearningPackage setPackageActive(String id, boolean active);
    // Removes a course only when history does not depend on it.
    void deletePackage(String id);
}
