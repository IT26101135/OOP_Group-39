package com.drivingschool.common.exception;

/**
 * Thrown when a storage operation cannot be completed.
 *
 * The cause preserves the original failure for troubleshooting.
 */
public class StorageException extends RuntimeException {

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}