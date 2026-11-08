// Module owner: IT26101134

package com.drivingschool.booking.model;

import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.common.model.Identifiable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

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

        this.id = validateId(id, "B");
        this.studentId = validateId(studentId, "S");

        if (enrollmentId == null || enrollmentId.isBlank()) {
            throw new ValidationException(
                    "enrollmentId cannot be blank");
        }

        this.enrollmentId = enrollmentId;
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

        validateAssignmentState(
                status, instructorId, vehicleId);

        this.instructorId = instructorId;
        this.vehicleId = vehicleId;
    }

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

    private static <T> T requireValue(
            T value, String fieldName) {

        if (value == null) {
            throw new ValidationException(
                    fieldName + " cannot be null");
        }

        return value;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getInstructorId() {
        return instructorId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getRejectionReason() { return rejectionReason; }

    public void setRejectionReason(String rejectionReason) {
        if (rejectionReason != null && rejectionReason.length() > 300) {
            throw new ValidationException("Rejection reason is too long.");
        }
        this.rejectionReason = rejectionReason;
    }

    public String getRequestedInstructorId() { return requestedInstructorId; }
    public void setRequestedInstructorId(String id) {
        validateOptionalId(id, "I", "requestedInstructorId");
        this.requestedInstructorId = id;
    }
    public String getRequestedVehicleId() { return requestedVehicleId; }
    public void setRequestedVehicleId(String id) {
        validateOptionalId(id, "V", "requestedVehicleId");
        this.requestedVehicleId = id;
    }

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
    public void assignForInstructor(String instructorId, String vehicleId) {
        if (status != BookingStatus.PENDING) {
            throw new ValidationException("Only pending requests can be assigned.");
        }
        validateId(instructorId, "I");
        validateId(vehicleId, "V");
        this.instructorId = instructorId;
        this.vehicleId = vehicleId;
    }

    public void complete() {
        if (status != BookingStatus.CONFIRMED) {
            throw new ValidationException(
                    "Only CONFIRMED bookings can be completed");
        }

        this.status = BookingStatus.COMPLETED;
    }

    public void markNoShow() {
        if (status != BookingStatus.CONFIRMED) {
            throw new ValidationException("Only confirmed lessons can be marked no-show.");
        }
        status = BookingStatus.NO_SHOW;
    }

    public void cancel() {
        if (status != BookingStatus.PENDING
                && status != BookingStatus.CONFIRMED) {
            throw new ValidationException(
                    "Only PENDING or CONFIRMED bookings can be cancelled");
        }

        // Preserve assignments when cancelling a confirmed booking.
        this.status = BookingStatus.CANCELLED;
    }

    public void reassign(String newInstructorId, String newVehicleId) {
        if (status != BookingStatus.CONFIRMED) throw new ValidationException("Only confirmed lessons can be reassigned.");
        validateId(newInstructorId, "I");
        validateId(newVehicleId, "V");
        instructorId = newInstructorId;
        vehicleId = newVehicleId;
    }

    public void forceCancel(String reason) {
        if (status == BookingStatus.COMPLETED || status == BookingStatus.CANCELLED)
            throw new ValidationException("Completed or cancelled lessons cannot be force-cancelled.");
        if (reason == null || reason.isBlank() || reason.trim().length() > 300)
            throw new ValidationException("Enter a force-cancellation reason up to 300 characters.");
        status = BookingStatus.CANCELLED;
        rejectionReason = reason.trim();
    }
}
