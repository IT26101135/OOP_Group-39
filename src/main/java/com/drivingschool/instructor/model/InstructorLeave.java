// Module owner: IT26101133
package com.drivingschool.instructor.model;

import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.common.model.Identifiable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class InstructorLeave implements Identifiable {
    private final String id;
    private final String instructorId;
    private final LocalDate date;
    private final String reason;

    @JsonCreator
    public InstructorLeave(@JsonProperty("id") String id,
                           @JsonProperty("instructorId") String instructorId,
                           @JsonProperty("date") LocalDate date,
                           @JsonProperty("reason") String reason) {
        if (id == null || !id.matches("L(?!000)[0-9]{3}")) throw new ValidationException("Leave ID is invalid.");
        if (instructorId == null || !instructorId.matches("I(?!000)[0-9]{3}")) throw new ValidationException("Instructor ID is invalid.");
        if (date == null || date.isBefore(LocalDate.now())) throw new ValidationException("Leave date must be today or later.");
        if (reason == null || reason.isBlank() || reason.trim().length() > 300) throw new ValidationException("Enter a leave reason up to 300 characters.");
        this.id = id;
        this.instructorId = instructorId;
        this.date = date;
        this.reason = reason.trim();
    }

    @Override public String getId() { return id; }
    public String getInstructorId() { return instructorId; }
    public LocalDate getDate() { return date; }
    public String getReason() { return reason; }
}
