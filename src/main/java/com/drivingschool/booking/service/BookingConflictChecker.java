/**
============================================================
MEMBER   : IT26101134 - [Name]
MODULE   : Bookings, FIFO queue and scheduling
FILE     : BookingConflictChecker.java
WHAT IT DOES (simple words): Applies the booking conflict checker rules.
REAL-LIFE EXAMPLE: A reception waiting line handles the oldest request first.
OOP USED : Encapsulation (private fields and public methods)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/
package com.drivingschool.booking.service;

import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.booking.util.LessonIntervalUtils;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.instructor.model.Availability;
import com.drivingschool.instructor.model.Instructor;
import com.drivingschool.instructor.repository.AvailabilityRepository;
import com.drivingschool.instructor.repository.InstructorLeaveRepository;
import com.drivingschool.instructor.repository.InstructorRepository;
import com.drivingschool.learningpackage.model.LearningPackage;
import com.drivingschool.learningpackage.model.PackageType;
import com.drivingschool.learningpackage.repository.LearningPackageRepository;
import com.drivingschool.student.model.Student;
import com.drivingschool.vehicle.model.Vehicle;
import com.drivingschool.vehicle.model.VehicleCategory;
import com.drivingschool.vehicle.model.VehicleOperationalStatus;
import com.drivingschool.vehicle.model.TransmissionType;
import com.drivingschool.vehicle.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import com.drivingschool.auth.model.User;
import com.drivingschool.instructor.model.InstructorLeave;
import java.util.Locale;

/** Central server-side rules for deciding whether a lesson can be confirmed. */
@Service
// SOLID: S - This class is responsible for application rules.
public class BookingConflictChecker {
    private final BookingRepository bookings;
    private final AvailabilityRepository availability;
    private final InstructorLeaveRepository leaves;
    private final InstructorRepository instructors;
    private final VehicleRepository vehicles;
    private final LearningPackageRepository packages;
    private final UserRepository users;
    private final Clock clock;
    private final PaidEnrollmentGuard paidEnrollments;

    // Receives the helpers or validates the details needed to create this object.
    public BookingConflictChecker(BookingRepository bookings,
                                  AvailabilityRepository availability,
                                  InstructorLeaveRepository leaves,
                                  InstructorRepository instructors,
                                  VehicleRepository vehicles,
                                  LearningPackageRepository packages,
                                  UserRepository users, Clock clock, PaidEnrollmentGuard paidEnrollments) {
        this.bookings = bookings;
        this.availability = availability;
        this.leaves = leaves;
        this.instructors = instructors;
        this.vehicles = vehicles;
        this.packages = packages;
        this.users = users;
        this.clock = clock;
        this.paidEnrollments = paidEnrollments;
    }

    /** Returns null when every applicable check passes; otherwise returns the first reason. */
    // Returns a clear reason if a student, instructor or vehicle cannot use this lesson slot.
    public String conflictReason(Booking target, String instructorId, String vehicleId) {
        return conflictReason(target, instructorId, vehicleId, null);
    }

