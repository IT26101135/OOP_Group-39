/**
============================================================
MEMBER   : IT26101132
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
import java.util.Map;

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
    private final LessonRecordRepository lessonRecords;
    private final InstructorLeaveRepository leaves;
    private final Clock clock;

    private record InstructorSeed(String name, int years, List<String> types, String bio) {}
    private static final List<InstructorSeed> TEACHERS = List.of(
            new InstructorSeed("Shahithiyan Sivarajah", 8, List.of("MANUAL"), "Shahithiyan works on clutch control and moving off on slopes. He breaks each exercise into small steps."),
            new InstructorSeed("Pragash Tharmalingam", 3, List.of("AUTOMATIC"), "Pragash helps new learners practise junctions and lane changes. He leaves time at the end to discuss what needs more practice."),
            new InstructorSeed("Piravarshini Kanagarajah", 12, List.of("MANUAL", "AUTOMATIC"), "Piravarshini teaches parking and driving in busy traffic. She gives clear feedback after each exercise."),
            new InstructorSeed("Ruwini Jayasekara", 5, List.of("AUTOMATIC", "REFRESHER"), "Ruwini works with learners returning after a break. Her lessons focus on observation, positioning and parking."),
            new InstructorSeed("Pasindu Wickramasinghe", 15, List.of("MANUAL", "MOTORCYCLE"), "Pasindu teaches low-speed control and careful observation. He repeats difficult exercises at a pace the learner can manage."),
            new InstructorSeed("Sayumi Perera", 2, List.of("MOTORCYCLE"), "Sayumi helps beginners practise balance, braking and turns. She explains each exercise before the learner starts."));
    private static final List<String> STUDENTS = List.of("Nethmi Senanayake", "Kavin Suthakaran",
            "Dinuka Fernando", "Abirami Sivanesan", "Tharushi Wijesinghe", "Arjun Rajendran",
            "Malithi Gunasekara", "Dharshan Nadarajah", "Isuri Dissanayake", "Janani Balendran",
            "Chamodi Abeysinghe", "Nivethan Yogarajah");
    private record CourseSeed(String name, String description, String price, int lessons, int validity, PackageType type) {}
    private static final List<CourseSeed> COURSES = List.of(
            new CourseSeed("Manual Car: Beginner Course", "Practise moving off, clutch control, gear changes and simple junctions in ten one-hour lessons.", "25000.00", 10, 90, PackageType.MANUAL),
            new CourseSeed("Manual Car: Complete Course", "Twenty hours of manual driving practice covering parking, hill starts, junctions and busier roads.", "45000.00", 20, 180, PackageType.MANUAL),
            new CourseSeed("Automatic Car: Beginner Course", "Learn steering, braking, observation and everyday junctions in an automatic car.", "28000.00", 10, 90, PackageType.AUTOMATIC),
            new CourseSeed("Automatic Car: Complete Course", "Twenty one-hour lessons to practise parking, lane changes and driving in varied traffic.", "50000.00", 20, 180, PackageType.AUTOMATIC),
            new CourseSeed("Motorcycle: Beginner Course", "Eight one-hour lessons covering balance, controlled braking, turns and road observation.", "15000.00", 8, 90, PackageType.MOTORCYCLE),
            new CourseSeed("Refresher Lessons (5 hours)", "Five lessons for drivers returning after a break, with practice focused on the areas you need.", "15000.00", 5, 60, PackageType.REFRESHER),
            new CourseSeed("Driving Test Preparation", "Five hours of manual car practice covering common manoeuvres, observation and areas your instructor identifies.", "12500.00", 5, 45, PackageType.MANUAL));
    private record LessonSeed(int student, int instructor, int vehicle, int day, BookingStatus status) {}
    private static final List<LessonSeed> LESSONS = List.of(
            new LessonSeed(1, 1, 1, 2, BookingStatus.PENDING),
            new LessonSeed(3, 2, 2, 3, BookingStatus.PENDING),
            new LessonSeed(5, 6, 6, 4, BookingStatus.PENDING),
            new LessonSeed(4, 4, 4, 5, BookingStatus.CONFIRMED),
            new LessonSeed(2, 5, 1, 6, BookingStatus.CONFIRMED),
            new LessonSeed(1, 1, 1, -22, BookingStatus.COMPLETED),
            new LessonSeed(2, 1, 1, -21, BookingStatus.COMPLETED),
            new LessonSeed(3, 2, 2, -20, BookingStatus.COMPLETED),
            new LessonSeed(4, 2, 2, -19, BookingStatus.COMPLETED),
            new LessonSeed(1, 3, 1, -18, BookingStatus.COMPLETED),
            new LessonSeed(4, 3, 2, -17, BookingStatus.COMPLETED),
            new LessonSeed(6, 4, 4, -12, BookingStatus.COMPLETED),
            new LessonSeed(3, 4, 2, -15, BookingStatus.COMPLETED),
            new LessonSeed(2, 5, 1, -14, BookingStatus.COMPLETED),
            new LessonSeed(7, 5, 1, -13, BookingStatus.COMPLETED),
            new LessonSeed(5, 6, 6, -11, BookingStatus.COMPLETED),
            new LessonSeed(5, 6, 5, -10, BookingStatus.COMPLETED),
            new LessonSeed(7, 1, 1, -9, BookingStatus.COMPLETED),
            new LessonSeed(7, 3, 1, -8, BookingStatus.COMPLETED),
            new LessonSeed(7, 5, 1, -7, BookingStatus.COMPLETED),
            new LessonSeed(7, 1, 1, -6, BookingStatus.COMPLETED),
            new LessonSeed(1, 1, 1, 7, BookingStatus.CANCELLED),
            new LessonSeed(3, 2, 2, -5, BookingStatus.NO_SHOW),
            new LessonSeed(2, 3, 1, 8, BookingStatus.REJECTED));

    // Receives the details this class needs and keeps them ready for its own job.
    public DemoDataSeedRunner(UserRepository users, InstructorRepository instructors,
                              LearningPackageRepository packages, EnrollmentRepository enrollments,
                              VehicleRepository vehicles, BookingRepository bookings,
                              ReviewRepository reviews, AvailabilityRepository availability,
                              PasswordHasher passwords, LessonRecordRepository lessonRecords,
                              InstructorLeaveRepository leaves, Clock clock) {
        this.users = users; this.instructors = instructors; this.packages = packages;
        this.enrollments = enrollments; this.vehicles = vehicles; this.bookings = bookings;
        this.reviews = reviews; this.availability = availability; this.passwords = passwords;
        this.lessonRecords = lessonRecords; this.leaves = leaves; this.clock = clock;
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
        seedLessonNotes();
        syncInstructorRatings();
    }

    // Adds demo accounts and matching instructor profiles when they are missing.
    private void seedAccounts() {
        // Step 1: Add the demo administrator only when that email is missing.
        if (!accountExists("admin@drivingschool.test")) {
            Admin admin = new Admin(users.nextId("A"), "Anjali Perera", "admin@drivingschool.test", passwords.hash(DEMO_PASSWORD));
            admin.setCreatedAt(LocalDate.now(clock).minusDays(210).atTime(9, 0).toString());
            users.save(admin);
        }
        // Step 2: Give each demo instructor an account and a matching teaching profile.
        for (int i = 1; i <= TEACHERS.size(); i++) {
            InstructorSeed details = TEACHERS.get(i - 1);
            String id = String.format("I%03d", i), email = String.format("instructor%d@drivingschool.test", i);
            if (!accountExists(email)) {
                InstructorAccount account = new InstructorAccount(id, details.name(), email, passwords.hash(DEMO_PASSWORD));
                account.setCreatedAt(LocalDate.now(clock).minusDays(200 - i).atTime(9, 0).toString());
                users.save(account);
            }
            if (instructors.findByIdStrict(id).isEmpty()) {
                Instructor profile = new Instructor(id, details.name(), email,
                        String.format("+940000002%02d", i), details.years(), true);
                profile.setBio(details.bio());
                profile.setVehicleTypes(details.types());
                instructors.save(profile);
            }
        }
        // Step 3: Add students with the original active, waiting and suspended examples.
        for (int i = 1; i <= STUDENTS.size(); i++) {
            String id = String.format("S%03d", i);
            String email = i <= 10 ? String.format("student%d@drivingschool.test", i)
                    : i == 11 ? "chamodi.abeysinghe@learners.example.test" : "nivethan.yogarajah@learners.example.test";
            if (accountExists(email)) continue;
            Student student = new Student(id, STUDENTS.get(i - 1), email, passwords.hash(DEMO_PASSWORD),
                    String.format("+940000001%02d", i));
            // Zero-prefix contact/NIC fixtures satisfy syntax without copying a real identity.
            student.setNic(String.format("0000000001%02d", i));
            student.setDateOfBirth(LocalDate.of(1995 + i % 9, i % 12 + 1, i + 3));
            student.setAddress((10 + i) + ", Nelum Lane, Lakeview");
            student.setLicenseTypeWanted(i <= 7 ? COURSES.get(i - 1).type().name() : "MANUAL");
            student.setCreatedAt(LocalDate.now(clock).minusDays(100 - i).atTime(10, 0).toString());
            student.setStatus(i == 10 ? AccountStatus.SUSPENDED : i == 9 ? AccountStatus.PENDING : AccountStatus.ACTIVE);
            users.save(student);
        }
    }

    // Adds seven courses with captured prices and one-hour lessons.
    private void seedPackages() {
        for (int i = 1; i <= COURSES.size(); i++) {
            String id = String.format("P%03d", i);
            if (packages.findByIdStrict(id).isPresent()) continue;
            CourseSeed details = COURSES.get(i - 1);
            LearningPackage course = new LearningPackage(id, details.name(), details.description(),
                    new BigDecimal(details.price()), details.lessons(), details.validity(), true);
            course.setType(details.type());
            course.setDurationHours(details.lessons());
            packages.save(course);
        }
    }

    // Adds six example teaching vehicles and a near-expiry insurance example.
    private void seedVehicles() {
        String[] plates = {"CAB-4826", "CAX-7319", "CAA-2684", "CBB-5197", "BHV-6248", "BIC-3086"};
        String[] makes = {"Suzuki", "Toyota", "Suzuki", "Nissan", "Bajaj", "Honda"};
        String[] models = {"Alto", "Vitz", "Celerio", "March", "Discover 125", "Dio"};
        LocalDate today = LocalDate.now(clock);
        for (int i = 1; i <= plates.length; i++) {
            String id = String.format("V%03d", i);
            if (vehicles.findByIdStrict(id).isPresent() || vehicleRegistrationExists(Vehicle.normalizeRegistrationNumber(plates[i - 1]))) continue;
            Vehicle vehicle = new Vehicle(id, plates[i - 1], makes[i - 1], models[i - 1],
                    i == 2 || i == 4 || i == 6 ? TransmissionType.AUTOMATIC : TransmissionType.MANUAL,
                    i >= 5 ? VehicleCategory.MOTORCYCLE : VehicleCategory.CAR,
                    i == 3 ? VehicleOperationalStatus.MAINTENANCE : VehicleOperationalStatus.IN_SERVICE);
            vehicle.setInsuranceExpiry(today.plusDays(i == 6 ? 10 : 240 + i * 15));
            vehicle.setLastServiceDate(today.minusDays(20 + i * 7));
            vehicle.setNextServiceDate(today.plusDays(i == 3 ? 3 : i == 6 ? 7 : 45 + i * 12));
            vehicles.save(vehicle);
        }
    }

    // Links example students to their packages without replacing existing choices.
    private void seedEnrollments() {
        for (int i = 1; i <= 7; i++) {
            String studentId = String.format("S%03d", i), enrollmentId = String.format("E%03d", i);
            // An existing receipt must never be recreated or refill an exhausted balance.
            if (enrollments.findByIdStrict(enrollmentId).isPresent()) continue;
            Student student = (Student) users.findByIdStrict(studentId).orElse(null);
            if (student == null || student.getActiveEnrollmentId() != null) continue;
            LearningPackage course = packages.findByIdStrict(String.format("P%03d", i)).orElseThrow();
            int daysAgo = i == 6 ? 70 : i == 7 ? 30 : 45;
            Enrollment enrollment = new Enrollment(enrollmentId, studentId, course.getId(),
                    LocalDate.now(clock).minusDays(daysAgo).atTime(10, 0), course.getPrice(), "PAID");
            enrollment.setValidUntil(enrollment.getEnrolledAt().toLocalDate().plusDays(course.getValidityDays()));
            enrollments.save(enrollment);
            int completed = 0;
            for (LessonSeed lesson : LESSONS) if (lesson.student() == i && lesson.status() == BookingStatus.COMPLETED) completed++;
            student.setActivePackageId(course.getId());
            student.setActiveEnrollmentId(enrollmentId);
            student.setLessonsRemaining(Math.max(0, course.getLessonCount() - completed));
            users.update(student);
        }
        seedPaymentExample("E008", "AWAITING_PAYMENT");
        seedPaymentExample("E009", "CANCELLED");
    }

    // Adds cashier examples without replacing decisions made during an earlier demo run.
    private void seedPaymentExample(String enrollmentId, String status) {
        if (enrollments.findByIdStrict(enrollmentId).isPresent()) return;
        Student student = (Student) users.findByIdStrict("S008").orElse(null);
        if (student == null) return;
        if ("AWAITING_PAYMENT".equals(status)
                && (student.getActivePackageId() != null || hasAwaitingRequest(student.getId()))) return;
        LearningPackage selected = packages.findByIdStrict("P001").orElseThrow();
        Enrollment enrollment = new Enrollment(enrollmentId, student.getId(), selected.getId(),
                LocalDate.now(clock).minusDays(1).atTime(11, 0), selected.getPrice(), "UNPAID");
        enrollment.setStatus(status);
        if ("CANCELLED".equals(status)) enrollment.setCancellationReason("I need to postpone lessons until my work hours change.");
        enrollments.save(enrollment);
    }

    // Keeps a restart from creating a second unpaid request for the same student.
    private boolean hasAwaitingRequest(String studentId) {
        for (Enrollment enrollment : enrollments.readAllStrict()) {
            if (enrollment.getStudentId().equals(studentId)
                    && "AWAITING_PAYMENT".equals(enrollment.getStatus())) return true;
        }
        return false;
    }

    // Adds the original working-week blocks without duplicating their IDs.
    private void seedAvailability() {
        for (int i = 1; i <= TEACHERS.size(); i++) for (DayOfWeek day : DayOfWeek.values()) {
            String id = String.format("H%03d", (i - 1) * 7 + day.getValue());
            if (availability.findByIdStrict(id).isEmpty())
                availability.save(new Availability(id, String.format("I%03d", i), day,
                        LocalTime.of(i % 2 == 0 ? 9 : 8, 0), LocalTime.of(i % 2 == 0 ? 16 : 17, 0)));
        }
        if (leaves.findByIdStrict("L001").isEmpty())
            leaves.save(new InstructorLeave("L001", "I005", LocalDate.now(clock).plusDays(14), "Family appointment"));
    }

    // Adds twenty-four linked lessons with varied attendance states.
    private void seedBookings() {
        for (int i = 1; i <= LESSONS.size(); i++) {
            String id = String.format("B%03d", i);
            if (bookings.findByIdStrict(id).isPresent()) continue;
            // Step 1: Keep the original sample student, instructor and vehicle links.
            // The revised links match each student's course and the instructor's teaching types.
            LessonSeed details = LESSONS.get(i - 1);
            // Step 2: Pick the original attendance example for this numbered lesson.
            BookingStatus status = details.status();
            boolean assigned = status != BookingStatus.REJECTED && (status != BookingStatus.PENDING || i == 3);
            // Step 3: Put finished lessons in the past and requests in the future.
            LocalDateTime start = LocalDate.now(clock).plusDays(details.day()).atTime(9, 0);
            LocalDateTime created = details.day() < 0 ? start.minusDays(3)
                    : LocalDate.now(clock).minusDays(4).atTime(8, 30).plusMinutes(i);
            Booking booking = new Booking(id, String.format("S%03d", details.student()),
                    String.format("E%03d", details.student()), start, start.plusHours(1),
                    assigned ? String.format("I%03d", details.instructor()) : null,
                    assigned ? String.format("V%03d", details.vehicle()) : null, status, created);
            if (status == BookingStatus.REJECTED) booking.setRejectionReason("The requested time was unavailable. Please choose another lesson time.");
            if (status == BookingStatus.CANCELLED) booking.setRejectionReason("The student could not attend because their work shift changed.");
            if (status == BookingStatus.PENDING) booking.setRequestedInstructorId(String.format("I%03d", details.instructor()));
            // Step 4: Save the fixture only after its enrollment and assignment are ready.
            bookings.save(booking);
        }
    }

    // Adds sixteen distinct reviews linked to completed lessons.
    private void seedReviews() {
        int[] ratings = {5, 4, 4, 4, 5, 4, 3, 4, 5, 5, 3, 3, 4, 2, 5, 3};
        String[] comments = {
                "We spent extra time on hill starts. I was much more comfortable with the clutch by the end.",
                "The gear-change practice was useful. I still need to work on checking my mirrors early.",
                "Clear instructions at junctions, and enough time to try the turns again.",
                "The lesson helped with lane positioning. A little more parking practice would have been useful.",
                "Piravarshini explained reverse parking one step at a time and let me repeat it without rushing.",
                "We worked on busy junctions. The feedback afterwards was clear and easy to remember.",
                "Useful practice after a long break, although the lesson felt rushed towards the end.",
                "Ruwini helped me spot where I was braking too late. I would like to practise the same route again.",
                "Pasindu corrected my hill-start technique and explained why the car was rolling back.",
                "The practice run showed exactly which observations I was missing before turning.",
                "The braking exercise helped. I wanted more time on turns rather than repeating the straight section.",
                "Patient explanations, but we started late and I felt short of practice time.",
                "The parking feedback was useful. I need another attempt before I feel ready.",
                "I left unsure about the manoeuvre. A slower explanation would have helped me more.",
                "The practice on observation and signalling made the test exercises easier to follow.",
                "The instructor answered my questions, though I would prefer fewer exercises in one lesson."
        };
        for (int i = 1; i <= ratings.length; i++) {
            String id = String.format("R%03d", i);
            if (reviews.findByIdStrict(id).isPresent()) continue;
            Booking lesson = bookings.findByIdStrict(String.format("B%03d", i + 5)).orElseThrow();
            ReviewStatus status = i <= 12 ? ReviewStatus.APPROVED : i % 2 == 1 ? ReviewStatus.PENDING : ReviewStatus.HIDDEN;
            LocalDateTime time = lesson.getEndTime().plusHours(2);
            reviews.save(new Review(id, lesson.getStudentId(), lesson.getId(), lesson.getInstructorId(),
                    ratings[i - 1], comments[i - 1], status, time, time));
        }
    }

    // CRUD: CREATE - add lesson feedback only when that note is missing.
    private void seedLessonNotes() {
        String[] bookingIds = {"B006", "B007", "B021"};
        String[] notes = {"Clutch control is improving. Practise checking the mirrors before moving off on a slope.",
                "Gear changes are smoother now. Leave a little more space when stopping behind another car.",
                "Completed the parking and observation exercises consistently. Keep checking for other road users before each manoeuvre."};
        for (int i = 0; i < bookingIds.length; i++) {
            String id = String.format("N%03d", i + 1);
            if (lessonRecords.findByIdStrict(id).isPresent()) continue;
            Booking lesson = bookings.findByIdStrict(bookingIds[i]).orElseThrow();
            lessonRecords.save(new LessonRecord(id, lesson.getId(), notes[i],
                    Map.of("Clutch control", i == 2 ? 4 : 3, "Parking", 4, "Road signs", 4, "Lane discipline", i == 0 ? 3 : 4),
                    i == 2, lesson.getEndTime().plusMinutes(15)));
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
