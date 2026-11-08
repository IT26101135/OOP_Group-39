/**
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages and enrollment
FILE     : EnrollmentOperations.java
WHAT IT DOES (simple words): Applies the rules for enrollment operations.
REAL-LIFE EXAMPLE: A course folder keeps the learning plan and enrollment details together.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.learningpackage.service;

import com.drivingschool.learningpackage.model.Enrollment;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

// OOP: ABSTRACTION - Pages depend on this promise rather than file details.
// SOLID: I - This contract exposes only operations for one kind of caller.
public interface EnrollmentOperations {
    // Starts a student learning plan after checking the account and course.
    Enrollment enroll(HttpServletRequest request, String packageId);
    // Lists only the signed-in student learning plans.
    List<Enrollment> ownEnrollments(HttpServletRequest request);
    // Changes the student note without changing payment or lesson balances.
    Enrollment updateLearningGoal(HttpServletRequest request, String enrollmentId, String learningGoal);
    // Removes only an owned, inactive, unused unpaid record.
    void deleteOwnEnrollment(HttpServletRequest request, String enrollmentId);
}
