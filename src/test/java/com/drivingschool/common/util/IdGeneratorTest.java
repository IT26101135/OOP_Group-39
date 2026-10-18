package com.drivingschool.common.util;

import com.drivingschool.common.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IdGeneratorTest {

    @Test
    void generatesFirstStudentId() {
        assertEquals("S001", IdGenerator.nextId("S", 0));
    }

    @Test
    void padsNumbersWithLeadingZeroes() {
        assertEquals("I025", IdGenerator.nextId("I", 24));
    }

    @Test
    void movesFromTwoDigitsToThreeDigits() {
        assertEquals("B100", IdGenerator.nextId("B", 99));
    }

    @Test
    void allowsTheLastThreeDigitId() {
        assertEquals("R999", IdGenerator.nextId("R", 998));
    }

    @Test
    void rejectsInvalidPrefixes() {
        String[] invalidPrefixes = {null, "", "s", "ST", "1", " S"};

        for (String prefix : invalidPrefixes) {
            assertThrows(
                    ValidationException.class,
                    () -> IdGenerator.nextId(prefix, 0)
            );
        }
    }

    @Test
    void rejectsNegativeSequenceNumbers() {
        assertThrows(
                ValidationException.class,
                () -> IdGenerator.nextId("S", -1)
        );
    }

    @Test
    void rejectsExhaustedOrOutOfRangeSequences() {
        assertThrows(
                ValidationException.class,
                () -> IdGenerator.nextId("S", 999)
        );

        assertThrows(
                ValidationException.class,
                () -> IdGenerator.nextId("S", Integer.MAX_VALUE)
        );
    }
}