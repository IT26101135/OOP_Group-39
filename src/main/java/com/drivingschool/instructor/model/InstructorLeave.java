/**
============================================================
MEMBER   : IT26101133 - [Name]
MODULE   : Instructors and bubble sort
FILE     : InstructorLeave.java
WHAT IT DOES (simple words): Keeps the details and validation rules of instructor leave.
REAL-LIFE EXAMPLE: A teacher diary keeps teaching hours, lesson notes and profile details organised.
OOP USED : Encapsulation (private fields and public methods)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/
package com.drivingschool.instructor.model;

import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.common.model.Identifiable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

// OOP: ENCAPSULATION - Private fields protect details; constructors and methods validate changes.
public class InstructorLeave implements Identifiable {
    private final String id;
    private final String instructorId;
    private final LocalDate date;
    private final String reason;

    @JsonCreator
    // Receives the helpers or validates the details needed to create this object.
    public InstructorLeave(@JsonProperty("id") String id,
                           @JsonProperty("instructorId") String instructorId,
                           @JsonProperty("date") LocalDate date,
                           @JsonProperty("reason") String reason) {
        if (id == null || !id.matches("L(?!000)[0-9]{3}")) throw new ValidationException("Leave ID is invalid.");
        if (instructorId == null || !instructorId.matches("I(?!000)[0-9]{3}")) throw new ValidationException("Instructor ID is invalid.");
        // Old leave days are still valid history when a file is read tomorrow.
        // The schedule service checks future dates when accepting a new request.
        if (date == null) throw new ValidationException("Leave date is required.");
        if (reason == null || reason.isBlank() || reason.trim().length() > 300) throw new ValidationException("Enter a leave reason up to 300 characters.");
        this.id = id;
        this.instructorId = instructorId;
        this.date = date;
        this.reason = reason.trim();
    }

    // Lets callers read the id without exposing the private field.
    @Override public String getId() { return id; }
    // Lets callers read the instructor id without exposing the private field.
    public String getInstructorId() { return instructorId; }
    // Lets callers read the date without exposing the private field.
    public LocalDate getDate() { return date; }
    // Lets callers read the reason without exposing the private field.
    public String getReason() { return reason; }
}
