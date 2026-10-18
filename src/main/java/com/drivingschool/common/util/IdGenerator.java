package com.drivingschool.common.util;

import com.drivingschool.common.exception.ValidationException;

import java.util.Locale;

/**
 * Formats the next ID using a prefix and the last issued number.
 *
 * This class does not read files or remember previously issued IDs.
 * The repository will manage persistent numbering and synchronization.
 */
public final class IdGenerator {

    private static final int MAX_SEQUENCE = 999;

    /**
     * Prevents creating objects of this utility class.
     */
    private IdGenerator() {
    }

    /**
     * Returns the next ID.
     *
     * Examples:
     * nextId("S", 0) returns "S001".
     * nextId("I", 24) returns "I025".
     *
     * @param prefix one uppercase English letter
     * @param lastIssuedNumber the last number issued, or 0 initially
     * @return the prefix followed by exactly three digits
     */
    public static String nextId(String prefix, int lastIssuedNumber) {

        if (prefix == null || !prefix.matches("[A-Z]")) {
            throw new ValidationException(
                    "ID prefix must be one uppercase letter."
            );
        }

        if (lastIssuedNumber < 0) {
            throw new ValidationException(
                    "The last issued ID number cannot be negative."
            );
        }

        if (lastIssuedNumber >= MAX_SEQUENCE) {
            throw new ValidationException(
                    "No more IDs are available for prefix " + prefix
                            + ". The maximum is " + prefix + MAX_SEQUENCE + "."
            );
        }

        int nextNumber = lastIssuedNumber + 1;

        return String.format(Locale.ROOT, "%s%03d", prefix, nextNumber);
    }
}