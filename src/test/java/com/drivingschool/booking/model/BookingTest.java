
package com.drivingschool.booking.model;

import com.drivingschool.common.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

    private final LocalDateTime start =
            LocalDateTime.of(2026, 11, 1, 10, 0);

    private final LocalDateTime end =
            LocalDateTime.of(2026, 11, 1, 11, 0);

    private final LocalDateTime created =
            LocalDateTime.of(2026, 10, 20, 9, 0);

    private Booking createPendingBooking() {
        return new Booking(
                "B001",
                "S001",
                "E001",
                start,
                end,
                null,
                null,
                BookingStatus.PENDING,
                created
        );
    }

    @Test
    void shouldCreateValidPendingBooking() {
        Booking booking = createPendingBooking();

        assertEquals("B001", booking.getId());
        assertEquals("S001", booking.getStudentId());
        assertEquals("E001", booking.getEnrollmentId());
        assertEquals(start, booking.getStartTime());
        assertEquals(end, booking.getEndTime());
        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertNull(booking.getInstructorId());
        assertNull(booking.getVehicleId());
        assertEquals(created, booking.getCreatedAt());
    }

    @Test
    void shouldRejectInvalidBookingId() {
        assertThrows(ValidationException.class, () ->
                new Booking(
                        "B000", "S001", "E001",
                        start, end, null, null,
                        BookingStatus.PENDING, created
                )
        );
    }

    @Test
    void shouldRejectInvalidStudentId() {
        assertThrows(ValidationException.class, () ->
                new Booking(
                        "B001", "S000", "E001",
                        start, end, null, null,
                        BookingStatus.PENDING, created
                )
        );
    }

    @Test
    void shouldRejectBlankEnrollmentId() {
        assertThrows(ValidationException.class, () ->
                new Booking(
                        "B001", "S001", " ",
                        start, end, null, null,
                        BookingStatus.PENDING, created
                )
        );
    }

    @Test
    void shouldRejectEndTimeBeforeStartTime() {
        assertThrows(ValidationException.class, () ->
                new Booking(
                        "B001", "S001", "E001",
                        end, start, null, null,
                        BookingStatus.PENDING, created
                )
        );
    }

    @Test
    void shouldRejectEqualStartAndEndTimes() {
        assertThrows(ValidationException.class, () ->
                new Booking(
                        "B001", "S001", "E001",
                        start, start, null, null,
                        BookingStatus.PENDING, created
                )
        );
    }

    @Test
    void shouldRejectPendingBookingWithAssignments() {
        assertThrows(ValidationException.class, () ->
                new Booking(
                        "B001", "S001", "E001",
                        start, end, "I001", "V001",
                        BookingStatus.PENDING, created
                )
        );
    }

    @Test
    void shouldRejectConfirmedBookingWithoutAssignments() {
        assertThrows(ValidationException.class, () ->
                new Booking(
                        "B001", "S001", "E001",
                        start, end, null, null,
                        BookingStatus.CONFIRMED, created
                )
        );
    }

    @Test
    void shouldRejectPartialAssignment() {
        assertThrows(ValidationException.class, () ->
                new Booking(
                        "B001", "S001", "E001",
                        start, end, "I001", null,
                        BookingStatus.CONFIRMED, created
                )
        );
    }

    @Test
    void shouldConfirmPendingBooking() {
        Booking booking = createPendingBooking();

        booking.confirm("I001", "V001");

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals("I001", booking.getInstructorId());
        assertEquals("V001", booking.getVehicleId());
    }

    @Test
    void shouldCompleteConfirmedBooking() {
        Booking booking = createPendingBooking();
        booking.confirm("I001", "V001");

        booking.complete();

        assertEquals(BookingStatus.COMPLETED, booking.getStatus());
    }

    @Test
    void shouldCancelPendingBooking() {
        Booking booking = createPendingBooking();

        booking.cancel();

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertNull(booking.getInstructorId());
        assertNull(booking.getVehicleId());
    }

    @Test
    void shouldCancelConfirmedBookingAndKeepAssignments() {
        Booking booking = createPendingBooking();
        booking.confirm("I001", "V001");

        booking.cancel();

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals("I001", booking.getInstructorId());
        assertEquals("V001", booking.getVehicleId());
    }

    @Test
    void shouldRejectConfirmingBookingTwice() {
        Booking booking = createPendingBooking();
        booking.confirm("I001", "V001");

        assertThrows(ValidationException.class,
                () -> booking.confirm("I002", "V002"));

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals("I001", booking.getInstructorId());
        assertEquals("V001", booking.getVehicleId());
    }

    @Test
    void shouldRejectCompletingPendingBooking() {
        Booking booking = createPendingBooking();

        assertThrows(ValidationException.class, booking::complete);

        assertEquals(BookingStatus.PENDING, booking.getStatus());
    }

    @Test
    void shouldRejectCancellingCompletedBooking() {
        Booking booking = createPendingBooking();
        booking.confirm("I001", "V001");
        booking.complete();

        assertThrows(ValidationException.class, booking::cancel);

        assertEquals(BookingStatus.COMPLETED, booking.getStatus());
    }

    @Test
    void shouldPreserveBookingDataAfterJsonRoundTrip() throws Exception {
        Booking original = createPendingBooking();
        original.confirm("I001", "V001");

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        String json = mapper.writeValueAsString(original);
        Booking restored = mapper.readValue(json, Booking.class);

        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getStudentId(), restored.getStudentId());
        assertEquals(original.getEnrollmentId(), restored.getEnrollmentId());
        assertEquals(original.getStartTime(), restored.getStartTime());
        assertEquals(original.getEndTime(), restored.getEndTime());
        assertEquals(original.getInstructorId(), restored.getInstructorId());
        assertEquals(original.getVehicleId(), restored.getVehicleId());
        assertEquals(original.getStatus(), restored.getStatus());
        assertEquals(original.getCreatedAt(), restored.getCreatedAt());
    }

    @Test
    void shouldPreserveCompletedBookingAfterJsonRoundTrip()
            throws Exception {
        Booking original = createPendingBooking();
        original.confirm("I001", "V001");
        original.complete();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        String json = mapper.writeValueAsString(original);
        Booking restored = mapper.readValue(json, Booking.class);

        assertEquals(BookingStatus.COMPLETED, restored.getStatus());
        assertEquals("I001", restored.getInstructorId());
        assertEquals("V001", restored.getVehicleId());
        assertEquals(original.getCreatedAt(), restored.getCreatedAt());
    }

    @Test
    void shouldPreserveCancelledBookingAfterJsonRoundTrip()
            throws Exception {
        Booking original = createPendingBooking();
        original.confirm("I001", "V001");
        original.cancel();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        String json = mapper.writeValueAsString(original);
        Booking restored = mapper.readValue(json, Booking.class);

        assertEquals(BookingStatus.CANCELLED, restored.getStatus());
        assertEquals("I001", restored.getInstructorId());
        assertEquals("V001", restored.getVehicleId());
        assertEquals(original.getCreatedAt(), restored.getCreatedAt());
    }

    @Test
    void shouldRejectInvalidInstructorIdWhenConfirming() {
        Booking booking = createPendingBooking();

        assertThrows(ValidationException.class,
                () -> booking.confirm("I000", "V001"));

        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertNull(booking.getInstructorId());
        assertNull(booking.getVehicleId());
    }

    @Test
    void shouldRejectInvalidVehicleIdWhenConfirming() {
        Booking booking = createPendingBooking();

        assertThrows(ValidationException.class,
                () -> booking.confirm("I001", "V000"));

        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertNull(booking.getInstructorId());
        assertNull(booking.getVehicleId());
    }

    @Test
    void shouldRejectInvalidInstructorAndVehicleFormats() {
        assertThrows(ValidationException.class, () ->
                new Booking(
                        "B001", "S001", "E001",
                        start, end, "INVALID", "V001",
                        BookingStatus.CONFIRMED, created
                ));

        assertThrows(ValidationException.class, () ->
                new Booking(
                        "B001", "S001", "E001",
                        start, end, "I001", "INVALID",
                        BookingStatus.CONFIRMED, created
                ));
    }

}
