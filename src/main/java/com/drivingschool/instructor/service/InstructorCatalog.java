/**
============================================================
MEMBER   : IT26101133 - [Name]
MODULE   : Instructors and bubble sort
FILE     : InstructorCatalog.java
WHAT IT DOES (simple words): Applies the instructor catalog rules.
REAL-LIFE EXAMPLE: A teacher diary keeps teaching hours, lesson notes and profile details organised.
OOP USED : Abstraction (a small interface)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/
package com.drivingschool.instructor.service;

import com.drivingschool.instructor.model.Instructor;
import java.util.List;

// OOP: ABSTRACTION - Callers use this promise instead of the implementation details.
// SOLID: I - This interface describes one small job for its callers.
public interface InstructorCatalog {
    // Passes the chosen ordering rule to the hand-written bubble sort.
    List<Instructor> sortInstructors(String sortBy, boolean ascending);
    // Finds one profile or gives a friendly missing-record error.
    Instructor getInstructorById(String id);
}
