/**
 * ============================================================
 * MEMBER   : IT26101134 - [Name]
 * MODULE   : Bookings, FIFO queue and scheduling
 * FILE     : SlotBoundaryTest.java
 * WHAT IT DOES (simple words): Checks late working hours stop at the same-day closing time.
 * REAL-LIFE EXAMPLE: Check a spare waiting list before handling real lesson requests.
 * OOP USED : Encapsulation, ordering polymorphism and exception handling
 * CRUD     : Tests the related booking operations using temporary data
 * ============================================================
 */
package com.drivingschool.booking.service;

import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.instructor.model.Availability;
import com.drivingschool.instructor.repository.AvailabilityRepository;
import com.drivingschool.instructor.repository.InstructorLeaveRepository;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SlotBoundaryTest {
    // A 22:00-23:30 block offers 22:00 only; it must never wrap to tomorrow and repeat forever.
    @Test
    void lateEveningHoursStopBeforeMidnight() {
        SlotAvailabilityService service = service(LocalTime.of(22, 0), LocalTime.of(23, 30));
        List<LocalDateTime> slots = assertTimeoutPreemptively(Duration.ofSeconds(2),
                () -> service.availableSlots("I001", LocalDate.of(2030, 1, 2)));
        assertEquals(List.of(LocalDateTime.of(2030, 1, 2, 22, 0)), slots);
    }

    // A short late block cannot fit a complete one-hour driving lesson.
    @Test
    void shortLateBlockOffersNoPartialLesson() {
        SlotAvailabilityService service = service(LocalTime.of(23, 0), LocalTime.of(23, 45));
        assertTrue(assertTimeoutPreemptively(Duration.ofSeconds(2),
                () -> service.availableSlots("I001", LocalDate.of(2030, 1, 2))).isEmpty());
    }

    // Supplies one saved working block and no bookings or leave days.
    private SlotAvailabilityService service(LocalTime start, LocalTime end) {
        AvailabilityRepository hours = mock(AvailabilityRepository.class);
        InstructorLeaveRepository leave = mock(InstructorLeaveRepository.class);
        BookingRepository bookings = mock(BookingRepository.class);
        when(hours.readAllStrict()).thenReturn(List.of(new Availability("H001", "I001",
                DayOfWeek.WEDNESDAY, start, end)));
        when(leave.readAllStrict()).thenReturn(List.of());
        when(bookings.readAllStrict()).thenReturn(List.of());
        return new SlotAvailabilityService(hours, leave, bookings,
                Clock.fixed(Instant.parse("2030-01-01T08:00:00Z"), ZoneOffset.UTC));
    }
}
