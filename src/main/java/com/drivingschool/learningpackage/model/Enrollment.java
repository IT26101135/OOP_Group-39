/**
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages and enrollment
FILE     : Enrollment.java
WHAT IT DOES (simple words): Keeps a student's package request, price and payment state.
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
    // One clear state controls both the receipt label and whether lessons may be used.
    private String status;
    // Optional fields keep older saved enrollments readable without migration.
    private String learningGoal;
    private LocalDate validUntil;
    private String cancellationReason;


    // Keeps existing Java callers working while new saved records gain a clear state.
    public Enrollment(String id, String studentId, String packageId,
                      LocalDateTime enrolledAt, BigDecimal amount, String paymentStatus) {
        this(id, studentId, packageId, enrolledAt, amount, paymentStatus, null);
    }

    @JsonCreator
    // Loads old receipts safely; a missing payment state means an existing paid receipt.
    public Enrollment(@JsonProperty("id") String id,
                      @JsonProperty("studentId") String studentId,
                      @JsonProperty("packageId") String packageId,
                      @JsonProperty("enrolledAt") LocalDateTime enrolledAt,
                      @JsonProperty("amount") BigDecimal amount,
                      @JsonProperty("paymentStatus") String paymentStatus,
                      @JsonProperty("status") String status) {
        // Step 1: Check the permanent numbers and the price captured at enrollment.
        validateDetails(id, studentId, packageId, enrolledAt, amount);
        this.id = id;
        this.studentId = studentId;
        this.packageId = packageId;
        this.enrolledAt = enrolledAt;
        this.amount = amount;
        // Step 2: Prefer the new state; retain explicit old unpaid receipts as unpaid.
        setStatus(initialStatus(paymentStatus, status));
    }

    // Checks the receipt belongs to real numbered records and has a positive saved price.
    private void validateDetails(String id, String studentId, String packageId,
                                 LocalDateTime enrolledAt, BigDecimal amount) {
        if (id == null || !id.matches("E(?!000)[0-9]{3}")) {
            throw new ValidationException("Enrollment ID must be E001 to E999.");
        }
        if (studentId == null || !studentId.matches("S(?!000)[0-9]{3}")) {
            throw new ValidationException("A valid student ID is required.");
        }
        if (packageId == null || !packageId.matches("P(?!000)[0-9]{3}")) {
            throw new ValidationException("A valid package ID is required.");
        }
        if (enrolledAt == null || amount == null || amount.signum() <= 0) {
            throw new ValidationException("Enrollment details are invalid.");
        }
    }

    // Defaults only missing old payment details to Paid; unknown payment words are refused.
    private String initialStatus(String paymentStatus, String status) {
        if (status != null && !status.isBlank()) return status;
        if ("UNPAID".equalsIgnoreCase(paymentStatus)) return "AWAITING_PAYMENT";
        if (paymentStatus == null || paymentStatus.isBlank()
                || "PAID".equalsIgnoreCase(paymentStatus)) return "PAID";
        throw new ValidationException("Enrollment payment status is invalid.");
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
    public String getPaymentStatus() { return isPaid() ? "PAID" : "UNPAID"; }

    // Gives pages and services the same clear enrollment state.
    public String getStatus() { return status; }

    // OOP: ENCAPSULATION - Only the three supported receipt states may be stored.
    // Stores a checked state; admin permission and transition rules belong in the service.
    public void setStatus(String status) {
        if (!"AWAITING_PAYMENT".equals(status) && !"PAID".equals(status)
                && !"CANCELLED".equals(status)) {
            throw new ValidationException("Enrollment status is invalid.");
        }
        this.status = status;
    }

    // Keeps old paymentStatus readers working without storing two conflicting values.
    @JsonIgnore
    public boolean isPaid() { return "PAID".equals(status); }

    // Uses the existing permanent number so old receipts need no reference migration.
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getReferenceCode() { return "ENR-" + id; }

    // Shows plain words rather than the saved uppercase state on student pages.
    @JsonIgnore
    public String getStatusLabel() {
        if ("AWAITING_PAYMENT".equals(status)) return "Awaiting payment";
        if ("CANCELLED".equals(status)) return "Cancelled";
        return "Paid";
    }

    // Keeps the administrator's explanation with a cancelled request for the student's records.
    public String getCancellationReason() { return cancellationReason; }

    // Checks an optional note before it becomes part of the saved payment history.
    public void setCancellationReason(String reason) {
        String cleaned = reason == null ? null : reason.trim();
        if (cleaned != null && cleaned.length() > 300) {
            throw new ValidationException("Cancellation reason must be at most 300 characters.");
        }
        cancellationReason = cleaned;
    }
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
