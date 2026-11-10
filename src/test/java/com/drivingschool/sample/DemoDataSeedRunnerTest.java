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
import java.nio.file.Files;
import java.time.*;
import java.util.List;
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
        LessonRecordRepository notes = new LessonRecordRepository(folder.toString(), mapper);
        InstructorLeaveRepository leaves = new InstructorLeaveRepository(folder.toString(), mapper);
        Clock clock = Clock.fixed(Instant.parse("2026-11-10T04:30:00Z"), ZoneId.of("Asia/Colombo"));
        DemoDataSeedRunner seed = new DemoDataSeedRunner(users, instructors, packages, enrollments,
                vehicles, bookings, reviews, availability, new PasswordHasher(), notes, leaves, clock);
        seed.run();
        seed.run();
        // Step 2: Check original links and counts plus all three payment examples.
        assertEquals(19, users.readAllStrict().size());
        assertEquals(6, instructors.readAllStrict().size());
        assertEquals(7, packages.readAllStrict().size());
        assertEquals(6, vehicles.readAllStrict().size());
        assertEquals(9, enrollments.readAllStrict().size());
        assertEquals("AWAITING_PAYMENT", enrollments.findByIdStrict("E008").orElseThrow().getStatus());
        assertEquals("CANCELLED", enrollments.findByIdStrict("E009").orElseThrow().getStatus());
        assertEquals(0, ((com.drivingschool.student.model.Student) users.findByIdStrict("S008").orElseThrow()).getLessonsRemaining());
        assertEquals(24, bookings.readAllStrict().size());
        assertEquals(16, reviews.readAllStrict().size());
        assertEquals(5, instructors.readAllStrict().stream().map(com.drivingschool.instructor.model.Instructor::getAverageRating).distinct().count());
        assertEquals(16, bookings.readAllStrict().stream().filter(b -> b.getStatus() == com.drivingschool.booking.model.BookingStatus.COMPLETED).count());
        assertEquals(12, reviews.readAllStrict().stream().filter(r -> r.getStatus() == com.drivingschool.review.model.ReviewStatus.APPROVED).count());
        for (var review : reviews.readAllStrict().stream().filter(r -> r.getBookingId() != null).toList()) {
            var booking = bookings.findByIdStrict(review.getBookingId()).orElseThrow();
            assertEquals(com.drivingschool.booking.model.BookingStatus.COMPLETED, booking.getStatus());
            assertEquals(booking.getStudentId(), review.getStudentId());
            assertEquals(booking.getInstructorId(), review.getInstructorId());
        }
        var report = new com.drivingschool.report.service.AdminReportService(enrollments, bookings,
                instructors, vehicles, reviews);
        assertEquals("190500.00", report.revenue().get("PAID"));
        assertEquals(24L, report.bookingsByStatus().values().stream().mapToLong(Long::longValue).sum());
        assertEquals(42, availability.readAllStrict().size());
        assertEquals(3, notes.readAllStrict().size());
        assertEquals(1, leaves.readAllStrict().size());
        assertEquals(List.of("Shahithiyan", "Pragash", "Piravarshini", "Ruwini", "Pasindu", "Sayumi"),
                instructors.readAllStrict().stream().map(i -> i.getName().split(" ")[0]).toList());
        assertEquals(List.of("I005", "I003", "I001", "I004", "I002", "I006"),
                new com.drivingschool.instructor.service.InstructorSorter().sortByExperience(instructors.readAllStrict())
                        .stream().map(com.drivingschool.instructor.model.Instructor::getId).toList());
        var waiting = bookings.readAllStrict().stream().filter(b -> b.getStatus() == com.drivingschool.booking.model.BookingStatus.PENDING
                && b.getInstructorId() == null).map(com.drivingschool.booking.model.Booking::getId).toList();
        assertEquals(List.of("B001", "B002"), waiting);
        assertEquals("I006", bookings.findByIdStrict("B003").orElseThrow().getInstructorId());
        for (var teacher : instructors.readAllStrict()) {
            var approved = reviews.readAllStrict().stream().filter(r -> teacher.getId().equals(r.getInstructorId())
                    && r.getStatus() == com.drivingschool.review.model.ReviewStatus.APPROVED).toList();
            assertEquals(2, approved.size());
            assertEquals(approved.stream().mapToInt(com.drivingschool.review.model.Review::getRating).average().orElse(0), teacher.getAverageRating());
            assertNull(teacher.getLicenseNumber());
            assertNull(teacher.getQualifications());
        }
        for (int i = 1; i <= 7; i++) {
            Student saved = (Student) users.findByIdStrict(String.format("S%03d", i)).orElseThrow();
            var receipt = enrollments.findByIdStrict(String.format("E%03d", i)).orElseThrow();
            var course = packages.findByIdStrict(receipt.getPackageId()).orElseThrow();
            long completed = bookings.readAllStrict().stream().filter(b -> b.getEnrollmentId().equals(receipt.getId())
                    && b.getStatus() == com.drivingschool.booking.model.BookingStatus.COMPLETED).count();
            assertEquals(course.getLessonCount(), saved.getLessonsRemaining() + completed);
            assertTrue(saved.getNic().matches("0{9}1[0-9]{2}"));
            assertTrue(saved.getPhone().startsWith("+94000000"));
        }
        assertTrue(enrollments.findByIdStrict("E006").orElseThrow().getValidUntil().isBefore(LocalDate.now(clock)));
        assertNull(((Student) users.findByIdStrict("S007").orElseThrow()).getActiveEnrollmentId());
        assertEquals(LocalDate.now(clock).plusDays(10), vehicles.findByIdStrict("V006").orElseThrow().getInsuranceExpiry());
        assertEquals(com.drivingschool.vehicle.model.VehicleOperationalStatus.MAINTENANCE,
                vehicles.findByIdStrict("V003").orElseThrow().getOperationalStatus());
        for (var lesson : bookings.readAllStrict()) {
            var receipt = enrollments.findByIdStrict(lesson.getEnrollmentId()).orElseThrow();
            assertEquals(lesson.getStudentId(), receipt.getStudentId());
            assertFalse(lesson.getStartTime().isBefore(receipt.getEnrolledAt()));
            if (lesson.getStatus() == com.drivingschool.booking.model.BookingStatus.COMPLETED)
                assertFalse(lesson.getStartTime().toLocalDate().isAfter(receipt.getValidUntil()));
            if (lesson.getInstructorId() == null) continue;
            var teacher = instructors.findByIdStrict(lesson.getInstructorId()).orElseThrow();
            var course = packages.findByIdStrict(receipt.getPackageId()).orElseThrow();
            var car = vehicles.findByIdStrict(lesson.getVehicleId()).orElseThrow();
            assertTrue(teacher.getVehicleTypes().contains(course.getType().name()));
            if (course.getType() == com.drivingschool.learningpackage.model.PackageType.MOTORCYCLE)
                assertEquals(com.drivingschool.vehicle.model.VehicleCategory.MOTORCYCLE, car.getVehicleCategory());
            else {
                assertEquals(com.drivingschool.vehicle.model.VehicleCategory.CAR, car.getVehicleCategory());
                if (course.getType() != com.drivingschool.learningpackage.model.PackageType.REFRESHER)
                    assertEquals(course.getType().name(), car.getTransmissionType().name());
            }
            assertNotEquals(com.drivingschool.vehicle.model.VehicleOperationalStatus.MAINTENANCE, car.getOperationalStatus());
        }
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
    @Test
    void checkedInSnapshotDeserializesWithMatchingNamesAndCounters() throws Exception {
        try (var files = Files.list(Path.of("sample-data"))) {
            for (Path file : files.filter(f -> f.toString().endsWith(".txt")).toList())
                Files.copy(file, folder.resolve(file.getFileName()));
        }
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        var users = new UserRepository(folder.toString(), mapper);
        var teachers = new InstructorRepository(folder, mapper);
        var courses = new LearningPackageRepository(folder, mapper);
        var bookings = new BookingRepository(folder.toString(), mapper);
        var reviews = new ReviewRepository(folder.toString(), mapper);
        var vehicles = new VehicleRepository(folder, mapper);
        var receipts = new EnrollmentRepository(folder.toString(), mapper);
        assertEquals(19, users.readAllStrict().size());
        assertEquals(6, teachers.readAllStrict().size());
        assertEquals(7, courses.readAllStrict().size());
        assertEquals(24, bookings.readAllStrict().size());
        assertEquals(16, reviews.readAllStrict().size());
        assertEquals(6, vehicles.readAllStrict().size());
        assertEquals(9, receipts.readAllStrict().size());
        assertEquals(3, new LessonRecordRepository(folder.toString(), mapper).readAllStrict().size());
        assertEquals(42, new AvailabilityRepository(folder.toString(), mapper).readAllStrict().size());
        assertEquals(1, new InstructorLeaveRepository(folder.toString(), mapper).readAllStrict().size());
        assertEquals("Sayumi Perera", teachers.findByIdStrict("I006").orElseThrow().getName());
        assertEquals("S013", users.nextId("S"));
        assertEquals("I007", users.nextId("I"));
        assertEquals("P008", courses.nextId("P"));
        assertEquals("B025", bookings.nextId("B"));
        PasswordHasher passwords = new PasswordHasher();
        for (var user : users.readAllStrict()) assertTrue(passwords.matches(DemoDataSeedRunner.DEMO_PASSWORD, user.getPasswordHash()));
        for (var review : reviews.readAllStrict()) {
            var booking = bookings.findByIdStrict(review.getBookingId()).orElseThrow();
            assertEquals(com.drivingschool.booking.model.BookingStatus.COMPLETED, booking.getStatus());
            assertEquals(booking.getStudentId(), review.getStudentId());
            assertEquals(booking.getInstructorId(), review.getInstructorId());
        }
    }

}
