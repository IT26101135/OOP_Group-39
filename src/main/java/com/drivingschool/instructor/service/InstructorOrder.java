/**
============================================================
MEMBER   : IT26101133 - [Name]
MODULE   : Instructors and bubble sort
FILE     : InstructorOrder.java
WHAT IT DOES (simple words): Applies the instructor order rules.
REAL-LIFE EXAMPLE: A teacher diary keeps teaching hours, lesson notes and profile details organised.
OOP USED : Abstraction (a small interface)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/
package com.drivingschool.instructor.service;

import com.drivingschool.instructor.model.Instructor;

// OOP: ABSTRACTION - Bubble sort asks only how two instructor cards compare.
// SOLID: I - This small interface has just the comparison needed by the sort.
// OOP: ABSTRACTION - Callers use this promise instead of the implementation details.
// SOLID: I - This interface describes one small job for its callers.
public interface InstructorOrder {
    // Compares two instructor cards using this class's chosen detail.
    int compare(Instructor first, Instructor second);
}
