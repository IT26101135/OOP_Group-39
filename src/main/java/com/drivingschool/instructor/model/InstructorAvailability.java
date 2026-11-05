package com.drivingschool.instructor.model;

import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.common.model.Identifiable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public final class InstructorAvailability implements Identifiable {

    private final String id;
    private final String instructorId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public InstructorAvailability(
            @JsonProperty(value = "id", required = true) String id,
            @JsonProperty(value = "instructorId", required = true) String instructorId,
            @JsonProperty(value = "startTime", required = true) LocalDateTime startTime,
            @JsonProperty(value = "endTime", required = true) LocalDateTime endTime) {

        if (id == null || !id.matches("T(?!000)[0-9]{3}")) {
            throw new ValidationException(
                    "Availability ID must be between T001 and T999.");
        }

        if (instructorId == null
                || !instructorId.matches("I(?!000)[0-9]{3}")) {
            throw new ValidationException(
                    "Instructor ID must be between I001 and I999.");
        }

        if (startTime == null || endTime == null
                || !startTime.isBefore(endTime)) {
            throw new ValidationException(
                    "Availability start time must be before end time.");
        }

        this.id = id;
        this.instructorId = instructorId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getInstructorId() {
        return instructorId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
}
