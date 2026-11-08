// Module owner: IT26101133
package com.drivingschool.instructor.service;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.service.AuthSessionService;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class InstructorScheduleService {
    private final AvailabilityRepository availability;
    private final InstructorLeaveRepository leaves;
    private final BookingRepository bookings;
    private final AuthSessionService auth;

    public InstructorScheduleService(AvailabilityRepository availability,
                                     InstructorLeaveRepository leaves,
                                     BookingRepository bookings,
                                     AuthSessionService auth) {
        this.availability = availability;
        this.leaves = leaves;
        this.bookings = bookings;
        this.auth = auth;
    }

    public List<Availability> availability(HttpServletRequest request) {
        String id = requireInstructor(request).getId();
        return availability.readAllStrict().stream()
                .filter(block -> block.getInstructorId().equals(id)).toList();
    }

    public List<InstructorLeave> leaves(HttpServletRequest request) {
        String id = requireInstructor(request).getId();
        return leaves.readAllStrict().stream()
                .filter(leave -> leave.getInstructorId().equals(id)).toList();
    }

    public Availability addAvailability(HttpServletRequest request,
                                        DayOfWeek day, LocalTime start,
                                        LocalTime end) {
        String id = requireInstructor(request).getId();
        if (day == null || start == null || end == null || !end.isAfter(start)) {
            throw new ValidationException("Availability end must be after its start.");
        }
        for (Availability other : availability.readAllStrict()) {
            if (other.getInstructorId().equals(id) && other.getDayOfWeek() == day
                    && start.isBefore(other.getEndTime())
                    && other.getStartTime().isBefore(end)) {
                throw new DuplicateRecordException("Availability blocks cannot overlap.");
            }
        }
        Availability block = new Availability(availability.nextId("H"),
                id, day, start, end);
        availability.save(block);
        return block;
    }

    public InstructorLeave addLeave(HttpServletRequest request,
                                    LocalDate date, String reason) {
        String id = requireInstructor(request).getId();
        if (leaves.readAllStrict().stream().anyMatch(leave ->
                leave.getInstructorId().equals(id) && leave.getDate().equals(date))) {
            throw new DuplicateRecordException("A leave day is already recorded for this date.");
        }
        boolean hasConfirmedLesson = bookings.readAllStrict().stream()
                .anyMatch(booking -> id.equals(booking.getInstructorId())
                        && booking.getStartTime().toLocalDate().equals(date)
                        && booking.getStatus() == BookingStatus.CONFIRMED);
        if (hasConfirmedLesson) {
            throw new ValidationException("Reschedule confirmed lessons before adding leave.");
        }
        InstructorLeave leave = new InstructorLeave(leaves.nextId("L"), id, date, reason);
        leaves.save(leave);
        return leave;
    }

    public void deleteAvailability(HttpServletRequest request, String availabilityId) {
        String instructorId = requireInstructor(request).getId();
        Availability block = availability.findByIdStrict(availabilityId)
                .orElseThrow(() -> new RecordNotFoundException("Availability block not found."));
        requireOwner(instructorId, block.getInstructorId());
        availability.delete(availabilityId);
    }

    public void deleteLeave(HttpServletRequest request, String leaveId) {
        String instructorId = requireInstructor(request).getId();
        InstructorLeave leave = leaves.findByIdStrict(leaveId)
                .orElseThrow(() -> new RecordNotFoundException("Leave record not found."));
        requireOwner(instructorId, leave.getInstructorId());
        leaves.delete(leaveId);
    }

    private User requireInstructor(HttpServletRequest request) {
        User user = auth.requireLogin(request);
        if (user.getRole() != Role.INSTRUCTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Instructor access is required.");
        }
        return user;
    }

    private void requireOwner(String requestedId, String actualId) {
        if (!requestedId.equals(actualId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You may only change your own schedule.");
        }
    }
}
