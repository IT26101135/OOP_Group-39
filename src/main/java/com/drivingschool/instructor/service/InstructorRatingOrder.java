/**
============================================================
MEMBER   : IT26101133 - [Name]
MODULE   : Instructors and bubble sort
FILE     : InstructorRatingOrder.java
WHAT IT DOES (simple words): Applies the instructor rating order rules.
REAL-LIFE EXAMPLE: A teacher diary keeps teaching hours, lesson notes and profile details organised.
OOP USED : Polymorphism (comparison method overriding)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/
package com.drivingschool.instructor.service;

import com.drivingschool.instructor.model.Instructor;

// OOP: POLYMORPHISM - The same compare method compares instructors by rating.
// SOLID: L - Every order returns a negative, zero or positive comparison consistently.
// SOLID: S - This class is responsible for application rules.
public class InstructorRatingOrder implements InstructorOrder {
    @Override
    // Compares two instructor cards using this class's chosen detail.
    public int compare(Instructor first, Instructor second) {
        return Double.compare(first.getAverageRating(), second.getAverageRating());
    }
}
