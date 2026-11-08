/**
============================================================
MEMBER   : IT26101134 - [Name]
MODULE   : Bookings, FIFO queue and scheduling
FILE     : LessonIntervalUtils.java
WHAT IT DOES (simple words): Provides lesson interval utils for scheduling.
REAL-LIFE EXAMPLE: A reception waiting line handles the oldest request first.
OOP USED : Encapsulation (private fields and public methods)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/

package com.drivingschool.booking.util;

import com.drivingschool.common.exception.ValidationException;
import java.time.LocalDateTime;

public final class LessonIntervalUtils {

    // Receives the helpers or validates the details needed to create this object.
    private LessonIntervalUtils() {
        // Prevent creating objects of this utility class.
    }

    // Treats touching lesson boundaries as free, but overlapping time as a conflict.
    public static boolean overlaps(
            LocalDateTime firstStart,
            LocalDateTime firstEnd,
            LocalDateTime secondStart,
            LocalDateTime secondEnd) {

        if (firstStart == null || firstEnd == null
                || secondStart == null || secondEnd == null) {
            throw new ValidationException(
                    "Lesson start and end times cannot be null.");
        }

        if (!firstStart.isBefore(firstEnd)) {
            throw new ValidationException(
                    "The first lesson must end after it starts.");
        }

        if (!secondStart.isBefore(secondEnd)) {
            throw new ValidationException(
                    "The second lesson must end after it starts.");
        }

        return firstStart.isBefore(secondEnd)
                && secondStart.isBefore(firstEnd);
    }
}
