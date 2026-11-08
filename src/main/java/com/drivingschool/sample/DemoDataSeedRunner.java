/**
============================================================
MEMBER   : IT26101132 - [Name]
MODULE   : Vehicles and Sample Data
FILE     : DemoDataSeedRunner.java
WHAT IT DOES (simple words): Adds repeat-safe example people, lessons, vehicles and reviews.
REAL-LIFE EXAMPLE: Like a fleet logbook showing which teaching cars need attention.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for vehicles
============================================================
*/
package com.drivingschool.sample;

import com.drivingschool.auth.model.*;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.util.PasswordHasher;
import com.drivingschool.booking.model.*;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.instructor.model.*;
import com.drivingschool.instructor.repository.*;
import com.drivingschool.learningpackage.model.*;
import com.drivingschool.learningpackage.repository.*;
import com.drivingschool.review.model.*;
import com.drivingschool.review.repository.ReviewRepository;
import com.drivingschool.student.model.Student;
import com.drivingschool.vehicle.model.*;
import com.drivingschool.vehicle.repository.VehicleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

/** Optional repeat-safe fixture loader for coursework demonstrations. */
@Component
@ConditionalOnProperty(name = "app.demo-seed.enabled", havingValue = "true")
public class DemoDataSeedRunner implements CommandLineRunner {
    public static final String DEMO_PASSWORD = "DriveSpark123!";
    private final UserRepository users;
    private final InstructorRepository instructors;
    private final LearningPackageRepository packages;
    private final EnrollmentRepository enrollments;
    private final VehicleRepository vehicles;
    private final BookingRepository bookings;
    private final ReviewRepository reviews;
    private final AvailabilityRepository availability;
    private final PasswordHasher passwords;

    // Receives the details this class needs and keeps them ready for its own job.
    public DemoDataSeedRunner(UserRepository users, InstructorRepository instructors,
                              LearningPackageRepository packages, EnrollmentRepository enrollments,
                              VehicleRepository vehicles, BookingRepository bookings,
                              ReviewRepository reviews, AvailabilityRepository availability,
                              PasswordHasher passwords) {
        this.users = users; this.instructors = instructors; this.packages = packages;
        this.enrollments = enrollments; this.vehicles = vehicles; this.bookings = bookings;
        this.reviews = reviews; this.availability = availability; this.passwords = passwords;
    }

    // Adds demo records in dependency order and leaves existing records in place.
    @Override
    public void run(String... args) {
        // Step 1: Create people and the resources that their lessons need.
        seedAccounts();
        seedPackages();
        seedVehicles();
        seedEnrollments();
        seedAvailability();
        // Step 2: Add lesson history and reviews only after their people exist.
        seedBookings();
        seedReviews();
        syncInstructorRatings();
    }

    // Adds demo accounts and matching instructor profiles when they are missing.
    private void seedAccounts() {
        // Step 1: Add the demo administrator only when that email is missing.
        if (!accountExists("admin@drivingschool.test"))
            users.save(new Admin(users.nextId("A"), "Demo Admin", "admin@drivingschool.test", passwords.hash(DEMO_PASSWORD)));
        // Step 2: Give each demo instructor an account and a matching teaching profile.
        for (int i = 1; i <= 5; i++) {
            String id = String.format("I%03d", i), email = String.format("instructor%d@drivingschool.test", i);
            if (!accountExists(email)) {
                InstructorAccount account = new InstructorAccount(id, "Instructor " + i, email, passwords.hash(DEMO_PASSWORD));
                users.save(account);
            }
            if (instructors.findByIdStrict(id).isEmpty()) {
                Instructor profile = new Instructor(id, "Instructor " + i, email,
                        "+9477000000" + i, i * 3, true);
                profile.setBio("Experienced driving instructor for practical lessons.");
                profile.setLicenseNumber("DL-DEMO-" + i);
                profile.setQualifications("Certified instructor");
                profile.setVehicleTypes(List.of("MANUAL", "AUTOMATIC", "MOTORCYCLE"));
                profile.setAverageRating(0.0);
                instructors.save(profile);
            }
        }
        // Step 3: Add students with the original active, waiting and suspended examples.
        for (int i = 1; i <= 10; i++) {
            String id = String.format("S%03d", i), email = String.format("student%d@drivingschool.test", i);
            if (!accountExists(email)) {
                Student student = new Student(id, "Student " + i, email, passwords.hash(DEMO_PASSWORD), "+9477111000" + i);
                student.setStatus(i == 10 ? AccountStatus.SUSPENDED : i >= 8 ? AccountStatus.PENDING : AccountStatus.ACTIVE);
                users.save(student);
            }
        }
    }

