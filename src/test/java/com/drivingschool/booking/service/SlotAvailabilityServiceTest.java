// Module owner: IT26101134
package com.drivingschool.booking.service;

import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.instructor.model.Availability;
import com.drivingschool.instructor.model.InstructorLeave;
import com.drivingschool.instructor.repository.AvailabilityRepository;
import com.drivingschool.instructor.repository.InstructorLeaveRepository;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SlotAvailabilityServiceTest {
    private final AvailabilityRepository availability = mock(AvailabilityRepository.class);
    private final InstructorLeaveRepository leaves = mock(InstructorLeaveRepository.class);
    private final BookingRepository bookings = mock(BookingRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2030-01-01T08:00:00Z"), ZoneOffset.UTC);
    private final SlotAvailabilityService service = new SlotAvailabilityService(availability, leaves, bookings, clock);

    @Test void slotsComeFromWorkingHoursAndExcludePastAndActiveBookingTimes() {
        when(availability.readAllStrict()).thenReturn(List.of(new Availability("H001", "I001",
                DayOfWeek.WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(12, 0))));
        when(leaves.readAllStrict()).thenReturn(List.of());
        Booking occupied = new Booking("B001", "S002", "E001",
                LocalDateTime.parse("2030-01-02T09:00"), LocalDateTime.parse("2030-01-02T10:00"),
                null, null, BookingStatus.PENDING, LocalDateTime.parse("2029-12-31T10:00"));
        occupied.setRequestedInstructorId("I001");
        when(bookings.readAllStrict()).thenReturn(List.of(occupied));
        assertEquals(List.of(LocalDateTime.parse("2030-01-02T08:00"),
                        LocalDateTime.parse("2030-01-02T10:00"),
                        LocalDateTime.parse("2030-01-02T11:00")),
                service.availableSlots("I001", LocalDate.parse("2030-01-02")));
    }

    @Test void leaveDayHasNoSlots() {
        when(availability.readAllStrict()).thenReturn(List.of(new Availability("H001", "I001",
                DayOfWeek.WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(12, 0))));
        when(leaves.readAllStrict()).thenReturn(List.of(new InstructorLeave("L001", "I001",
                LocalDate.parse("2030-01-02"), "Leave")));
        assertTrue(service.availableSlots("I001", LocalDate.parse("2030-01-02")).isEmpty());
    }
}
