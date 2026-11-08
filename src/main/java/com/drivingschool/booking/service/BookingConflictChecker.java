// Module owner: IT26101134
package com.drivingschool.booking.service;

import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
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
import java.util.Locale;

/** Central server-side rules for deciding whether a lesson can be confirmed. */
@Service
public class BookingConflictChecker {
    private final BookingRepository bookings;
    private final AvailabilityRepository availability;
    private final InstructorLeaveRepository leaves;
    private final InstructorRepository instructors;
    private final VehicleRepository vehicles;
    private final LearningPackageRepository packages;
    private final UserRepository users;
    private final Clock clock;

    public BookingConflictChecker(BookingRepository bookings,
                                  AvailabilityRepository availability,
                                  InstructorLeaveRepository leaves,
                                  InstructorRepository instructors,
                                  VehicleRepository vehicles,
                                  LearningPackageRepository packages,
                                  UserRepository users, Clock clock) {
        this.bookings = bookings;
        this.availability = availability;
        this.leaves = leaves;
        this.instructors = instructors;
        this.vehicles = vehicles;
        this.packages = packages;
        this.users = users;
        this.clock = clock;
    }

    /** Returns null when every applicable check passes; otherwise returns the first reason. */
    public String conflictReason(Booking target, String instructorId, String vehicleId) {
        return conflictReason(target, instructorId, vehicleId, null);
    }

    public String conflictReason(Booking target, String instructorId, String vehicleId,
                                 String ignoredBookingId) {
        LocalDateTime start = target.getStartTime();
        LocalDateTime end = target.getEndTime();
        if (!start.isAfter(LocalDateTime.now(clock))) return "The lesson time must be in the future.";

        var student = users.findByIdStrict(target.getStudentId())
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

        Instructor instructor = instructors.findByIdStrict(instructorId)
                .orElseThrow(() -> new RecordNotFoundException("Instructor not found."));
        if (!instructor.isActive()) return "The selected instructor is inactive.";
        boolean working = availability.readAllStrict().stream().anyMatch(block ->
                block.getInstructorId().equals(instructorId)
                        && block.getDayOfWeek() == start.getDayOfWeek()
                        && !start.toLocalTime().isBefore(block.getStartTime())
                        && !end.toLocalTime().isAfter(block.getEndTime()));
        if (!working) return "The instructor is not working at the selected time.";
        if (leaves.readAllStrict().stream().anyMatch(leave ->
                leave.getInstructorId().equals(instructorId)
                        && leave.getDate().equals(start.toLocalDate()))) {
            return "The instructor is on leave on that date.";
        }

        PackageType type = learningPackage.getType();
        if (!instructor.getVehicleTypes().isEmpty()
                && instructor.getVehicleTypes().stream().map(s -> s.toUpperCase(Locale.ROOT))
                .noneMatch(s -> s.contains(type.name()))) {
            return "The instructor is not qualified for this package vehicle type.";
        }

        Vehicle vehicle = null;
        if (vehicleId != null) {
            vehicle = vehicles.findByIdStrict(vehicleId)
                    .orElseThrow(() -> new RecordNotFoundException("Vehicle not found."));
            if (vehicle.getOperationalStatus() != VehicleOperationalStatus.IN_SERVICE) {
                return "The vehicle is in maintenance or unavailable.";
            }
            if (!matches(type, vehicle)) return "The vehicle type does not match the learning package.";
        }

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

    private boolean active(BookingStatus status) {
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }

    private boolean matches(PackageType type, Vehicle vehicle) {
        return switch (type) {
            case MOTORCYCLE -> vehicle.getVehicleCategory() == VehicleCategory.MOTORCYCLE;
            case MANUAL -> vehicle.getVehicleCategory() == VehicleCategory.CAR
                    && vehicle.getTransmissionType() == TransmissionType.MANUAL;
            case AUTOMATIC -> vehicle.getVehicleCategory() == VehicleCategory.CAR
                    && vehicle.getTransmissionType() == TransmissionType.AUTOMATIC;
            case REFRESHER -> vehicle.getVehicleCategory() == VehicleCategory.CAR;
        };
    }
}
