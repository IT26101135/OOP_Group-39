/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : LoginResult.java
WHAT IT DOES (simple words): Applies the rules for login result.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.auth.service;

public enum LoginResult {
    SUCCESS,
    INVALID_CREDENTIALS,
    PENDING_APPROVAL,
    SUSPENDED
}
