
package com.drivingschool.booking.util;

import com.drivingschool.common.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LessonIntervalUtilsTest {

    private LocalDateTime time(int day, int hour, int minute) {
        return LocalDateTime.of(2026, 10, day, hour, minute);
    }

    @Test
    void separatedIntervalsDoNotOverlap() {
        assertFalse(LessonIntervalUtils.overlaps(
                time(20, 9, 0), time(20, 10, 0),
                time(20, 11, 0), time(20, 12, 0)));
    }

    @Test
    void separatedIntervalsInReverseOrderDoNotOverlap() {
        assertFalse(LessonIntervalUtils.overlaps(
                time(20, 11, 0), time(20, 12, 0),
                time(20, 9, 0), time(20, 10, 0)));
    }

    @Test
    void adjacentIntervalsDoNotOverlap() {
        assertFalse(LessonIntervalUtils.overlaps(
                time(20, 9, 0), time(20, 10, 0),
                time(20, 10, 0), time(20, 11, 0)));
    }

    @Test
    void partiallyOverlappingIntervalsOverlap() {
        assertTrue(LessonIntervalUtils.overlaps(
                time(20, 9, 0), time(20, 11, 0),
                time(20, 10, 0), time(20, 12, 0)));
    }

    @Test
    void containedIntervalOverlaps() {
        assertTrue(LessonIntervalUtils.overlaps(
                time(20, 9, 0), time(20, 12, 0),
                time(20, 10, 0), time(20, 11, 0)));
    }

    @Test
    void identicalIntervalsOverlap() {
        assertTrue(LessonIntervalUtils.overlaps(
                time(20, 9, 0), time(20, 10, 0),
                time(20, 9, 0), time(20, 10, 0)));
    }

    @Test
    void sameStartWithDifferentEndsOverlaps() {
        assertTrue(LessonIntervalUtils.overlaps(
                time(20, 9, 0), time(20, 10, 0),
                time(20, 9, 0), time(20, 11, 0)));
    }

    @Test
    void sameEndWithDifferentStartsOverlaps() {
        assertTrue(LessonIntervalUtils.overlaps(
                time(20, 9, 0), time(20, 11, 0),
                time(20, 10, 0), time(20, 11, 0)));
    }

    @Test
    void sameClockTimesOnDifferentDatesDoNotOverlap() {
        assertFalse(LessonIntervalUtils.overlaps(
                time(20, 9, 0), time(20, 10, 0),
                time(21, 9, 0), time(21, 10, 0)));
    }

    @Test
    void intervalCrossingMidnightIsValid() {
        assertTrue(LessonIntervalUtils.overlaps(
                time(20, 23, 0), time(21, 1, 0),
                time(21, 0, 30), time(21, 2, 0)));
    }

    @Test
    void nullFirstStartThrowsException() {
        assertThrows(ValidationException.class, () ->
                LessonIntervalUtils.overlaps(
                        null, time(20, 10, 0),
                        time(20, 11, 0), time(20, 12, 0)));
    }

    @Test
    void nullFirstEndThrowsException() {
        assertThrows(ValidationException.class, () ->
                LessonIntervalUtils.overlaps(
                        time(20, 9, 0), null,
                        time(20, 11, 0), time(20, 12, 0)));
    }

    @Test
    void nullSecondStartThrowsException() {
        assertThrows(ValidationException.class, () ->
                LessonIntervalUtils.overlaps(
                        time(20, 9, 0), time(20, 10, 0),
                        null, time(20, 12, 0)));
    }

    @Test
    void nullSecondEndThrowsException() {
        assertThrows(ValidationException.class, () ->
                LessonIntervalUtils.overlaps(
                        time(20, 9, 0), time(20, 10, 0),
                        time(20, 11, 0), null));
    }

    @Test
    void equalFirstStartAndEndThrowsException() {
        assertThrows(ValidationException.class, () ->
                LessonIntervalUtils.overlaps(
                        time(20, 9, 0), time(20, 9, 0),
                        time(20, 11, 0), time(20, 12, 0)));
    }

    @Test
    void reversedFirstIntervalThrowsException() {
        assertThrows(ValidationException.class, () ->
                LessonIntervalUtils.overlaps(
                        time(20, 10, 0), time(20, 9, 0),
                        time(20, 11, 0), time(20, 12, 0)));
    }

    @Test
    void equalSecondStartAndEndThrowsException() {
        assertThrows(ValidationException.class, () ->
                LessonIntervalUtils.overlaps(
                        time(20, 9, 0), time(20, 10, 0),
                        time(20, 11, 0), time(20, 11, 0)));
    }

    @Test
    void reversedSecondIntervalThrowsException() {
        assertThrows(ValidationException.class, () ->
                LessonIntervalUtils.overlaps(
                        time(20, 9, 0), time(20, 10, 0),
                        time(20, 12, 0), time(20, 11, 0)));
    }

    @Test
    void overlapResultIsSymmetric() {
        boolean first = LessonIntervalUtils.overlaps(
                time(20, 9, 0), time(20, 11, 0),
                time(20, 10, 0), time(20, 12, 0));

        boolean second = LessonIntervalUtils.overlaps(
                time(20, 10, 0), time(20, 12, 0),
                time(20, 9, 0), time(20, 11, 0));

        assertEquals(first, second);
    }
}

