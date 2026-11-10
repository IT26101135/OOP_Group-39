// MEMBER: IT26101134 - Attendance display and existing time-setting boundaries. CRUD: no writes.
package com.drivingschool.booking;

import com.drivingschool.booking.controller.AttendancePresentationAdvice;
import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import java.time.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AttendancePresentationAdviceTest {
    private final Clock clock = Clock.fixed(Instant.parse("2030-01-10T08:00:00Z"), ZoneOffset.UTC);

    @Test
    void attendanceButtonsWaitUntilExactEnd() {
        AttendancePresentationAdvice display = new AttendancePresentationAdvice(clock, true);
        assertFalse(display.isAvailable(lesson(LocalDateTime.now(clock).plusSeconds(1), BookingStatus.CONFIRMED)));
        assertTrue(display.isAvailable(lesson(LocalDateTime.now(clock), BookingStatus.CONFIRMED)));
    }

    @Test
    void explicitPreviewOverrideEnablesFutureConfirmedLesson() {
        Booking booking = lesson(LocalDateTime.now(clock).plusDays(1), BookingStatus.CONFIRMED);
        assertFalse(new AttendancePresentationAdvice(clock, true).isAvailable(booking));
        assertTrue(new AttendancePresentationAdvice(clock, false).isAvailable(booking));
    }

    @Test
    void overrideDoesNotOfferAttendanceForClosedOrPendingLessons() {
        AttendancePresentationAdvice display = new AttendancePresentationAdvice(clock, false);
        for (BookingStatus status : new BookingStatus[]{BookingStatus.PENDING, BookingStatus.COMPLETED,
                BookingStatus.CANCELLED, BookingStatus.REJECTED, BookingStatus.NO_SHOW}) {
            assertFalse(display.isAvailable(lesson(LocalDateTime.now(clock), status)));
        }
    }

    private Booking lesson(LocalDateTime end, BookingStatus status) {
        return new Booking("B001", "S001", "E001", end.minusHours(1), end,
                "I001", "V001", status, LocalDateTime.now(clock).minusDays(2));
    }
}
