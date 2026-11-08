// Module owner: IT26101133
package com.drivingschool.instructor.model;

import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.common.model.Identifiable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class Availability implements Identifiable {
    private final String id;
    private final String instructorId;
    private final DayOfWeek dayOfWeek;
    private final LocalTime startTime;
    private final LocalTime endTime;

    @JsonCreator
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

    @Override public String getId() { return id; }
    public String getInstructorId() { return instructorId; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
}
