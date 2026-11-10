// Module owner: IT26101135 - Read-only names for authorized account pages.
package com.drivingschool.common.controller;

import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.instructor.repository.InstructorRepository;
import com.drivingschool.learningpackage.repository.EnrollmentRepository;
import com.drivingschool.learningpackage.repository.LearningPackageRepository;
import com.drivingschool.vehicle.repository.VehicleRepository;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class AccountDisplayAdvice {
    private final UserRepository users;
    private final InstructorRepository instructors;
    private final LearningPackageRepository packages;
    private final VehicleRepository vehicles;
    private final BookingRepository bookings;
    private final EnrollmentRepository enrollments;

    public AccountDisplayAdvice(UserRepository users, InstructorRepository instructors,
            LearningPackageRepository packages, VehicleRepository vehicles,
            BookingRepository bookings, EnrollmentRepository enrollments) {
        this.users = users;
        this.instructors = instructors;
        this.packages = packages;
        this.vehicles = vehicles;
        this.bookings = bookings;
        this.enrollments = enrollments;
    }

    @ModelAttribute("accountDisplay")
    public Names display() { return new Names(); }

    // OOP: Encapsulation - templates can resolve display labels, not account details.
    // CRUD: READ - lazily read each catalog once per request, after page authorization.
    public class Names {
        private Map<String, String> people;
        private Map<String, String> courses;
        private Map<String, String> fleet;
        private Map<String, String> lessons;
        private Map<String, String> receipts;

        public String person(String id) {
            if (id == null || id.isBlank()) return "Not assigned";
            if (people == null) {
                people = new HashMap<>();
                for (var person : users.readAllStrict()) people.put(person.getId(), person.getName());
                for (var person : instructors.readAllStrict()) people.put(person.getId(), person.getName());
            }
            return people.getOrDefault(id, "Name unavailable");
        }

        public String course(String id) {
            if (id == null || id.isBlank()) return "No active package";
            if (courses == null) {
                courses = new HashMap<>();
                for (var course : packages.readAllStrict()) courses.put(course.getId(), course.getName());
            }
            return courses.getOrDefault(id, "Package no longer listed");
        }

        public String vehicle(String id) {
            if (id == null || id.isBlank()) return "Not assigned";
            if (fleet == null) {
                fleet = new HashMap<>();
                for (var vehicle : vehicles.readAllStrict()) fleet.put(vehicle.getId(), vehicle.getRegistrationNumber());
            }
            return fleet.getOrDefault(id, "Vehicle no longer listed");
        }

        public String lesson(String id) {
            if (lessons == null) {
                lessons = new HashMap<>();
                var display = new SchoolPresentationAdvice.PublicDisplay();
                for (var lesson : bookings.readAllStrict()) lessons.put(lesson.getId(),
                        display.date(lesson.getStartTime().toLocalDate()) + " at " + display.time(lesson.getStartTime().toLocalTime()));
            }
            return lessons.getOrDefault(id, "Lesson date unavailable");
        }

        public String enrollment(String id) {
            if (receipts == null) {
                receipts = new HashMap<>();
                for (var receipt : enrollments.readAllStrict()) receipts.put(receipt.getId(), course(receipt.getPackageId()));
            }
            return receipts.getOrDefault(id, "Package no longer listed");
        }

        public String reportLabel(String label) {
            if (label == null) return "Not recorded";
            if (label.matches("[0-9]{4}-[0-9]{2}")) return java.time.YearMonth.parse(label).format(
                    java.time.format.DateTimeFormatter.ofPattern("MMM yyyy", java.util.Locale.UK));
            if (label.matches("I[0-9]{3}.*") || label.matches("S[0-9]{3}")) return person(label.substring(0, 4));
            if (label.matches("P[0-9]{3}")) return course(label);
            if (label.matches("V[0-9]{3}")) return vehicle(label);
            return new SchoolPresentationAdvice.PublicDisplay().label(label);
        }
    }
}
