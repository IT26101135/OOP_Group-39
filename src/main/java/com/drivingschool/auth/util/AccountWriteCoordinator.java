/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : AccountWriteCoordinator.java
WHAT IT DOES (simple words): Supports account write coordinator for the account or course workflow.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.auth.util;

import java.util.function.Supplier;

// Only one account writer may enter at a time, like one person using a cashier desk.
// Supplier<T> is Java's small callback interface: operation.get() runs the supplied work
// and returns its result. Existing callers use () -> { ... } to name that work.
// Keeping this shared lock prevents two modules from overwriting each other's changes.
public final class AccountWriteCoordinator {

    // Receives the helpers this class needs so each job stays separate.
    private AccountWriteCoordinator() {
    }

    /**
     * Coordinates account-changing operations within this application JVM.
     * Every account writer must use this method for its complete operation.
     */
    public static synchronized <T> T execute(Supplier<T> operation) {
        return operation.get();
    }
}