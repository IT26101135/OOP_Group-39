/**
============================================================
MEMBER   : IT26101134 - [Name]
MODULE   : Bookings, FIFO queue and scheduling
FILE     : Booking.java
WHAT IT DOES (simple words): Keeps the details and validation rules of booking.
REAL-LIFE EXAMPLE: A reception waiting line handles the oldest request first.
OOP USED : Encapsulation (private fields and public methods)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/

package com.drivingschool.booking.model;

import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.common.model.Identifiable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

// OOP: ENCAPSULATION - Private fields protect details; constructors and methods validate changes.
public class Booking implements Identifiable {

    private final String id;
    private final String studentId;
    private final String enrollmentId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final LocalDateTime createdAt;

    private String instructorId;
    private String vehicleId;
    private BookingStatus status;
    private String rejectionReason;
    private String requestedInstructorId;
    private String requestedVehicleId;

    @JsonCreator
    // Receives the helpers or validates the details needed to create this object.
    public Booking(
            @JsonProperty("id") String id,
            @JsonProperty("studentId") String studentId,
            @JsonProperty("enrollmentId") String enrollmentId,
            @JsonProperty("startTime") LocalDateTime startTime,
            @JsonProperty("endTime") LocalDateTime endTime,
            @JsonProperty("instructorId") String instructorId,
            @JsonProperty("vehicleId") String vehicleId,
            @JsonProperty("status") BookingStatus status,
            @JsonProperty("createdAt") LocalDateTime createdAt) {

        // Step 1: Give the request valid filing numbers for the booking and student.
        this.id = validateId(id, "B");
        this.studentId = validateId(studentId, "S");

        if (enrollmentId == null || enrollmentId.isBlank()) {
            throw new ValidationException(
                    "enrollmentId cannot be blank");
        }

        this.enrollmentId = enrollmentId;
        // Step 2: A lesson needs real times, with the end after the start.
        this.startTime = requireValue(startTime, "startTime");
        this.endTime = requireValue(endTime, "endTime");
        this.createdAt = requireValue(createdAt, "createdAt");

        if (!endTime.isAfter(startTime)) {
            throw new ValidationException(
                    "endTime must be after startTime");
        }

        this.status = requireValue(status, "status");

        validateOptionalId(instructorId, "I", "instructorId");
        validateOptionalId(vehicleId, "V", "vehicleId");

        // Step 3: Instructor and vehicle must be assigned together for this status.
        validateAssignmentState(
                status, instructorId, vehicleId);

        this.instructorId = instructorId;
        this.vehicleId = vehicleId;
    }

    // Stops invalid id before the record can be saved.
    private static String validateId(
            String value, String prefix) {

        if (value == null
                || !value.matches(prefix + "[0-9]{3}")) {
            throw new ValidationException(
                    "ID must match " + prefix + "001 to "
                            + prefix + "999");
        }

        int number = Integer.parseInt(value.substring(1));

        if (number < 1 || number > 999) {
            throw new ValidationException(
                    "ID number must be between 001 and 999");
        }

        return value;
    }

    // Stops invalid optional id before the record can be saved.
    private static void validateOptionalId(
            String value, String prefix, String fieldName) {

        if (value != null) {
            try {
                validateId(value, prefix);
            } catch (ValidationException exception) {
                throw new ValidationException(
                        fieldName + " must be null or a valid "
                                + prefix + "001 to " + prefix + "999 ID");
            }
        }
    }

    // Stops invalid assignment state before the record can be saved.
    private static void validateAssignmentState(
            BookingStatus status,
            String instructorId,
            String vehicleId) {

        boolean hasInstructor = instructorId != null;
        boolean hasVehicle = vehicleId != null;

        if (hasInstructor != hasVehicle) {
            throw new ValidationException(
                    "Instructor and vehicle must be assigned together");
        }

        switch (status) {
            case PENDING:
                if (hasInstructor != hasVehicle) {
                    throw new ValidationException(
                            "Pending assignment must include both instructor and vehicle.");
                }
                break;

            case CONFIRMED:
            case COMPLETED:
            case NO_SHOW:
                if (!hasInstructor) {
                    throw new ValidationException(
                            status + " bookings require both assignments");
                }
                break;

            case CANCELLED:
            case REJECTED:
                // Both null or both assigned are allowed.
                break;

            default:
                throw new ValidationException(
                        "Unsupported booking status");
        }
    }

    // T means the same kind of value comes back: this one null check serves dates and statuses.
    // Keeps the require value step in one place for this module.
    private static <T> T requireValue(
            T value, String fieldName) {

        if (value == null) {
            throw new ValidationException(
                    fieldName + " cannot be null");
        }

        return value;
    }

    @Override
    // Lets callers read the id without exposing the private field.
    public String getId() {
        return id;
    }

    // Lets callers read the student id without exposing the private field.
    public String getStudentId() {
        return studentId;
    }

    // Lets callers read the enrollment id without exposing the private field.
    public String getEnrollmentId() {
        return enrollmentId;
    }

    // Lets callers read the start time without exposing the private field.
    public LocalDateTime getStartTime() {
        return startTime;
    }

    // Lets callers read the end time without exposing the private field.
    public LocalDateTime getEndTime() {
        return endTime;
    }

    // Lets callers read the instructor id without exposing the private field.
    public String getInstructorId() {
        return instructorId;
    }

    // Lets callers read the vehicle id without exposing the private field.
    public String getVehicleId() {
        return vehicleId;
    }

    // Lets callers read the status without exposing the private field.
    public BookingStatus getStatus() {
        return status;
    }

    // Lets callers read the created at without exposing the private field.
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Lets callers read the rejection reason without exposing the private field.
    public String getRejectionReason() { return rejectionReason; }

    // Changes the rejection reason through the record's validation rules.
    public void setRejectionReason(String rejectionReason) {
        if (rejectionReason != null && rejectionReason.length() > 300) {
            throw new ValidationException("Rejection reason is too long.");
        }
        this.rejectionReason = rejectionReason;
    }

    // Lets callers read the requested instructor id without exposing the private field.
    public String getRequestedInstructorId() { return requestedInstructorId; }
    // Changes the requested instructor id through the record's validation rules.
    public void setRequestedInstructorId(String id) {
        validateOptionalId(id, "I", "requestedInstructorId");
        this.requestedInstructorId = id;
    }
    // Lets callers read the requested vehicle id without exposing the private field.
    public String getRequestedVehicleId() { return requestedVehicleId; }
    // Changes the requested vehicle id through the record's validation rules.
    public void setRequestedVehicleId(String id) {
        validateOptionalId(id, "V", "requestedVehicleId");
        this.requestedVehicleId = id;
    }

    // Keeps the reject step in one place for this module.
    public void reject(String reason) {
        if (status != BookingStatus.PENDING) {
            throw new ValidationException("Only pending requests can be rejected.");
        }
        if (reason == null || reason.isBlank() || reason.trim().length() > 300) {
            throw new ValidationException("A rejection reason up to 300 characters is required.");
        }
        status = BookingStatus.REJECTED;
        rejectionReason = reason.trim();
    }

    // Keeps the confirm step in one place for this module.
    public void confirm(String instructorId, String vehicleId) {
        if (status != BookingStatus.PENDING) {
            throw new ValidationException(
                    "Only PENDING bookings can be confirmed");
        }

        validateId(instructorId, "I");
        validateId(vehicleId, "V");

        // Apply the complete assignment and status together.
        this.instructorId = instructorId;
        this.vehicleId = vehicleId;
        this.status = BookingStatus.CONFIRMED;
    }

    /** Stores an administrator's proposed assignment while instructor approval is pending. */
    // Keeps the assign for instructor step in one place for this module.
    public void assignForInstructor(String instructorId, String vehicleId) {
        if (status != BookingStatus.PENDING) {
            throw new ValidationException("Only pending requests can be assigned.");
        }
        validateId(instructorId, "I");
        validateId(vehicleId, "V");
        this.instructorId = instructorId;
        this.vehicleId = vehicleId;
    }

    // Marks a confirmed lesson complete through the booking rules.
    public void complete() {
        complete(LocalDateTime.now(java.time.ZoneId.of("Asia/Colombo")), true);
    }

    // Step 1: Check attendance time before changing the saved lesson state.
    public void complete(LocalDateTime now, boolean requireLessonEnded) {
        if (status != BookingStatus.CONFIRMED) {
            throw new ValidationException(
                    "Only CONFIRMED bookings can be completed");
        }

        if (requireLessonEnded && now.isBefore(endTime)) {
            throw new ValidationException("This lesson has not ended yet. Completion becomes available at "
                    + endTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    + " (Asia/Colombo).");
        }
        this.status = BookingStatus.COMPLETED;
    }

    // Keeps the mark no show step in one place for this module.
    public void markNoShow() {
        markNoShow(LocalDateTime.now(java.time.ZoneId.of("Asia/Colombo")), true);
    }

    // Step 1: A student cannot be absent before the lesson starts.
    public void markNoShow(LocalDateTime now, boolean requireLessonEnded) {
        if (status != BookingStatus.CONFIRMED) {
            throw new ValidationException("Only confirmed lessons can be marked no-show.");
        }
        if (requireLessonEnded && now.isBefore(startTime)) {
            throw new ValidationException("This lesson has not started yet. No-show becomes available at "
                    + startTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    + " (Asia/Colombo).");
        }
        status = BookingStatus.NO_SHOW;
    }

    // Sends the owned request through the cancellation and notice-period checks.
    public void cancel() {
        if (status != BookingStatus.PENDING
                && status != BookingStatus.CONFIRMED) {
            throw new ValidationException(
                    "Only PENDING or CONFIRMED bookings can be cancelled");
        }

        // Preserve assignments when cancelling a confirmed booking.
        this.status = BookingStatus.CANCELLED;
    }

    // Checks conflicts before changing an existing confirmed lesson's resources.
    public void reassign(String newInstructorId, String newVehicleId) {
        if (status != BookingStatus.CONFIRMED) throw new ValidationException("Only confirmed lessons can be reassigned.");
        validateId(newInstructorId, "I");
        validateId(newVehicleId, "V");
        instructorId = newInstructorId;
        vehicleId = newVehicleId;
    }

    // Stores the administrator's reason when a lesson must be cancelled.
    public void forceCancel(String reason) {
        if (status == BookingStatus.COMPLETED || status == BookingStatus.CANCELLED)
            throw new ValidationException("Completed or cancelled lessons cannot be force-cancelled.");
        if (reason == null || reason.isBlank() || reason.trim().length() > 300)
            throw new ValidationException("Enter a force-cancellation reason up to 300 characters.");
        status = BookingStatus.CANCELLED;
        rejectionReason = reason.trim();
    }
}
