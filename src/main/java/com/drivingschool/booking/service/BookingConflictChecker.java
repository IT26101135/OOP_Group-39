package com.drivingschool.booking.service;

import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.booking.util.LessonIntervalUtils;
import com.drivingschool.common.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.List;

public final class BookingConflictChecker {

    private BookingConflictChecker() {
    }

    public static boolean hasStudentConflict(
            List<Booking> bookings,
            String studentId,
            LocalDateTime start,
            LocalDateTime end,
            String excludedBookingId) {

        validateInputs(bookings, studentId, start, end);

        return bookings.stream().anyMatch(booking ->
                !isExcluded(booking, excludedBookingId)
                        && studentId.equals(booking.getStudentId())
                        && blocksStudent(booking.getStatus())
                        && LessonIntervalUtils.overlaps(
                        start, end,
                        booking.getStartTime(),
                        booking.getEndTime()
                )
        );
    }

    public static boolean hasInstructorConflict(
            List<Booking> bookings,
            String instructorId,
            LocalDateTime start,
            LocalDateTime end,
            String excludedBookingId) {

        validateInputs(bookings, instructorId, start, end);

        return bookings.stream().anyMatch(booking ->
                !isExcluded(booking, excludedBookingId)
                        && instructorId.equals(booking.getInstructorId())
                        && blocksResource(booking.getStatus())
                        && LessonIntervalUtils.overlaps(
                        start, end,
                        booking.getStartTime(),
                        booking.getEndTime()
                )
        );
    }

    public static boolean hasVehicleConflict(
            List<Booking> bookings,
            String vehicleId,
            LocalDateTime start,
            LocalDateTime end,
            String excludedBookingId) {

        validateInputs(bookings, vehicleId, start, end);

        return bookings.stream().anyMatch(booking ->
                !isExcluded(booking, excludedBookingId)
                        && vehicleId.equals(booking.getVehicleId())
                        && blocksResource(booking.getStatus())
                        && LessonIntervalUtils.overlaps(
                        start, end,
                        booking.getStartTime(),
                        booking.getEndTime()
                )
        );
    }

    private static boolean blocksStudent(BookingStatus status) {
        return status == BookingStatus.PENDING
                || status == BookingStatus.CONFIRMED
                || status == BookingStatus.COMPLETED;
    }

    private static boolean blocksResource(BookingStatus status) {
        return status == BookingStatus.CONFIRMED
                || status == BookingStatus.COMPLETED;
    }

    private static boolean isExcluded(
            Booking booking,
            String excludedBookingId) {

        return excludedBookingId != null
                && excludedBookingId.equals(booking.getId());
    }

    private static void validateInputs(
            List<Booking> bookings,
            String resourceId,
            LocalDateTime start,
            LocalDateTime end) {

        if (bookings == null) {
            throw new ValidationException(
                    "Booking list cannot be null."
            );
        }

        if (resourceId == null || resourceId.isBlank()) {
            throw new ValidationException(
                    "Resource ID cannot be blank."
            );
        }

        if (start == null || end == null) {
            throw new ValidationException(
                    "Lesson start and end times cannot be null."
            );
        }

        if (!start.isBefore(end)) {
            throw new ValidationException(
                    "Lesson start time must be before end time."
            );
        }
    }
}
