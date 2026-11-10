/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : StudentLookup.java
WHAT IT DOES (simple words): Defines the small set of operations another class promises to provide.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.student.api;

import com.drivingschool.student.dto.StudentSummary;

import java.util.Optional;

// OOP: ABSTRACTION - Callers use this small promise instead of implementation details.
// SOLID: I - Only the operations needed by these callers are exposed.
public interface StudentLookup {

    // Provides the exists operation promised by this small contract.
    boolean exists(String studentId);

    // Provides the find by id operation promised by this small contract.
    Optional<StudentSummary> findById(String studentId);
}