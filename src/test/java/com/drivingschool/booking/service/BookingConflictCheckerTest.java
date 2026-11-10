package com.drivingschool.booking.service;

import com.drivingschool.booking.model.Booking;
import com.drivingschool.common.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookingConflictCheckerTest {

    private final LocalDateTime start =
            LocalDateTime.of(2026, 11, 1, 10, 0);

    private final LocalDateTime end =
            LocalDateTime.of(2026, 11, 1, 11, 0);

    private final LocalDateTime created =
            LocalDateTime.of(2026, 10, 20, 9, 0);

    private Booking createBooking(
            String id,
            String studentId,
            String instructorId,
            String vehicleId,
            Booking.BookingStatus status,
            LocalDateTime bookingStart,
            LocalDateTime bookingEnd) {

        return new Booking(
                id,
                studentId,
                "E001",
                bookingStart,
                bookingEnd,
                instructorId,
                vehicleId,
                status,
                created
        );
    }

    private Booking createPendingBooking() {
        return createBooking(
                "B001", "S001", null, null,
                Booking.BookingStatus.PENDING, start, end
        );
    }

    private Booking createConfirmedBooking() {
        return createBooking(
                "B001", "S001", "I001", "V001",
                Booking.BookingStatus.CONFIRMED, start, end
        );
    }

    @Test
    void pendingBookingShouldBlockStudent() {
        assertTrue(BookingConflictChecker.hasStudentConflict(
                List.of(createPendingBooking()),
                "S001", start, end, null
        ));
    }

    @Test
    void confirmedBookingShouldBlockStudent() {
        assertTrue(BookingConflictChecker.hasStudentConflict(
                List.of(createConfirmedBooking()),
                "S001", start, end, null
        ));
    }

    @Test
    void completedBookingShouldBlockStudent() {
        Booking booking = createConfirmedBooking();
        booking.complete();

        assertTrue(BookingConflictChecker.hasStudentConflict(
                List.of(booking), "S001", start, end, null
        ));
    }

    @Test
    void cancelledBookingShouldNotBlockStudent() {
        Booking booking = createPendingBooking();
        booking.cancel();

        assertFalse(BookingConflictChecker.hasStudentConflict(
                List.of(booking), "S001", start, end, null
        ));
    }

    @Test
    void pendingBookingShouldNotBlockInstructorOrVehicle() {
        assertFalse(BookingConflictChecker.hasInstructorConflict(
                List.of(createPendingBooking()),
                "I001", start, end, null
        ));

        assertFalse(BookingConflictChecker.hasVehicleConflict(
                List.of(createPendingBooking()),
                "V001", start, end, null
        ));
    }

    @Test
    void confirmedBookingShouldBlockInstructorAndVehicle() {
        Booking booking = createConfirmedBooking();

        assertTrue(BookingConflictChecker.hasInstructorConflict(
                List.of(booking), "I001", start, end, null
        ));

        assertTrue(BookingConflictChecker.hasVehicleConflict(
                List.of(booking), "V001", start, end, null
        ));
    }

    @Test
    void completedBookingShouldBlockInstructorAndVehicle() {
        Booking booking = createConfirmedBooking();
        booking.complete();

        assertTrue(BookingConflictChecker.hasInstructorConflict(
                List.of(booking), "I001", start, end, null
        ));

        assertTrue(BookingConflictChecker.hasVehicleConflict(
                List.of(booking), "V001", start, end, null
        ));
    }

    @Test
    void cancelledConfirmedBookingShouldNotBlockResources() {
        Booking booking = createConfirmedBooking();
        booking.cancel();

        assertFalse(BookingConflictChecker.hasInstructorConflict(
                List.of(booking), "I001", start, end, null
        ));

        assertFalse(BookingConflictChecker.hasVehicleConflict(
                List.of(booking), "V001", start, end, null
        ));
    }

    @Test
    void differentStudentShouldNotConflict() {
        assertFalse(BookingConflictChecker.hasStudentConflict(
                List.of(createPendingBooking()),
                "S002", start, end, null
        ));
    }

    @Test
    void differentInstructorShouldNotConflict() {
        assertFalse(BookingConflictChecker.hasInstructorConflict(
                List.of(createConfirmedBooking()),
                "I002", start, end, null
        ));
    }

    @Test
    void differentVehicleShouldNotConflict() {
        assertFalse(BookingConflictChecker.hasVehicleConflict(
                List.of(createConfirmedBooking()),
                "V002", start, end, null
        ));
    }

    @Test
    void backToBackLessonsShouldNotConflict() {
        LocalDateTime nextStart = end;
        LocalDateTime nextEnd = end.plusHours(1);

        assertFalse(BookingConflictChecker.hasStudentConflict(
                List.of(createPendingBooking()),
                "S001", nextStart, nextEnd, null
        ));
    }

    @Test
    void overlappingLessonsShouldConflict() {
        LocalDateTime overlappingStart = start.plusMinutes(30);
        LocalDateTime overlappingEnd = end.plusMinutes(30);

        assertTrue(BookingConflictChecker.hasStudentConflict(
                List.of(createPendingBooking()),
                "S001", overlappingStart, overlappingEnd, null
        ));
    }

    @Test
    void excludedBookingShouldNotConflictWithItself() {
        assertFalse(BookingConflictChecker.hasStudentConflict(
                List.of(createPendingBooking()),
                "S001", start, end, "B001"
        ));
    }

    @Test
    void nullBookingListShouldBeRejected() {
        assertThrows(ValidationException.class, () ->
                BookingConflictChecker.hasStudentConflict(
                        null, "S001", start, end, null
                )
        );
    }

    @Test
    void blankResourceIdShouldBeRejected() {
        assertThrows(ValidationException.class, () ->
                BookingConflictChecker.hasStudentConflict(
                        List.of(), " ", start, end, null
                )
        );
    }

    @Test
    void invalidTimeIntervalShouldBeRejected() {
        assertThrows(ValidationException.class, () ->
                BookingConflictChecker.hasStudentConflict(
                        List.of(), "S001", end, start, null
                )
        );
    }
}
