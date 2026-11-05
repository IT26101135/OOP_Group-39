package com.drivingschool.instructor.model;

import com.drivingschool.common.exception.ValidationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class InstructorAvailabilityTest {

    private final LocalDateTime start =
            LocalDateTime.of(2026, 11, 9, 9, 0);

    private final LocalDateTime end = start.plusHours(2);

    private ObjectMapper mapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void shouldCreateValidAvailability() {
        InstructorAvailability availability =
                new InstructorAvailability("T001", "I001", start, end);

        assertEquals("T001", availability.getId());
        assertEquals("I001", availability.getInstructorId());
        assertEquals(start, availability.getStartTime());
        assertEquals(end, availability.getEndTime());
    }

    @Test
    void shouldAcceptMaximumIds() {
        InstructorAvailability availability =
                new InstructorAvailability("T999", "I999", start, end);

        assertEquals("T999", availability.getId());
        assertEquals("I999", availability.getInstructorId());
    }

    @Test
    void shouldRejectInvalidAvailabilityIds() {
        for (String id : new String[]{
                null, "", "T000", "T00", "T1000",
                "t001", "I001", " T001", "T001 "
        }) {
            assertThrows(ValidationException.class,
                    () -> new InstructorAvailability(
                            id, "I001", start, end));
        }
    }

    @Test
    void shouldRejectInvalidInstructorIds() {
        for (String id : new String[]{
                null, "", "I000", "I00", "I1000",
                "i001", "T001", " I001", "I001 "
        }) {
            assertThrows(ValidationException.class,
                    () -> new InstructorAvailability(
                            "T001", id, start, end));
        }
    }

    @Test
    void shouldRejectNullTimes() {
        assertThrows(ValidationException.class,
                () -> new InstructorAvailability(
                        "T001", "I001", null, end));

        assertThrows(ValidationException.class,
                () -> new InstructorAvailability(
                        "T001", "I001", start, null));
    }

    @Test
    void shouldRejectEqualAndReversedTimes() {
        assertThrows(ValidationException.class,
                () -> new InstructorAvailability(
                        "T001", "I001", start, start));

        assertThrows(ValidationException.class,
                () -> new InstructorAvailability(
                        "T001", "I001", end, start));
    }

    @Test
    void shouldAllowAdjacentIntervals() {
        InstructorAvailability first =
                new InstructorAvailability(
                        "T001", "I001", start, end);

        InstructorAvailability second =
                new InstructorAvailability(
                        "T002", "I001", end, end.plusHours(2));

        assertEquals(first.getEndTime(), second.getStartTime());
    }

    @Test
    void shouldPreserveFieldsDuringJsonRoundTrip() throws Exception {
        InstructorAvailability original =
                new InstructorAvailability("T001", "I001", start, end);

        String json = mapper().writeValueAsString(original);

        InstructorAvailability restored =
                mapper().readValue(json, InstructorAvailability.class);

        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getInstructorId(), restored.getInstructorId());
        assertEquals(original.getStartTime(), restored.getStartTime());
        assertEquals(original.getEndTime(), restored.getEndTime());
    }

    @Test
    void shouldRejectInvalidPersistedId() {
        String json = """
                {
                  "id": "T000",
                  "instructorId": "I001",
                  "startTime": "2026-11-09T09:00:00",
                  "endTime": "2026-11-09T11:00:00"
                }
                """;

        assertThrows(JsonMappingException.class,
                () -> mapper().readValue(
                        json, InstructorAvailability.class));
    }

    @Test
    void shouldRejectInvalidPersistedInterval() {
        String json = """
                {
                  "id": "T001",
                  "instructorId": "I001",
                  "startTime": "2026-11-09T11:00:00",
                  "endTime": "2026-11-09T09:00:00"
                }
                """;

        assertThrows(JsonMappingException.class,
                () -> mapper().readValue(
                        json, InstructorAvailability.class));
    }

    @Test
    void shouldRejectMissingRequiredJsonFields() {
        String[] invalidJson = {
                """
                {
                  "instructorId": "I001",
                  "startTime": "2026-11-09T09:00:00",
                  "endTime": "2026-11-09T11:00:00"
                }
                """,
                """
                {
                  "id": "T001",
                  "startTime": "2026-11-09T09:00:00",
                  "endTime": "2026-11-09T11:00:00"
                }
                """,
                """
                {
                  "id": "T001",
                  "instructorId": "I001",
                  "endTime": "2026-11-09T11:00:00"
                }
                """,
                """
                {
                  "id": "T001",
                  "instructorId": "I001",
                  "startTime": "2026-11-09T09:00:00"
                }
                """
        };

        for (String json : invalidJson) {
            assertThrows(JsonMappingException.class,
                    () -> mapper().readValue(
                            json, InstructorAvailability.class));
        }
    }

    @Test
    void shouldRejectNullJsonFields() {
        String json = """
                {
                  "id": "T001",
                  "instructorId": "I001",
                  "startTime": null,
                  "endTime": "2026-11-09T11:00:00"
                }
                """;

        assertThrows(JsonMappingException.class,
                () -> mapper().readValue(
                        json, InstructorAvailability.class));
    }
}