package com.drivingschool.common.exception;

/**
 * Thrown when a record conflicts with an existing unique value.
 */
public class DuplicateRecordException extends RuntimeException {

    public DuplicateRecordException(String message) {
        super(message);
    }
}