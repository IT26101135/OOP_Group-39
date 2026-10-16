package com.drivingschool.common.exception;

/**
 * Thrown when a value violates an application validation rule.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}