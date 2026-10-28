
package com.drivingschool.booking.util;

import com.drivingschool.common.exception.ValidationException;
import java.time.LocalDateTime;

public final class LessonIntervalUtils {

    private LessonIntervalUtils() {
        // Prevent creating objects of this utility class.
    }

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
