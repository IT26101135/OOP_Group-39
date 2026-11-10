/**
 * ============================================================
 * MEMBER   : IT26101135 - [Name]
 * MODULE   : Shared foundation
 * FILE     : RecordNotFoundException.java
 * WHAT IT DOES (simple words): Carries a clear explanation when an operation cannot continue.
 * REAL-LIFE EXAMPLE: The receptionist explains what went wrong and what to do next.
 * OOP USED : Exception handling
 * CRUD     : Supports errors during CREATE, READ, UPDATE and DELETE
 * ============================================================
 */
package com.drivingschool.common.exception;

/**
 * Thrown when an operation requires a record that does not exist.
 */
public class RecordNotFoundException extends RuntimeException {

    // Keeps the reason so the page can explain this problem to the user.
    public RecordNotFoundException(String message) {
        super(message);
    }
}