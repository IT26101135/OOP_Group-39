/**
============================================================
MEMBER   : IT26101133 - [Name]
MODULE   : Instructors and bubble sort
FILE     : InstructorScheduleService.java
WHAT IT DOES (simple words): Applies the instructor schedule rules.
REAL-LIFE EXAMPLE: A teacher diary keeps teaching hours, lesson notes and profile details organised.
OOP USED : Encapsulation (private fields and public methods)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/
package com.drivingschool.instructor.service;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.auth.util.AccountWriteCoordinator;
import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.instructor.model.Availability;
import com.drivingschool.instructor.model.InstructorLeave;
import com.drivingschool.instructor.repository.AvailabilityRepository;
import com.drivingschool.instructor.repository.InstructorLeaveRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
// SOLID: S - This class is responsible for application rules.
public class InstructorScheduleService implements InstructorScheduleOperations {
    private final AvailabilityRepository availability;
    private final InstructorLeaveRepository leaves;
    private final BookingRepository bookings;
    private final AuthSessionService auth;
    private final Clock clock;

    // Receives the helpers or validates the details needed to create this object.
    public InstructorScheduleService(AvailabilityRepository availability,
            InstructorLeaveRepository leaves, BookingRepository bookings,
            AuthSessionService auth, Clock clock) {
        this.availability = availability;
        this.leaves = leaves;
        this.bookings = bookings;
        this.auth = auth;
        this.clock = clock;
    }

    // Lists only the signed-in instructor's weekly working blocks.
    // CRUD: READ   - finds or lists availability blocks
    public List<Availability> availability(HttpServletRequest request) {
        String instructorId = requireInstructor(request).getId();
        List<Availability> ownBlocks = new ArrayList<>();
        for (Availability block : availability.readAllStrict()) {
            if (block.getInstructorId().equals(instructorId)) ownBlocks.add(block);
        }
        return ownBlocks;
    }

    // Lists only the signed-in instructor's recorded days off, including history.
    // CRUD: READ   - finds or lists leave records
    public List<InstructorLeave> leaves(HttpServletRequest request) {
        String instructorId = requireInstructor(request).getId();
        List<InstructorLeave> ownLeave = new ArrayList<>();
        for (InstructorLeave leave : leaves.readAllStrict()) {
            if (leave.getInstructorId().equals(instructorId)) ownLeave.add(leave);
        }
        return ownLeave;
    }

    // Adds working hours only when their times are valid and do not overlap.
    // CRUD: CREATE - adds a new availability block and saves it
    public Availability addAvailability(HttpServletRequest request,
            DayOfWeek day, LocalTime start, LocalTime end) {
        synchronized (AccountWriteCoordinator.class) {
            // Step 1: Validate the hours and check for an overlapping block.
            String instructorId = requireInstructor(request).getId();
            Availability checked = new Availability("H001", instructorId, day, start, end);
            ensureNoOverlap(checked, null);
            // Step 2: Give the checked block its permanent filing number and save it.
            Availability block = new Availability(availability.nextId("H"), instructorId, day, start, end);
            availability.save(block);
            return block;
        }
    }

    // Edits owned working hours while preserving confirmed lessons and avoiding overlap.
    // CRUD: UPDATE - changes an existing availability block
    public Availability updateAvailability(HttpServletRequest request,
            String id, DayOfWeek day, LocalTime start, LocalTime end) {
        synchronized (AccountWriteCoordinator.class) {
            // Step 1: Find this instructor's block before considering changes.
            String instructorId = requireInstructor(request).getId();
            Availability current = ownBlock(instructorId, id);
            Availability replacement = new Availability(id, instructorId, day, start, end);
            // Step 2: Preserve confirmed lessons and reject overlapping hours.
            ensureBlockHasNoConfirmedLessons(current);
            ensureNoOverlap(replacement, id);
            availability.update(replacement);
            return replacement;
        }
    }

    // Records a future day off after checking for duplicate leave and confirmed lessons.
    // CRUD: CREATE - adds a new leave record and saves it
    public InstructorLeave addLeave(HttpServletRequest request, LocalDate date, String reason) {
        synchronized (AccountWriteCoordinator.class) {
            String instructorId = requireInstructor(request).getId();
            // New requests must be today or later; stored historical records remain readable.
            validateLeave(instructorId, date, null);
            InstructorLeave leave = new InstructorLeave(leaves.nextId("L"), instructorId, date, reason);
            leaves.save(leave);
            return leave;
        }
    }

    // Changes an owned leave record after checking its new date is safe.
    // CRUD: UPDATE - changes an existing leave record
    public InstructorLeave updateLeave(HttpServletRequest request,
            String id, LocalDate date, String reason) {
        synchronized (AccountWriteCoordinator.class) {
            String instructorId = requireInstructor(request).getId();
            ownLeave(instructorId, id);
            validateLeave(instructorId, date, id);
            InstructorLeave replacement = new InstructorLeave(id, instructorId, date, reason);
            leaves.update(replacement);
            return replacement;
        }
    }

    // Removes only a working block belonging to this instructor.
    // CRUD: DELETE - removes a availability block
    public void deleteAvailability(HttpServletRequest request, String id) {
        synchronized (AccountWriteCoordinator.class) {
            Availability block = ownBlock(requireInstructor(request).getId(), id);
            ensureBlockHasNoConfirmedLessons(block);
            availability.delete(id);
        }
    }

    // Removes only a leave record belonging to this instructor.
    // CRUD: DELETE - removes a leave record
    public void deleteLeave(HttpServletRequest request, String id) {
        synchronized (AccountWriteCoordinator.class) {
            ownLeave(requireInstructor(request).getId(), id);
            leaves.delete(id);
        }
    }

    // Keeps one teaching hour from appearing in two blocks on the same day.
    // OOP: EXCEPTION HANDLING - DuplicateRecordException becomes a friendly conflict page.
    private void ensureNoOverlap(Availability checked, String excludedId) {
        for (Availability other : availability.readAllStrict()) {
            if (!other.getId().equals(excludedId)
                    && other.getInstructorId().equals(checked.getInstructorId())
                    && other.getDayOfWeek() == checked.getDayOfWeek()
                    && checked.getStartTime().isBefore(other.getEndTime())
                    && other.getStartTime().isBefore(checked.getEndTime())) {
                throw new DuplicateRecordException("Availability blocks cannot overlap.");
            }
        }
    }

    // Stops invalid leave before the record can be saved.
    private void validateLeave(String instructorId, LocalDate date, String excludedId) {
        if (date == null || date.isBefore(LocalDate.now(clock))) {
            throw new ValidationException("Leave date must be today or later.");
        }
        for (InstructorLeave leave : leaves.readAllStrict()) {
            if (!leave.getId().equals(excludedId) && leave.getInstructorId().equals(instructorId)
                    && leave.getDate().equals(date)) {
                throw new DuplicateRecordException("A leave day is already recorded for this date.");
            }
        }
        ensureDayHasNoConfirmedLessons(instructorId, date);
        for (Booking booking : bookings.readAllStrict()) {
            if (instructorId.equals(booking.getInstructorId())
                    && booking.getStartTime().toLocalDate().equals(date)
                    && booking.getStatus() == BookingStatus.PENDING) {
                throw new ValidationException("You have an assigned lesson waiting for your decision on this day. "
                        + "Ask an administrator to resolve it before adding leave.");
            }
        }
    }

    // Refuses leave if a student is already promised a lesson on that day.
    private void ensureDayHasNoConfirmedLessons(String instructorId, LocalDate date) {
        for (Booking booking : bookings.readAllStrict()) {
            if (instructorId.equals(booking.getInstructorId())
                    && booking.getStartTime().toLocalDate().equals(date)
                    && booking.getStatus() == BookingStatus.CONFIRMED) {
                throw new ValidationException("Reschedule confirmed lessons before adding leave.");
            }
        }
    }

    // Keeps edited hours from invalidating lessons already promised to students.
    private void ensureBlockHasNoConfirmedLessons(Availability block) {
        for (Booking booking : bookings.readAllStrict()) {
            if (block.getInstructorId().equals(booking.getInstructorId())
                    && booking.getStatus() == BookingStatus.CONFIRMED
                    && booking.getStartTime().getDayOfWeek() == block.getDayOfWeek()
                    && !booking.getStartTime().toLocalTime().isBefore(block.getStartTime())
                    && booking.getStartTime().toLocalTime().isBefore(block.getEndTime())) {
                throw new ValidationException("Ask an administrator to reschedule confirmed lessons before changing or removing these working hours.");
            }
        }
    }

    // Finds the selected working block and checks who owns it.
    private Availability ownBlock(String instructorId, String id) {
        Availability block = availability.findByIdStrict(id)
                .orElseThrow(() -> new RecordNotFoundException("Availability block not found."));
        requireOwner(instructorId, block.getInstructorId());
        return block;
    }

    // Finds the selected leave record and checks who owns it.
    private InstructorLeave ownLeave(String instructorId, String id) {
        InstructorLeave leave = leaves.findByIdStrict(id)
                .orElseThrow(() -> new RecordNotFoundException("Leave record not found."));
        requireOwner(instructorId, leave.getInstructorId());
        return leave;
    }

    // Refuses instructor-only work unless the signed-in account has that role.
    private User requireInstructor(HttpServletRequest request) {
        User user = auth.requireLogin(request);
        if (user.getRole() != Role.INSTRUCTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Instructor access is required.");
        }
        return user;
    }

    // Keeps instructors from changing someone else's schedule.
    private void requireOwner(String requestedId, String actualId) {
        if (!requestedId.equals(actualId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You may only change your own schedule.");
        }
    }
}