    // Returns a clear reason if a student, instructor or vehicle cannot use this lesson slot.
    public String conflictReason(Booking target, String instructorId, String vehicleId,
                                 String ignoredBookingId) {
        LocalDateTime start = target.getStartTime();
        LocalDateTime end = target.getEndTime();
        if (!start.isAfter(LocalDateTime.now(clock))) return "The lesson time must be in the future.";

        // Step 1: Check the student still has the enrollment needed for this request.
        String paymentProblem = paidEnrollments.problemFor(target.getStudentId(), target.getEnrollmentId());
        if (paymentProblem != null) return paymentProblem;
        User student = users.findByIdStrict(target.getStudentId())
                .orElseThrow(() -> new RecordNotFoundException("Student account not found."));
        if (!(student instanceof Student profile) || profile.getLessonsRemaining() <= 0
                || profile.getActiveEnrollmentId() == null
                || profile.getActivePackageId() == null
                || !target.getEnrollmentId().equals(profile.getActiveEnrollmentId())) {
            return "The student must have an active enrollment with lessons remaining.";
        }
        LearningPackage learningPackage = packages.findByIdStrict(profile.getActivePackageId())
                .orElseThrow(() -> new RecordNotFoundException("Active package not found."));
        if (!learningPackage.isActive()) return "The student's learning package is inactive.";

        // Step 2: Check the instructor is active, working, present and qualified.
        Instructor instructor = instructors.findByIdStrict(instructorId)
                .orElseThrow(() -> new RecordNotFoundException("Instructor not found."));
        if (!instructor.isActive()) return "The selected instructor is inactive.";
        if (!isWorking(instructorId, start, end)) return "The instructor is not working at the selected time.";
        if (isOnLeave(instructorId, start)) return "The instructor is on leave on that date.";
        PackageType type = learningPackage.getType();
        if (!isQualified(instructor, type)) return "The instructor is not qualified for this package vehicle type.";

        // Step 3: If a vehicle is being assigned, check its status and course type.
        Vehicle vehicle = null;
        if (vehicleId != null) {
            vehicle = vehicles.findByIdStrict(vehicleId)
                    .orElseThrow(() -> new RecordNotFoundException("Vehicle not found."));
            if (vehicle.getOperationalStatus() != VehicleOperationalStatus.IN_SERVICE) {
                return "The vehicle is in maintenance or unavailable.";
            }
            if (!matches(type, vehicle)) return "The vehicle type does not match the learning package.";
        }

        // Step 4: Keep the student, instructor and vehicle free of overlapping promises.
        for (Booking other : bookings.readAllStrict()) {
            if (other.getId().equals(target.getId()) || other.getId().equals(ignoredBookingId)
                    || !active(other.getStatus())
                    || !LessonIntervalUtils.overlaps(start, end,
                    other.getStartTime(), other.getEndTime())) continue;
            if (target.getStudentId().equals(other.getStudentId())) return "The student already has a lesson at that time.";
            if (instructorId.equals(other.getInstructorId())
                    || instructorId.equals(other.getRequestedInstructorId())) return "The instructor already has a lesson at that time.";
            if (vehicleId != null && vehicleId.equals(other.getVehicleId())) return "The vehicle already has a lesson at that time.";
        }
        return null;
    }

    // Checks whether a booking still reserves time in the schedule.
    private boolean active(Booking.BookingStatus status) {
        return status == Booking.BookingStatus.PENDING || status == Booking.BookingStatus.CONFIRMED;
    }

    // Keeps the is working step in one place for this module.
    private boolean isWorking(String instructorId, LocalDateTime start, LocalDateTime end) {
        if (!start.toLocalDate().equals(end.toLocalDate())) return false;
        for (Availability block : availability.readAllStrict()) {
            if (block.getInstructorId().equals(instructorId) && block.getDayOfWeek() == start.getDayOfWeek()
                    && !start.toLocalTime().isBefore(block.getStartTime())
                    && !end.toLocalTime().isAfter(block.getEndTime())) return true;
        }
        return false;
    }

    // Checks the saved day-off list before offering times.
    private boolean isOnLeave(String instructorId, LocalDateTime start) {
        for (InstructorLeave leave : leaves.readAllStrict()) {
            if (leave.getInstructorId().equals(instructorId) && leave.getDate().equals(start.toLocalDate())) return true;
        }
        return false;
    }

    // Keeps the is qualified step in one place for this module.
    private boolean isQualified(Instructor instructor, PackageType type) {
        if (instructor.getVehicleTypes().isEmpty()) return true;
        for (String taughtType : instructor.getVehicleTypes()) {
            if (taughtType.toUpperCase(Locale.ROOT).contains(type.name())) return true;
        }
        return false;
    }

    // Checks whether the vehicle category and transmission fit the chosen course.
    private boolean matches(PackageType type, Vehicle vehicle) {
        if (type == PackageType.MOTORCYCLE) return vehicle.getVehicleCategory() == VehicleCategory.MOTORCYCLE;
        if (vehicle.getVehicleCategory() != VehicleCategory.CAR) return false;
        if (type == PackageType.MANUAL) return vehicle.getTransmissionType() == TransmissionType.MANUAL;
        if (type == PackageType.AUTOMATIC) return vehicle.getTransmissionType() == TransmissionType.AUTOMATIC;
        return type == PackageType.REFRESHER;
    }
}
