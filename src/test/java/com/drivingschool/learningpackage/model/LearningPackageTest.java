package com.drivingschool.learningpackage.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;

import com.drivingschool.common.exception.ValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class LearningPackageTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private LearningPackage createValidPackage() {
        return new LearningPackage(
                "P001",
                "Beginner Package",
                "A package for new driving students.",
                new BigDecimal("15000.00"),
                10,
                30,
                true
        );
    }

    @Test
    void createsPackageAndTrimsText() {
        LearningPackage learningPackage = new LearningPackage(
                "P001",
                "  Beginner Package  ",
                "  A package for new driving students.  ",
                new BigDecimal("15000"),
                10,
                30,
                true
        );

        assertEquals("P001", learningPackage.getId());
        assertEquals("Beginner Package", learningPackage.getName());
        assertEquals(
                "A package for new driving students.",
                learningPackage.getDescription()
        );
        assertEquals(
                new BigDecimal("15000.00"),
                learningPackage.getPrice()
        );
        assertEquals(10, learningPackage.getLessonCount());
        assertEquals(30, learningPackage.getValidityDays());
        assertTrue(learningPackage.isActive());
    }

    @Test
    void acceptsFirstAndLastValidIds() {
        LearningPackage first = new LearningPackage(
                "P001", "Basic Package",
                "A basic driving package.",
                new BigDecimal("100.00"), 1, 1, true
        );

        LearningPackage last = new LearningPackage(
                "P999", "Premium Package",
                "A premium driving package.",
                new BigDecimal("1000000.00"), 100, 365, false
        );

        assertEquals("P001", first.getId());
        assertEquals("P999", last.getId());
    }

    @Test
    void rejectsInvalidIds() {
        assertThrows(ValidationException.class,
                () -> new LearningPackage(
                        "P000", "Basic Package",
                        "A basic driving package.",
                        new BigDecimal("100.00"), 1, 1, true
                ));

        assertThrows(ValidationException.class,
                () -> new LearningPackage(
                        "P1000", "Basic Package",
                        "A basic driving package.",
                        new BigDecimal("100.00"), 1, 1, true
                ));

        assertThrows(ValidationException.class,
                () -> new LearningPackage(
                        null, "Basic Package",
                        "A basic driving package.",
                        new BigDecimal("100.00"), 1, 1, true
                ));
    }

    @Test
    void validatesName() {
        assertThrows(ValidationException.class,
                () -> createPackageWithName(null));

        assertThrows(ValidationException.class,
                () -> createPackageWithName(" "));

        assertThrows(ValidationException.class,
                () -> createPackageWithName("A"));

        assertDoesNotThrow(
                () -> createPackageWithName("AB"));

        assertDoesNotThrow(
                () -> createPackageWithName("N".repeat(100)));

        assertThrows(ValidationException.class,
                () -> createPackageWithName("N".repeat(101)));
    }

    @Test
    void validatesDescription() {
        assertThrows(ValidationException.class,
                () -> createPackageWithDescription(null));

        assertThrows(ValidationException.class,
                () -> createPackageWithDescription("Short"));

        assertDoesNotThrow(
                () -> createPackageWithDescription("1234567890"));

        assertDoesNotThrow(
                () -> createPackageWithDescription("D".repeat(1000)));

        assertThrows(ValidationException.class,
                () -> createPackageWithDescription("D".repeat(1001)));
    }

    @Test
    void normalizesPriceToTwoDecimalPlaces() {
        LearningPackage learningPackage = createValidPackage();

        learningPackage.setPrice(new BigDecimal("15000"));
        assertEquals(new BigDecimal("15000.00"),
                learningPackage.getPrice());

        learningPackage.setPrice(new BigDecimal("15000.5"));
        assertEquals(new BigDecimal("15000.50"),
                learningPackage.getPrice());

        learningPackage.setPrice(new BigDecimal("15000.50"));
        assertEquals(new BigDecimal("15000.50"),
                learningPackage.getPrice());
    }

    @Test
    void validatesPriceLimitsAndRounding() {
        LearningPackage learningPackage = createValidPackage();

        assertThrows(ValidationException.class,
                () -> learningPackage.setPrice(null));

        assertThrows(ValidationException.class,
                () -> learningPackage.setPrice(new BigDecimal("0")));

        assertThrows(ValidationException.class,
                () -> learningPackage.setPrice(new BigDecimal("-1")));

        assertThrows(ValidationException.class,
                () -> learningPackage.setPrice(
                        new BigDecimal("1000000.01")));

        assertThrows(ValidationException.class,
                () -> learningPackage.setPrice(
                        new BigDecimal("15000.555")));

        assertDoesNotThrow(
                () -> learningPackage.setPrice(new BigDecimal("0.01")));

        assertDoesNotThrow(
                () -> learningPackage.setPrice(
                        new BigDecimal("1000000.00")));
    }

    @Test
    void validatesLessonCountLimits() {
        LearningPackage learningPackage = createValidPackage();

        assertThrows(ValidationException.class,
                () -> learningPackage.setLessonCount(0));

        assertThrows(ValidationException.class,
                () -> learningPackage.setLessonCount(101));

        assertDoesNotThrow(
                () -> learningPackage.setLessonCount(1));

        assertDoesNotThrow(
                () -> learningPackage.setLessonCount(100));
    }

    @Test
    void validatesValidityDaysLimits() {
        LearningPackage learningPackage = createValidPackage();

        assertThrows(ValidationException.class,
                () -> learningPackage.setValidityDays(0));

        assertThrows(ValidationException.class,
                () -> learningPackage.setValidityDays(366));

        assertDoesNotThrow(
                () -> learningPackage.setValidityDays(1));

        assertDoesNotThrow(
                () -> learningPackage.setValidityDays(365));
    }

    @Test
    void supportsBothActiveValues() {
        LearningPackage learningPackage = createValidPackage();

        learningPackage.setActive(false);
        assertFalse(learningPackage.isActive());

        learningPackage.setActive(true);
        assertTrue(learningPackage.isActive());
    }

    @Test
    void invalidUpdatesPreservePreviousValues() {
        LearningPackage learningPackage = createValidPackage();

        assertThrows(ValidationException.class,
                () -> learningPackage.setName(" "));

        assertEquals("Beginner Package", learningPackage.getName());

        assertThrows(ValidationException.class,
                () -> learningPackage.setDescription("Too short"));

        assertEquals(
                "A package for new driving students.",
                learningPackage.getDescription()
        );

        assertThrows(ValidationException.class,
                () -> learningPackage.setPrice(
                        new BigDecimal("15000.555")));

        assertEquals(
                new BigDecimal("15000.00"),
                learningPackage.getPrice()
        );

        assertThrows(ValidationException.class,
                () -> learningPackage.setLessonCount(0));

        assertEquals(10, learningPackage.getLessonCount());

        assertThrows(ValidationException.class,
                () -> learningPackage.setValidityDays(0));

        assertEquals(30, learningPackage.getValidityDays());
    }

    @Test
    void jsonRoundTripPreservesEveryField()
            throws JsonProcessingException {

        LearningPackage original = createValidPackage();
        original.setActive(false);

        String json = objectMapper.writeValueAsString(original);

        LearningPackage restored = objectMapper.readValue(
                json, LearningPackage.class
        );

        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getName(), restored.getName());
        assertEquals(original.getDescription(), restored.getDescription());
        assertEquals(original.getPrice(), restored.getPrice());
        assertEquals(original.getLessonCount(), restored.getLessonCount());
        assertEquals(original.getValidityDays(), restored.getValidityDays());
        assertEquals(original.isActive(), restored.isActive());
    }


    private ObjectNode validPackageJson() {
        return objectMapper.valueToTree(createValidPackage());
    }

    @Test
    void rejectsEachMissingRequiredJsonProperty() {
        String[] properties = {
                "id", "name", "description", "price",
                "lessonCount", "validityDays", "active"
        };

        for (String property : properties) {
            ObjectNode json = validPackageJson();
            json.remove(property);

            assertThrows(
                    MismatchedInputException.class,
                    () -> objectMapper.treeToValue(json, LearningPackage.class),
                    "Expected missing property to be rejected: " + property
            );
        }
    }

    @Test
    void rejectsEachExplicitNullRequiredJsonProperty() {
        String[] properties = {
                "id", "name", "description", "price",
                "lessonCount", "validityDays", "active"
        };

        for (String property : properties) {
            ObjectNode json = validPackageJson();
            json.putNull(property);

            ValueInstantiationException exception = assertThrows(
                    ValueInstantiationException.class,
                    () -> objectMapper.treeToValue(json, LearningPackage.class),
                    "Expected null property to be rejected: " + property
            );

            assertInstanceOf(ValidationException.class, exception.getCause());
        }
    }

    @Test
    void rejectsInvalidDomainValueInValidJsonStructure() {
        ObjectNode json = validPackageJson();
        json.put("id", "P000");

        ValueInstantiationException exception = assertThrows(
                ValueInstantiationException.class,
                () -> objectMapper.treeToValue(json, LearningPackage.class)
        );

        assertInstanceOf(ValidationException.class, exception.getCause());
    }


    private LearningPackage createPackageWithName(String name) {
        return new LearningPackage(
                "P001", name,
                "A package for new driving students.",
                new BigDecimal("15000.00"), 10, 30, true
        );
    }

    private LearningPackage createPackageWithDescription(
            String description) {

        return new LearningPackage(
                "P001", "Beginner Package",
                description,
                new BigDecimal("15000.00"), 10, 30, true
        );
    }
}