package com.drivingschool.booking.model;

import com.drivingschool.common.exception.ValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    private ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

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

    private Booking createConfirmedBooking() {
        Booking booking = createPendingBooking();
        booking.confirm("I001", "V001");
        return booking;
    }

    private JsonNode toJson(Booking booking) throws Exception {
        return createMapper().readTree(
                createMapper().writeValueAsString(booking)
        );
    }

    private void assertRequiredFieldRejected(String field, boolean explicitNull)
            throws Exception {
        ObjectMapper mapper = createMapper();
        ObjectNode json = (ObjectNode) mapper.readTree(
                mapper.writeValueAsString(createPendingBooking())
        );

        if (explicitNull) {
            json.putNull(field);
        } else {
            json.remove(field);
        }

        ValueInstantiationException exception = assertThrows(
                ValueInstantiationException.class,
                () -> mapper.treeToValue(json, Booking.class),
                "Expected field " + field
                        + " to be rejected; explicitNull=" + explicitNull
        );

        assertInstanceOf(ValidationException.class, exception.getCause());
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
                new Booking("B000", "S001", "E001",
                        start, end, null, null,
                        BookingStatus.PENDING, created));
    }

    @Test
    void shouldRejectInvalidStudentId() {
        assertThrows(ValidationException.class, () ->
                new Booking("B001", "S000", "E001",
                        start, end, null, null,
                        BookingStatus.PENDING, created));
    }

    @Test
    void shouldRejectBlankEnrollmentId() {
        assertThrows(ValidationException.class, () ->
                new Booking("B001", "S001", " ",
                        start, end, null, null,
                        BookingStatus.PENDING, created));
    }

    @Test
    void shouldRejectEndTimeBeforeStartTime() {
        assertThrows(ValidationException.class, () ->
                new Booking("B001", "S001", "E001",
                        end, start, null, null,
                        BookingStatus.PENDING, created));
    }

    @Test
    void shouldRejectEqualStartAndEndTimes() {
        assertThrows(ValidationException.class, () ->
                new Booking("B001", "S001", "E001",
                        start, start, null, null,
                        BookingStatus.PENDING, created));
    }

    @Test
    void shouldRejectPendingBookingWithAssignments() {
        assertThrows(ValidationException.class, () ->
                new Booking("B001", "S001", "E001",
                        start, end, "I001", "V001",
                        BookingStatus.PENDING, created));
    }

    @Test
    void shouldRejectConfirmedBookingWithoutAssignments() {
        assertThrows(ValidationException.class, () ->
                new Booking("B001", "S001", "E001",
                        start, end, null, null,
                        BookingStatus.CONFIRMED, created));
    }

    @Test
    void shouldRejectPartialAssignmentWithInstructorOnly() {
        assertThrows(ValidationException.class, () ->
                new Booking("B001", "S001", "E001",
                        start, end, "I001", null,
                        BookingStatus.CONFIRMED, created));
    }

    @Test
    void shouldRejectPartialAssignmentWithVehicleOnly() {
        assertThrows(ValidationException.class, () ->
                new Booking("B001", "S001", "E001",
                        start, end, null, "V001",
                        BookingStatus.CONFIRMED, created));
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
        Booking booking = createConfirmedBooking();

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
        Booking booking = createConfirmedBooking();

        booking.cancel();

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals("I001", booking.getInstructorId());
        assertEquals("V001", booking.getVehicleId());
    }

    @Test
    void shouldRejectConfirmingBookingTwiceWithoutChangingState() {
        Booking booking = createConfirmedBooking();

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
        Booking booking = createConfirmedBooking();
        booking.complete();

        assertThrows(ValidationException.class, booking::cancel);

        assertEquals(BookingStatus.COMPLETED, booking.getStatus());
    }

    @Test
    void shouldRejectAllLifecycleOperationsOnCancelledBooking()
            throws Exception {
        Booking booking = createConfirmedBooking();
        booking.cancel();

        assertThrows(ValidationException.class,
                () -> booking.confirm("I002", "V002"));
        assertThrows(ValidationException.class, booking::complete);
        assertThrows(ValidationException.class, booking::cancel);

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals("I001", booking.getInstructorId());
        assertEquals("V001", booking.getVehicleId());
    }

    @Test
    void shouldPreservePendingBookingAfterJsonRoundTrip()
            throws Exception {
        Booking original = createPendingBooking();
        ObjectMapper mapper = createMapper();

        String json = mapper.writeValueAsString(original);
        Booking restored = mapper.readValue(json, Booking.class);

        assertEquals(BookingStatus.PENDING, restored.getStatus());
        assertNull(restored.getInstructorId());
        assertNull(restored.getVehicleId());
        assertBookingDataEquals(original, restored);
    }

    @Test
    void shouldPreserveConfirmedBookingAfterJsonRoundTrip()
            throws Exception {
        Booking original = createConfirmedBooking();
        ObjectMapper mapper = createMapper();

        String json = mapper.writeValueAsString(original);
        Booking restored = mapper.readValue(json, Booking.class);

        assertEquals(BookingStatus.CONFIRMED, restored.getStatus());
        assertBookingDataEquals(original, restored);
    }

    @Test
    void shouldPreserveCompletedBookingAfterJsonRoundTrip()
            throws Exception {
        Booking original = createConfirmedBooking();
        original.complete();
        ObjectMapper mapper = createMapper();

        String json = mapper.writeValueAsString(original);
        Booking restored = mapper.readValue(json, Booking.class);

        assertEquals(BookingStatus.COMPLETED, restored.getStatus());
        assertBookingDataEquals(original, restored);
    }

    @Test
    void shouldPreserveCancelledConfirmedBookingAfterJsonRoundTrip()
            throws Exception {
        Booking original = createConfirmedBooking();
        original.cancel();
        ObjectMapper mapper = createMapper();

        String json = mapper.writeValueAsString(original);
        Booking restored = mapper.readValue(json, Booking.class);

        assertEquals(BookingStatus.CANCELLED, restored.getStatus());
        assertEquals("I001", restored.getInstructorId());
        assertEquals("V001", restored.getVehicleId());
        assertBookingDataEquals(original, restored);
    }

    @Test
    void shouldPreserveCancelledPendingBookingAfterJsonRoundTrip()
            throws Exception {
        Booking original = createPendingBooking();
        original.cancel();
        ObjectMapper mapper = createMapper();

        String json = mapper.writeValueAsString(original);
        Booking restored = mapper.readValue(json, Booking.class);

        assertEquals(BookingStatus.CANCELLED, restored.getStatus());
        assertNull(restored.getInstructorId());
        assertNull(restored.getVehicleId());
        assertBookingDataEquals(original, restored);
    }

    @Test
    void shouldSerializeTimestampsAsIsoTextAndRestoreTheirValues()
            throws Exception {
        Booking original = createConfirmedBooking();
        ObjectMapper mapper = createMapper();

        String json = mapper.writeValueAsString(original);
        JsonNode jsonNode = mapper.readTree(json);

        assertTrue(jsonNode.get("startTime").isTextual());
        assertTrue(jsonNode.get("endTime").isTextual());
        assertTrue(jsonNode.get("createdAt").isTextual());

        assertEquals(start, LocalDateTime.parse(
                jsonNode.get("startTime").asText()));
        assertEquals(end, LocalDateTime.parse(
                jsonNode.get("endTime").asText()));
        assertEquals(created, LocalDateTime.parse(
                jsonNode.get("createdAt").asText()));

        Booking restored = mapper.readValue(json, Booking.class);
        assertBookingDataEquals(original, restored);
    }

    @Test
    void shouldRejectCompletedBookingWithoutAssignmentsDuringReconstruction() {
        assertThrows(ValidationException.class, () ->
                new Booking("B001", "S001", "E001",
                        start, end, null, null,
                        BookingStatus.COMPLETED, created));
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
                new Booking("B001", "S001", "E001",
                        start, end, "INVALID", "V001",
                        BookingStatus.CONFIRMED, created));

        assertThrows(ValidationException.class, () ->
                new Booking("B001", "S001", "E001",
                        start, end, "I001", "INVALID",
                        BookingStatus.CONFIRMED, created));
    }

    @Test
    void shouldRejectMissingRequiredJsonFields() throws Exception {
        String[] fields = {
                "id", "studentId", "enrollmentId",
                "startTime", "endTime", "status", "createdAt"
        };

        for (String field : fields) {
            assertRequiredFieldRejected(field, false);
        }
    }

    @Test
    void shouldRejectExplicitNullRequiredJsonFields() throws Exception {
        String[] fields = {
                "id", "studentId", "enrollmentId",
                "startTime", "endTime", "status", "createdAt"
        };

        for (String field : fields) {
            assertRequiredFieldRejected(field, true);
        }
    }

    private void assertBookingDataEquals(Booking expected, Booking actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getStudentId(), actual.getStudentId());
        assertEquals(expected.getEnrollmentId(), actual.getEnrollmentId());
        assertEquals(expected.getStartTime(), actual.getStartTime());
        assertEquals(expected.getEndTime(), actual.getEndTime());
        assertEquals(expected.getInstructorId(), actual.getInstructorId());
        assertEquals(expected.getVehicleId(), actual.getVehicleId());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getCreatedAt(), actual.getCreatedAt());
    }
}