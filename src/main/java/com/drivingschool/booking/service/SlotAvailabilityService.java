/**
============================================================
MEMBER   : IT26101134 - [Name]
MODULE   : Bookings, FIFO queue and scheduling
FILE     : SlotAvailabilityService.java
WHAT IT DOES (simple words): Applies the slot availability rules.
REAL-LIFE EXAMPLE: A reception waiting line handles the oldest request first.
OOP USED : Encapsulation (private fields and public methods)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/
package com.drivingschool.booking.service;

import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.booking.util.LessonIntervalUtils;
import com.drivingschool.instructor.model.Availability;
import com.drivingschool.instructor.model.InstructorLeave;
import com.drivingschool.instructor.repository.AvailabilityRepository;
import com.drivingschool.instructor.repository.InstructorLeaveRepository;
import org.springframework.stereotype.Service;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
// SOLID: S - This class is responsible for application rules.
public class SlotAvailabilityService {
    public static final int LESSON_MINUTES = 60;
    private final AvailabilityRepository availability;
    private final InstructorLeaveRepository leaves;
    private final BookingRepository bookings;
    private final Clock clock;

    // Receives the helpers or validates the details needed to create this object.
    public SlotAvailabilityService(AvailabilityRepository availability,
            InstructorLeaveRepository leaves, BookingRepository bookings, Clock clock) {
        this.availability = availability;
        this.leaves = leaves;
        this.bookings = bookings;
        this.clock = clock;
    }

    // Builds future one-hour options that fit the instructor's hours and avoid conflicts.
    public List<LocalDateTime> availableSlots(String instructorId, LocalDate date) {
        return availableSlots(instructorId, date, null, null);
    }

    // Builds future one-hour options that fit the instructor's hours and avoid conflicts.
    public List<LocalDateTime> availableSlots(String instructorId, LocalDate date, String excludingBookingId) {
        return availableSlots(instructorId, date, excludingBookingId, null);
    }

    // Builds future one-hour options that fit the instructor's hours and avoid conflicts.
    public List<LocalDateTime> availableSlots(String instructorId, LocalDate date,
            String excludingBookingId, String studentId) {
        // Step 1: A day off cannot offer any teaching slots.
        if (instructorId == null || date == null || isOnLeave(instructorId, date)) return List.of();
        List<LocalDateTime> result = new ArrayList<>();
        List<Booking> savedBookings = bookings.readAllStrict();
        // Step 2: Build one-hour options only inside this instructor's working blocks.
        for (Availability block : availability.readAllStrict()) {
            if (block.getInstructorId().equals(instructorId) && block.getDayOfWeek() == date.getDayOfWeek()) {
                addBlockSlots(result, block, date, savedBookings, excludingBookingId, studentId);
            }
        }
        return List.copyOf(result);
    }

    // Adds future free hours from one saved working block.
    private void addBlockSlots(List<LocalDateTime> result, Availability block, LocalDate date,
            List<Booking> savedBookings, String excludedId, String studentId) {
        LocalDateTime start = date.atTime(block.getStartTime());
        LocalDateTime blockEnd = date.atTime(block.getEndTime());
        LocalDateTime now = LocalDateTime.now(clock);
        // Including the date prevents 23:00 + one hour from wrapping back to 00:00 forever.
        while (!start.plusMinutes(LESSON_MINUTES).isAfter(blockEnd)) {
            LocalDateTime end = start.plusMinutes(LESSON_MINUTES);
            if (start.isAfter(now) && !slotIsBusy(savedBookings, block.getInstructorId(), studentId,
                    excludedId, start, end)) insertSlot(result, start);
            start = end;
        }
    }

    // Checks the saved day-off list before offering times.
    private boolean isOnLeave(String instructorId, LocalDate date) {
        for (InstructorLeave leave : leaves.readAllStrict()) {
            if (leave.getInstructorId().equals(instructorId) && leave.getDate().equals(date)) return true;
        }
        return false;
    }

    // Checks instructor and student overlaps using the same boundaries as scheduling.
    private boolean slotIsBusy(List<Booking> saved, String instructorId, String studentId,
            String excludedId, LocalDateTime start, LocalDateTime end) {
        for (Booking booking : saved) {
            if (booking.getId().equals(excludedId) || !active(booking.getStatus())) continue;
            boolean sameInstructor = instructorId.equals(booking.getInstructorId())
                    || instructorId.equals(booking.getRequestedInstructorId());
            boolean sameStudent = studentId != null && studentId.equals(booking.getStudentId());
            if ((sameInstructor || sameStudent) && LessonIntervalUtils.overlaps(start, end,
                    booking.getStartTime(), booking.getEndTime())) return true;
        }
        return false;
    }

    // Keeps the insert slot step in one place for this module.
    private void insertSlot(List<LocalDateTime> result, LocalDateTime slot) {
        if (result.contains(slot)) return;
        int index = result.size();
        while (index > 0 && result.get(index - 1).isAfter(slot)) index--;
        result.add(index, slot);
    }

    // Checks whether a booking still reserves time in the schedule.
    private boolean active(BookingStatus status) {
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }
}
