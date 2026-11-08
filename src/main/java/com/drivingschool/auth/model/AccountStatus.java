/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : AccountStatus.java
WHAT IT DOES (simple words): Keeps the details and rules of account status.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.auth.model;

/** Lifecycle state used to decide whether an account may sign in. */
public enum AccountStatus {
    PENDING,
    ACTIVE,
    SUSPENDED
}
