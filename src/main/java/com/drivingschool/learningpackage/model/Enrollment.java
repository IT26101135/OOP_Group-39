/**
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages and enrollment
FILE     : Enrollment.java
WHAT IT DOES (simple words): Keeps the details and rules of enrollment.
REAL-LIFE EXAMPLE: A course folder keeps the learning plan and enrollment details together.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.learningpackage.model;

import com.drivingschool.common.exception.ValidationException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

// OOP: ENCAPSULATION - Private details are changed only through validation.
public class Enrollment implements LearningRecord {
    private final String id;
    private final String studentId;
    private final String packageId;
    private final LocalDateTime enrolledAt;
    private final BigDecimal amount;
    private final String paymentStatus;
    // Optional fields keep older saved enrollments readable without migration.
    private String learningGoal;
    private LocalDate validUntil;


    @JsonCreator
    // Receives the helpers this class needs so each job stays separate.
    public Enrollment(@JsonProperty("id") String id,
                      @JsonProperty("studentId") String studentId,
                      @JsonProperty("packageId") String packageId,
                      @JsonProperty("enrolledAt") LocalDateTime enrolledAt,
                      @JsonProperty("amount") BigDecimal amount,
                      @JsonProperty("paymentStatus") String paymentStatus) {
        if (id == null || !id.matches("E(?!000)[0-9]{3}")) {
            throw new ValidationException("Enrollment ID must be E001 to E999.");
        }
        if (studentId == null || !studentId.matches("S(?!000)[0-9]{3}")) {
            throw new ValidationException("A valid student ID is required.");
        }
        if (packageId == null || !packageId.matches("P(?!000)[0-9]{3}")) {
            throw new ValidationException("A valid package ID is required.");
        }
        if (enrolledAt == null || amount == null || amount.signum() <= 0
                || paymentStatus == null || paymentStatus.isBlank()) {
            throw new ValidationException("Enrollment details are invalid.");
        }
        this.id = id;
        this.studentId = studentId;
        this.packageId = packageId;
        this.enrolledAt = enrolledAt;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
    }

    // Gives storage the permanent number on this enrollment folder.
    @Override public String getId() { return id; }
    // Shares the student id through a public method while the field stays private.
    public String getStudentId() { return studentId; }
    // Shares the package id through a public method while the field stays private.
    public String getPackageId() { return packageId; }
    // Shares the enrolled at through a public method while the field stays private.
    public LocalDateTime getEnrolledAt() { return enrolledAt; }
    // Shares the amount through a public method while the field stays private.
    public BigDecimal getAmount() { return amount; }
    // Shares the payment status through a public method while the field stays private.
    public String getPaymentStatus() { return paymentStatus; }
    // Shares the learning goal through a public method while the field stays private.
    public String getLearningGoal() { return learningGoal; }

    // Stores the learning goal after the checks required by this record.
    public void setLearningGoal(String learningGoal) {
        String cleaned = learningGoal == null ? null : learningGoal.trim();
        if (cleaned != null && cleaned.length() > 300) {
            throw new ValidationException("Learning goal must be at most 300 characters.");
        }
        this.learningGoal = cleaned;
    }

    // Shares the valid until through a public method while the field stays private.
    public LocalDate getValidUntil() { return validUntil; }

    // Stores the valid until after the checks required by this record.
    public void setValidUntil(LocalDate validUntil) {
        if (validUntil != null && validUntil.isBefore(enrolledAt.toLocalDate())) {
            throw new ValidationException("Enrollment expiry cannot be before enrollment.");
        }
        this.validUntil = validUntil;
    }
    // OOP: POLYMORPHISM - LearningRecord headings differ for a course and an enrollment.
    // SOLID: L - Both records provide a readable heading through the same small contract.
    @Override
    @JsonIgnore
    // Shares the display name through a public method while the field stays private.
    public String getDisplayName() { return "Enrollment " + id; }
}