    // Adds six example courses with their original prices and lesson counts.
    private void seedPackages() {
        PackageType[] types = PackageType.values();
        for (int i = 1; i <= 6; i++) {
            String id = String.format("P%03d", i);
            if (packages.findByIdStrict(id).isPresent()) continue;
            LearningPackage learningPackage = new LearningPackage(id, "Demo Package " + i,
                    "Sample learning package for driving school demonstrations.",
                    new BigDecimal(10000 + i * 2500 + ".00"), 8 + i, 90, true);
            learningPackage.setType(types[(i - 1) % types.length]); learningPackage.setDurationHours(8 + i);
            packages.save(learningPackage);
        }
    }

    // Adds six example teaching vehicles and a near-expiry insurance example.
    private void seedVehicles() {
        for (int i = 1; i <= 6; i++) {
            String id = String.format("V%03d", i), plate = "DEMO" + (1000 + i);
            if (vehicleRegistrationExists(plate)) continue;
            Vehicle vehicle = new Vehicle(id, plate, "DemoMake", "Model " + i,
                    i == 2 ? TransmissionType.AUTOMATIC : TransmissionType.MANUAL,
                    i == 6 ? VehicleCategory.MOTORCYCLE : VehicleCategory.CAR,
                    i == 5 ? VehicleOperationalStatus.MAINTENANCE : VehicleOperationalStatus.IN_SERVICE);
            vehicle.setInsuranceExpiry(LocalDate.now().plusDays(i == 6 ? 10 : 300));
            vehicle.setLastServiceDate(LocalDate.now().minusDays(30));
            vehicle.setNextServiceDate(LocalDate.now().plusDays(120));
            vehicles.save(vehicle);
        }
    }

    // Links example students to their packages without replacing existing choices.
    private void seedEnrollments() {
        for (int i = 1; i <= 7; i++) {
            String studentId = String.format("S%03d", i), enrollmentId = String.format("E%03d", i);
            Student student = (Student) users.findByIdStrict(studentId).orElse(null);
            if (student == null || student.getActiveEnrollmentId() != null) continue;
            LearningPackage learningPackage = packages.findByIdStrict(String.format("P%03d", (i - 1) % 6 + 1)).orElseThrow();
            Enrollment enrollment = new Enrollment(enrollmentId, studentId, learningPackage.getId(), LocalDateTime.now().minusDays(i), learningPackage.getPrice(), "PAID");
            enrollments.save(enrollment); student.setActivePackageId(learningPackage.getId()); student.setActiveEnrollmentId(enrollment.getId()); student.setLessonsRemaining(learningPackage.getLessonCount()); users.update(student);
        }
    }

    // Adds the original working-week blocks without duplicating their IDs.
    private void seedAvailability() {
        for (int i = 1; i <= 5; i++) for (DayOfWeek day : DayOfWeek.values()) {
            String id = String.format("H%03d", (i - 1) * 7 + day.getValue());
            if (availability.findByIdStrict(id).isEmpty())
                availability.save(new Availability(id, String.format("I%03d", i), day, LocalTime.of(8, 0), LocalTime.of(17, 0)));
        }
    }

