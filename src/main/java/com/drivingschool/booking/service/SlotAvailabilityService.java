// Module owner: IT26101134
package com.drivingschool.booking.service;

import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.booking.util.LessonIntervalUtils;
import com.drivingschool.instructor.repository.AvailabilityRepository;
import com.drivingschool.instructor.repository.InstructorLeaveRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Builds one-hour lesson options from the instructor's saved weekly schedule. */
@Service
public class SlotAvailabilityService {
    public static final int LESSON_MINUTES = 60;

    private final AvailabilityRepository availability;
    private final InstructorLeaveRepository leaves;
    private final BookingRepository bookings;
    private final Clock clock;

    public SlotAvailabilityService(AvailabilityRepository availability,
                                   InstructorLeaveRepository leaves,
                                   BookingRepository bookings, Clock clock) {
        this.availability = availability;
        this.leaves = leaves;
        this.bookings = bookings;
        this.clock = clock;
    }

    public List<LocalDateTime> availableSlots(String instructorId, LocalDate date) {
        return availableSlots(instructorId, date, null, null);
    }

    public List<LocalDateTime> availableSlots(String instructorId, LocalDate date,
                                               String excludingBookingId) {
        return availableSlots(instructorId, date, excludingBookingId, null);
    }

    public List<LocalDateTime> availableSlots(String instructorId, LocalDate date,
                                               String excludingBookingId, String studentId) {
        if (instructorId == null || date == null) return List.of();
        if (leaves.readAllStrict().stream().anyMatch(leave ->
                leave.getInstructorId().equals(instructorId) && leave.getDate().equals(date))) {
            return List.of();
        }
        LocalDateTime now = LocalDateTime.now(clock);
        List<LocalDateTime> result = new ArrayList<>();
        for (var block : availability.readAllStrict()) {
            if (!block.getInstructorId().equals(instructorId)
                    || block.getDayOfWeek() != date.getDayOfWeek()) continue;
            for (LocalTime start = block.getStartTime();
                 !start.plusMinutes(LESSON_MINUTES).isAfter(block.getEndTime());
                 start = start.plusMinutes(LESSON_MINUTES)) {
                LocalDateTime slotStart = LocalDateTime.of(date, start);
                LocalDateTime slotEnd = slotStart.plusMinutes(LESSON_MINUTES);
                if (!slotStart.isAfter(now)) continue;
                boolean busy = bookings.readAllStrict().stream().anyMatch(booking ->
                        !booking.getId().equals(excludingBookingId)
                                && (active(booking.getStatus())
                                && (instructorId.equals(booking.getInstructorId())
                                || instructorId.equals(booking.getRequestedInstructorId()))
                                && LessonIntervalUtils.overlaps(slotStart, slotEnd,
                                booking.getStartTime(), booking.getEndTime())
                                || studentId != null && studentId.equals(booking.getStudentId())
                                && active(booking.getStatus())
                                && LessonIntervalUtils.overlaps(slotStart, slotEnd,
                                booking.getStartTime(), booking.getEndTime())));
                if (!busy && !result.contains(slotStart)) result.add(slotStart);
            }
        }
        result.sort(LocalDateTime::compareTo);
        return List.copyOf(result);
    }

    private boolean active(BookingStatus status) {
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }
}
