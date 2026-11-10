/**
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages
FILE     : LearningPackageLifecycleTest.java
WHAT IT DOES (simple words): Checks full package CRUD and protects referenced course history.
REAL-LIFE EXAMPLE: Keeping an old course brochure when a student's receipt still refers to it.
OOP USED : Encapsulation, repositories and custom exceptions
CRUD     : CREATE, READ, UPDATE, DELETE
============================================================
*/
package com.drivingschool.learningpackage.service;

import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.learningpackage.model.Enrollment;
import com.drivingschool.learningpackage.model.LearningPackage;
import com.drivingschool.learningpackage.model.PackageType;
import com.drivingschool.learningpackage.repository.EnrollmentRepository;
import com.drivingschool.learningpackage.repository.LearningPackageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class LearningPackageLifecycleTest {
    @TempDir Path folder;
    private LearningPackageRepository packages;
    private EnrollmentRepository enrollments;
    private BookingRepository bookings;
    private LearningPackageService service;
    private final LocalDateTime lessonTime = LocalDateTime.of(2026, 1, 1, 9, 0);

    // Uses actual temporary files to check the service rules without touching demo or project data.
    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        packages = new LearningPackageRepository(folder, mapper);
        enrollments = new EnrollmentRepository(folder.toString(), mapper);
        bookings = new BookingRepository(folder.toString(), mapper);
        service = new LearningPackageService(packages, bookings, enrollments);
    }

    // A complete unused package can be created, read, changed and safely removed.
    @Test
    void supportsFullPackageCrudAndSavedMetadata() {
        LearningPackage course = createCourse();
        assertEquals(course.getName(), service.findPackageById(course.getId()).orElseThrow().getName());
        PackageType type = PackageType.values()[0];
        service.updatePackage(course.getId(), "Updated course", "Updated practical driving course",
                new BigDecimal("2500.00"), 6, 45, type, 8);
        LearningPackage saved = packages.findByIdStrict(course.getId()).orElseThrow();
        assertEquals("Updated course", saved.getName());
        assertEquals(type, saved.getType());
        assertEquals(8, saved.getDurationHours());
        service.deletePackage(course.getId());
        assertTrue(packages.findByIdStrict(course.getId()).isEmpty());
    }

    // Any enrollment refers to the old purchase details, so deleting its course must fail.
    @Test
    void enrollmentHistoryPreventsPackageDeletion() {
        LearningPackage course = createCourse();
        enrollments.save(new Enrollment("E001", "S001", course.getId(), lessonTime, course.getPrice(), "PAID"));
        assertThrows(ValidationException.class, () -> service.deletePackage(course.getId()));
        assertTrue(packages.findByIdStrict(course.getId()).isPresent());
    }

    // Older lesson files may carry a package ID directly; compatibility history stays protected.
    @Test
    void legacyLessonReferencePreventsPackageDeletion() {
        LearningPackage course = createCourse();
        bookings.save(new Booking("B001", "S001", course.getId(), lessonTime, lessonTime.plusHours(1),
                null, null, BookingStatus.CANCELLED, lessonTime.minusDays(1)));
        assertThrows(ValidationException.class, () -> service.deletePackage(course.getId()));
        assertTrue(packages.findByIdStrict(course.getId()).isPresent());
    }

    // Missing and malformed targets use the existing custom exceptions instead of deleting silently.
    @Test
    void missingAndMalformedDeleteTargetsAreRejected() {
        assertThrows(RecordNotFoundException.class, () -> service.deletePackage("P999"));
        assertThrows(ValidationException.class, () -> service.deletePackage("bad"));
    }

    // Makes a simple valid course for each lifecycle scenario.
    private LearningPackage createCourse() {
        return service.createPackage("Practice course", "Practical driving lessons", new BigDecimal("1000.00"), 5, 30, true);
    }
}
