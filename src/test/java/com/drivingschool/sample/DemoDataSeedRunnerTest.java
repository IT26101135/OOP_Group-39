// Module owner: IT26101132
package com.drivingschool.sample;

import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.util.PasswordHasher;
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
    @Test void seedCanRunTwiceWithoutDuplicateRecords() {
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
        assertEquals(16, users.readAllStrict().size());
        assertEquals(5, instructors.readAllStrict().size());
        assertEquals(6, packages.readAllStrict().size());
        assertEquals(6, vehicles.readAllStrict().size());
        assertEquals(7, enrollments.readAllStrict().size());
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
    }
}