    // Adds the original fifteen lesson requests and their example attendance states.
    private void seedBookings() {
        for (int i = 1; i <= 15; i++) {
            String id = String.format("B%03d", i);
            if (bookings.findByIdStrict(id).isPresent()) continue;
            // Step 1: Keep the original sample student, instructor and vehicle links.
            int student = (i - 1) % 7 + 1, instructor = (i - 1) % 5 + 1, vehicle = (i - 1) % 6 + 1;
            // Step 2: Pick the original attendance example for this numbered lesson.
            BookingStatus status = switch (i) {
                case 1, 2, 3, 12, 15 -> BookingStatus.PENDING;
                case 4, 5 -> BookingStatus.CONFIRMED;
                case 6, 7, 11, 13, 14 -> BookingStatus.COMPLETED;
                case 8 -> BookingStatus.NO_SHOW;
                case 9 -> BookingStatus.CANCELLED;
                default -> BookingStatus.REJECTED;
            };
            boolean assigned = status != BookingStatus.PENDING && status != BookingStatus.REJECTED;
            if (i == 11) instructor = 3;
            if (i == 13) instructor = 4;
            if (i == 14) instructor = 5;
            // Step 3: Put finished lessons in the past and requests in the future.
            LocalDateTime start = status == BookingStatus.COMPLETED || status == BookingStatus.NO_SHOW
                    ? LocalDateTime.now().minusDays(i).withHour(9).withMinute(0)
                    : LocalDateTime.now().plusDays(i).withHour(9).withMinute(0);
            String eId = String.format("E%03d", student);
            if (enrollments.findByIdStrict(eId).isEmpty()) { student = 1; eId = "E001"; }
            Booking booking = new Booking(id, String.format("S%03d", student), eId,
                    start, start.plusHours(1), assigned ? String.format("I%03d", instructor) : null,
                    assigned ? String.format("V%03d", vehicle) : null, status, start.minusDays(5));
            if (status == BookingStatus.REJECTED) booking.setRejectionReason("Sample conflict rejection.");
            if (status == BookingStatus.PENDING) booking.setRequestedInstructorId(String.format("I%03d", instructor));
            // Step 4: Save the fixture only after its enrollment and assignment are ready.
            bookings.save(booking);
        }
    }

    // Adds ten example reviews with the original links and moderation states.
    private void seedReviews() {
        for (int i = 1; i <= 10; i++) {
            String id = String.format("R%03d", i);
            if (reviews.findByIdStrict(id).isPresent()) continue;
            ReviewStatus status = switch (i) {
                case 2, 4, 6, 8, 10 -> ReviewStatus.APPROVED;
                case 1, 5 -> ReviewStatus.PENDING;
                default -> ReviewStatus.HIDDEN;
            };
            LocalDateTime time = LocalDateTime.now().minusDays(i);
            String linkedBooking = switch (i) { case 2 -> "B006"; case 4 -> "B007"; case 6 -> "B011"; case 8 -> "B013"; case 10 -> "B014"; default -> null; };
            String instructorId = switch (i) { case 2 -> "I001"; case 4 -> "I002"; case 6 -> "I003"; case 8 -> "I004"; case 10 -> "I005"; default -> null; };
            int reviewStudent = switch (i) { case 2 -> 6; case 4 -> 7; case 6 -> 4; case 8 -> 6; case 10 -> 7; default -> i; };
            reviews.save(new Review(id, String.format("S%03d", reviewStudent), linkedBooking, instructorId,
                    (i - 1) % 5 + 1, "Sample student review number " + i + " for evaluation.", status, time, time));
        }
    }

    // Recounts only approved feedback so demo profile ratings match their reviews.
    private void syncInstructorRatings() {
        for (Instructor instructor : instructors.readAllStrict()) {
            int ratingTotal = 0;
            int approvedCount = 0;
            for (Review review : reviews.readAllStrict()) {
                if (instructor.getId().equals(review.getInstructorId())
                        && review.getStatus() == ReviewStatus.APPROVED) {
                    ratingTotal += review.getRating();
                    approvedCount++;
                }
            }
            double average = 0.0;
            if (approvedCount > 0) {
                average = (double) ratingTotal / approvedCount;
            }
            instructor.setAverageRating(Math.round(average * 100.0) / 100.0);
            instructors.update(instructor);
        }
    }

    // Leaves an existing account untouched when the demo app starts a second time.
    private boolean accountExists(String email) {
        for (User user : users.readAllStrict()) {
            if (user.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    // Avoids adding a second demo car with the same registration plate.
    private boolean vehicleRegistrationExists(String registrationNumber) {
        for (Vehicle vehicle : vehicles.readAllStrict()) {
            if (vehicle.getRegistrationNumber().equals(registrationNumber)) {
                return true;
            }
        }
        return false;
    }
}
