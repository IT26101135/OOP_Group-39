/**
 * ============================================================
 * MEMBER   : IT26101136 - [Name]
 * MODULE   : Learning packages and enrollment
 * FILE     : EnrollmentAdministration.java
 * WHAT IT DOES (simple words): Lists the payment jobs an administrator may perform.
 * REAL-LIFE EXAMPLE: A cashier's checklist separates receipt work from student forms.
 * OOP USED : Abstraction through a small interface
 * CRUD     : READ and UPDATE enrollment payment requests
 * ============================================================
 */
package com.drivingschool.learningpackage.service;

import com.drivingschool.learningpackage.model.Enrollment;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

// OOP: ABSTRACTION - The admin page asks for payment operations, not file details.
// SOLID: I - Student pages do not need to depend on this administrator contract.
public interface EnrollmentAdministration {
    // Lists only the receipts that are still waiting at the cashier desk.
    // CRUD: READ   - finds or lists enrollments
    List<Enrollment> awaitingPayments(HttpServletRequest request);
    // Confirms one payment and gives the student their lesson tickets.
    // CRUD: UPDATE - changes an existing enrollment
    Enrollment markPaid(HttpServletRequest request, String enrollmentId);
    // Keeps a cancelled request in history with an optional explanation.
    // CRUD: UPDATE - changes an existing enrollment
    Enrollment cancelRequest(HttpServletRequest request, String enrollmentId, String reason);
    // Supplies a readable student name for the administrator's receipt list.
    // CRUD: READ   - finds or lists student accounts
    String studentName(HttpServletRequest request, String studentId);
    // Supplies a readable course title without changing a receipt's saved amount.
    // CRUD: READ   - finds or lists packages
    String packageName(String packageId);
}
