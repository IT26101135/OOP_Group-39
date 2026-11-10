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
    // CRUD: CREATE - adds a new enrollment and saves it
    Enrollment enroll(HttpServletRequest request, String packageId);
    // Lists only the signed-in student learning plans.
    // CRUD: READ   - finds or lists enrollments
    List<Enrollment> ownEnrollments(HttpServletRequest request);
    // Looks up one receipt only after checking that it belongs to the signed-in student.
    // CRUD: READ   - finds or lists enrollments
    Enrollment ownEnrollment(HttpServletRequest request, String enrollmentId);
    // Finds the course title without changing the price saved on the receipt.
    // CRUD: READ   - finds or lists packages
    String packageName(String packageId);
    // Changes the student note without changing payment or lesson balances.
    // CRUD: UPDATE - changes an existing enrollment
    Enrollment updateLearningGoal(HttpServletRequest request, String enrollmentId, String learningGoal);
    // Removes only an owned, inactive, unused unpaid record.
    // CRUD: DELETE - removes a enrollment
    void deleteOwnEnrollment(HttpServletRequest request, String enrollmentId);
}
