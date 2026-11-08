/**
============================================================
MEMBER   : IT26101133 - [Name]
MODULE   : Instructors and bubble sort
FILE     : Availability.java
WHAT IT DOES (simple words): Keeps the details and validation rules of availability.
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

import java.time.DayOfWeek;
import java.time.LocalTime;

// OOP: ENCAPSULATION - Private fields protect details; constructors and methods validate changes.
public class Availability implements Identifiable {
    private final String id;
    private final String instructorId;
    private final DayOfWeek dayOfWeek;
    private final LocalTime startTime;
    private final LocalTime endTime;

    @JsonCreator
    // Receives the helpers or validates the details needed to create this object.
    public Availability(@JsonProperty("id") String id,
                        @JsonProperty("instructorId") String instructorId,
                        @JsonProperty("dayOfWeek") DayOfWeek dayOfWeek,
                        @JsonProperty("startTime") LocalTime startTime,
                        @JsonProperty("endTime") LocalTime endTime) {
        if (id == null || !id.matches("H(?!000)[0-9]{3}")) throw new ValidationException("Availability ID is invalid.");
        if (instructorId == null || !instructorId.matches("I(?!000)[0-9]{3}")) throw new ValidationException("Instructor ID is invalid.");
        if (dayOfWeek == null || startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw new ValidationException("Availability must have a day and an end time after its start.");
        }
        this.id = id;
        this.instructorId = instructorId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Lets callers read the id without exposing the private field.
    @Override public String getId() { return id; }
    // Lets callers read the instructor id without exposing the private field.
    public String getInstructorId() { return instructorId; }
    // Lets callers read the day of week without exposing the private field.
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    // Lets callers read the start time without exposing the private field.
    public LocalTime getStartTime() { return startTime; }
    // Lets callers read the end time without exposing the private field.
    public LocalTime getEndTime() { return endTime; }
}
