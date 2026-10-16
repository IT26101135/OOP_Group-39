package com.drivingschool.common.exception;

/**
 * Thrown when an operation requires a record that does not exist.
 */
public class RecordNotFoundException extends RuntimeException {

    public RecordNotFoundException(String message) {
        super(message);
    }
}