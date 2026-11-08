/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : StudentSummary.java
WHAT IT DOES (simple words): Shares safe student summary details with pages.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.student.dto;

import com.drivingschool.common.exception.ValidationException;

public final class StudentSummary {

    private final String id;
    private final String name;

    // Receives the helpers this class needs so each job stays separate.
    public StudentSummary(String id, String name) {
        if (id == null || !id.matches("S(?!000)[0-9]{3}")) {
            throw new ValidationException(
                    "Student ID must be between S001 and S999."
            );
        }

        if (name == null
                || name.trim().length() < 2
                || name.trim().length() > 100) {
            throw new ValidationException(
                    "Student name must contain 2 to 100 characters."
            );
        }

        this.id = id;
        this.name = name.trim();
    }

    // Shares the id through a public method while the field stays private.
    public String getId() {
        return id;
    }

    // Shares the name through a public method while the field stays private.
    public String getName() {
        return name;
    }
}