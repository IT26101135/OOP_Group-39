/**
============================================================
MEMBER   : IT26101133 - [Name]
MODULE   : Instructors and bubble sort
FILE     : InstructorScheduleOperations.java
WHAT IT DOES (simple words): Applies the instructor schedule operations rules.
REAL-LIFE EXAMPLE: A teacher diary keeps teaching hours, lesson notes and profile details organised.
OOP USED : Abstraction (a small interface)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/
package com.drivingschool.instructor.service;

import com.drivingschool.instructor.model.Availability;
import com.drivingschool.instructor.model.InstructorLeave;
import jakarta.servlet.http.HttpServletRequest;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// OOP: ABSTRACTION - Callers use this promise instead of the implementation details.
// SOLID: I - This interface describes one small job for its callers.
public interface InstructorScheduleOperations {
    // Lists only the signed-in instructor's weekly working blocks.
    List<Availability> availability(HttpServletRequest request);
    // Lists only the signed-in instructor's recorded days off, including history.
    List<InstructorLeave> leaves(HttpServletRequest request);
    // Adds working hours only when their times are valid and do not overlap.
    Availability addAvailability(HttpServletRequest request, DayOfWeek day, LocalTime start, LocalTime end);
    // Edits owned working hours while preserving confirmed lessons and avoiding overlap.
    Availability updateAvailability(HttpServletRequest request, String id, DayOfWeek day, LocalTime start, LocalTime end);
    // Records a future day off after checking for duplicate leave and confirmed lessons.
    InstructorLeave addLeave(HttpServletRequest request, LocalDate date, String reason);
    // Changes an owned leave record after checking its new date is safe.
    InstructorLeave updateLeave(HttpServletRequest request, String id, LocalDate date, String reason);
    // Removes only a working block belonging to this instructor.
    void deleteAvailability(HttpServletRequest request, String id);
    // Removes only a leave record belonging to this instructor.
    void deleteLeave(HttpServletRequest request, String id);
}
