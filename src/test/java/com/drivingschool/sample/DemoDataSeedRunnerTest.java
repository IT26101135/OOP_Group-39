/**
 * ============================================================
 * MEMBER   : IT26101132 - [Name]
 * MODULE   : Sample data
 * FILE     : DemoDataSeedRunnerTest.java
 * WHAT IT DOES (simple words): Checks a demo restart does not duplicate records or decisions.
 * REAL-LIFE EXAMPLE: Reopening the office does not issue yesterday's receipts again.
 * OOP USED : Encapsulation and repository inheritance
 * CRUD     : CREATE and READ temporary sample data
 * ============================================================
 */
package com.drivingschool.sample;

import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.util.PasswordHasher;
import com.drivingschool.learningpackage.model.Enrollment;
import com.drivingschool.student.model.Student;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.instructor.repository.*;
import com.drivingschool.learningpackage.repository.*;
import com.drivingschool.review.repository.ReviewRepository;
import com.drivingschool.vehicle.repository.VehicleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class DemoDataSeedRunnerTest {
    @TempDir Path folder;
    // Runs the loader twice and checks all saved examples still match their original links.
    @Test void seedCanRunTwiceWithoutDuplicateRecords() {
        // Step 1: Prepare spare filing drawers instead of the real coursework records.
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        UserRepository users = new UserRepository(folder.toString(), mapper);
        InstructorRepository instructors = new InstructorRepository(folder, mapper);
        LearningPackageRepository packages = new LearningPackageRepository(folder, mapper);
        EnrollmentRepository enrollments = new EnrollmentRepository(folder.toString(), mapper);
        VehicleRepository vehicles = new VehicleRepository(folder, mapper);
        BookingRepository bookings = new BookingRepository(folder.toString(), mapper);
        ReviewRepository reviews = new ReviewRepository(folder.toString(), mapper);
        AvailabilityRepository availability = new AvailabilityRepository(folder.toString(), mapper);
        DemoDataSeedRunner seed = new DemoDataSeedRunner(users, instructors, packages, enrollments,
                vehicles, bookings, reviews, availability, new PasswordHasher());
        seed.run();
        seed.run();
        // Step 2: Check original links and counts plus all three payment examples.
        assertEquals(16, users.readAllStrict().size());
        assertEquals(5, instructors.readAllStrict().size());
        assertEquals(6, packages.readAllStrict().size());
        assertEquals(6, vehicles.readAllStrict().size());
        assertEquals(9, enrollments.readAllStrict().size());
        assertEquals("AWAITING_PAYMENT", enrollments.findByIdStrict("E008").orElseThrow().getStatus());
        assertEquals("CANCELLED", enrollments.findByIdStrict("E009").orElseThrow().getStatus());
        assertEquals(0, ((com.drivingschool.student.model.Student) users.findByIdStrict("S008").orElseThrow()).getLessonsRemaining());
        assertEquals(15, bookings.readAllStrict().size());
        assertEquals(10, reviews.readAllStrict().size());
        assertEquals(5, instructors.readAllStrict().stream().map(com.drivingschool.instructor.model.Instructor::getAverageRating).distinct().count());
        assertEquals(5, bookings.readAllStrict().stream().filter(b -> b.getStatus() == com.drivingschool.booking.model.BookingStatus.COMPLETED).count());
        assertEquals(5, reviews.readAllStrict().stream().filter(r -> r.getStatus() == com.drivingschool.review.model.ReviewStatus.APPROVED).count());
        for (var review : reviews.readAllStrict().stream().filter(r -> r.getBookingId() != null).toList()) {
            var booking = bookings.findByIdStrict(review.getBookingId()).orElseThrow();
            assertEquals(com.drivingschool.booking.model.BookingStatus.COMPLETED, booking.getStatus());
            assertEquals(booking.getStudentId(), review.getStudentId());
            assertEquals(booking.getInstructorId(), review.getInstructorId());
        }
        var report = new com.drivingschool.report.service.AdminReportService(enrollments, bookings,
                instructors, vehicles, reviews);
        assertEquals("125000.00", report.revenue().get("PAID"));
        assertEquals(15L, report.bookingsByStatus().values().stream().mapToLong(Long::longValue).sum());
        // A later run must retain a cancelled example and must not refill used Paid lessons.
        // Step 3: A restart must retain decisions and must not issue used tickets again.
        Enrollment handled = enrollments.findByIdStrict("E008").orElseThrow();
        handled.setStatus("CANCELLED");
        enrollments.update(handled);
        Student student = (Student) users.findByIdStrict("S001").orElseThrow();
        student.setLessonsRemaining(0);
        users.update(student);
        seed.run();
        assertEquals(9, enrollments.readAllStrict().size());
        assertEquals("CANCELLED", enrollments.findByIdStrict("E008").orElseThrow().getStatus());
        assertEquals(0, ((com.drivingschool.student.model.Student) users.findByIdStrict("S001").orElseThrow()).getLessonsRemaining());
    }
}
