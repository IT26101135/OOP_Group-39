
package com.drivingschool.instructor.model;

import com.drivingschool.common.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InstructorTest {

    @Test
    void shouldCreateValidInstructor() {
        Instructor instructor = new Instructor(
                "I001",
                "  Kumar Silva  ",
                "  kumar@example.com  ",
                "0771234567",
                5,
                true
        );

        assertEquals("I001", instructor.getId());
        assertEquals("Kumar Silva", instructor.getName());
        assertEquals("kumar@example.com", instructor.getEmail());
        assertEquals("0771234567", instructor.getPhone());
        assertEquals(5, instructor.getYearsOfExperience());
        assertTrue(instructor.isActive());
    }

    @Test
    void shouldRejectInvalidInstructorId() {
        assertThrows(ValidationException.class,
                () -> new Instructor(
                        "I000",
                        "Kumar Silva",
                        "kumar@example.com",
                        "0771234567",
                        5,
                        true
                ));
    }

    @Test
    void shouldRejectNegativeExperience() {
        assertThrows(ValidationException.class,
                () -> new Instructor(
                        "I002",
                        "Nimal Silva",
                        "nimal@example.com",
                        "0771234567",
                        -1,
                        true
                ));
    }

    @Test
    void shouldPreserveNameAfterInvalidUpdate() {
        Instructor instructor = new Instructor(
                "I003",
                "Kumar Silva",
                "kumar@example.com",
                "0771234567",
                5,
                true
        );

        assertThrows(ValidationException.class,
                () -> instructor.setName(" "));

        assertEquals("Kumar Silva", instructor.getName());
    }

    @Test
    void shouldAcceptMaximumId() {
        Instructor instructor = new Instructor(
                "I999", "Kumar Silva", "kumar@example.com",
                "0771234567", 5, true
        );

        assertEquals("I999", instructor.getId());
    }

    @Test
    void shouldRejectInvalidEmail() {
        assertThrows(ValidationException.class,
                () -> new Instructor(
                        "I004", "Kumar Silva", "invalid-email",
                        "0771234567", 5, true
                ));
    }

    @Test
    void shouldRejectInvalidPhone() {
        assertThrows(ValidationException.class,
                () -> new Instructor(
                        "I005", "Kumar Silva", "kumar@example.com",
                        "ABC123", 5, true
                ));
    }

    @Test
    void shouldAcceptExperienceBoundaries() {
        Instructor beginner = new Instructor(
                "I006", "Kumar Silva", "kumar@example.com",
                "0771234567", 0, true
        );

        Instructor experienced = new Instructor(
                "I007", "Nimal Silva", "nimal@example.com",
                "0771234568", 60, false
        );

        assertEquals(0, beginner.getYearsOfExperience());
        assertEquals(60, experienced.getYearsOfExperience());
        assertFalse(experienced.isActive());
    }

    @Test
    void shouldRejectExperienceAboveMaximum() {
        assertThrows(ValidationException.class,
                () -> new Instructor(
                        "I008", "Kumar Silva", "kumar@example.com",
                        "0771234567", 61, true
                ));
    }

    @Test
    void shouldPreserveExperienceAfterInvalidUpdate() {
        Instructor instructor = new Instructor(
                "I009", "Kumar Silva", "kumar@example.com",
                "0771234567", 5, true
        );

        assertThrows(ValidationException.class,
                () -> instructor.setYearsOfExperience(-1));

        assertEquals(5, instructor.getYearsOfExperience());
    }

    @Test
    void shouldPreserveAllFieldsDuringJsonRoundTrip()
            throws Exception {

        com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper();

        Instructor original = new Instructor(
                "I010",
                "Kumar Silva",
                "kumar@example.com",
                "0771234567",
                5,
                false
        );

        String json = mapper.writeValueAsString(original);

        Instructor restored =
                mapper.readValue(json, Instructor.class);

        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getName(), restored.getName());
        assertEquals(original.getEmail(), restored.getEmail());
        assertEquals(original.getPhone(), restored.getPhone());
        assertEquals(original.getYearsOfExperience(),
                restored.getYearsOfExperience());

        assertFalse(restored.isActive());
    }

    @Test
    void shouldRejectInvalidPersistedExperience() {
        com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper();

        String invalidJson = """
            {
              "id": "I011",
              "name": "Kumar Silva",
              "email": "kumar@example.com",
              "phone": "0771234567",
              "yearsOfExperience": -5,
              "active": true
            }
            """;

        assertThrows(
                com.fasterxml.jackson.databind.JsonMappingException.class,
                () -> mapper.readValue(
                        invalidJson, Instructor.class)
        );
    }

    @Test
    void shouldRejectNullAndBlankRequiredFields() {
        assertThrows(ValidationException.class,
                () -> new Instructor(
                        null, "Kumar Silva", "kumar@example.com",
                        "0771234567", 5, true));

        assertThrows(ValidationException.class,
                () -> new Instructor(
                        "I012", "  ", "kumar@example.com",
                        "0771234567", 5, true));

        assertThrows(ValidationException.class,
                () -> new Instructor(
                        "I012", "Kumar Silva", null,
                        "0771234567", 5, true));

        assertThrows(ValidationException.class,
                () -> new Instructor(
                        "I012", "Kumar Silva", "kumar@example.com",
                        null, 5, true));
    }

    @Test
    void shouldRejectMalformedIds() {
        for (String id : new String[]{"I00", "I1000", "A001", "i001", "IABC"}) {
            assertThrows(ValidationException.class,
                    () -> new Instructor(
                            id, "Kumar Silva", "kumar@example.com",
                            "0771234567", 5, true));
        }
    }

    @Test
    void shouldValidateNameLengthAndUnicode() {
        assertThrows(ValidationException.class,
                () -> new Instructor(
                        "I013", "A", "kumar@example.com",
                        "0771234567", 5, true));

        assertThrows(ValidationException.class,
                () -> new Instructor(
                        "I013", "A".repeat(101), "kumar@example.com",
                        "0771234567", 5, true));

        Instructor instructor = new Instructor(
                "I013", "அருண் குமார்", "kumar@example.com",
                "0771234567", 5, true);

        assertEquals("அருண் குமார்", instructor.getName());
    }

    @Test
    void shouldValidatePhoneBoundaries() {
        Instructor instructor = new Instructor(
                "I014", "Kumar Silva", "kumar@example.com",
                "+94771234567", 5, true);

        assertEquals("+94771234567", instructor.getPhone());

        instructor.setPhone("0123456");
        assertEquals("0123456", instructor.getPhone());

        assertThrows(ValidationException.class,
                () -> instructor.setPhone("123456"));

        assertThrows(ValidationException.class,
                () -> instructor.setPhone("1234567890123456"));

        assertThrows(ValidationException.class,
                () -> instructor.setPhone("++94771234567"));
    }

    @Test
    void shouldRejectExcessivelyLongEmail() {
        assertThrows(ValidationException.class,
                () -> new Instructor(
                        "I015", "Kumar Silva",
                        "a".repeat(250) + "@example.com",
                        "0771234567", 5, true));
    }

    @Test
    void shouldPreserveEmailAndPhoneAfterInvalidUpdates() {
        Instructor instructor = new Instructor(
                "I016",
                "Kumar Silva",
                "kumar@example.com",
                "0771234567",
                5,
                true
        );

        assertThrows(ValidationException.class,
                () -> instructor.setEmail("invalid-email"));

        assertThrows(ValidationException.class,
                () -> instructor.setPhone("123"));

        assertEquals("kumar@example.com", instructor.getEmail());
        assertEquals("0771234567", instructor.getPhone());
    }

    @Test
    void shouldAcceptNameAndPhoneUpperBoundaries() {
        Instructor instructor = new Instructor(
                "I017",
                "A".repeat(100),
                "kumar@example.com",
                "123456789012345",
                5,
                true
        );

        assertEquals(100, instructor.getName().length());
        assertEquals("123456789012345", instructor.getPhone());

        instructor.setName("AB");
        assertEquals("AB", instructor.getName());
    }

    @Test
    void shouldRejectMissingActiveFieldInJson() {

        com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper();

        String invalidJson = """
                {
                  "id": "I018",
                  "name": "Kumar Silva",
                  "email": "kumar@example.com",
                  "phone": "0771234567",
                  "yearsOfExperience": 5
                }
                """;

        assertThrows(
                com.fasterxml.jackson.databind.JsonMappingException.class,
                () -> mapper.readValue(invalidJson, Instructor.class)
        );
    }


}
