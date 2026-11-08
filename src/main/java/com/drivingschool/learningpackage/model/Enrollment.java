// Module owner: IT26101136
package com.drivingschool.learningpackage.model;

import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.common.model.Identifiable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Enrollment implements Identifiable {
    private final String id;
    private final String studentId;
    private final String packageId;
    private final LocalDateTime enrolledAt;
    private final BigDecimal amount;
    private final String paymentStatus;

    @JsonCreator
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

    @Override public String getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getPackageId() { return packageId; }
    public LocalDateTime getEnrolledAt() { return enrolledAt; }
    public BigDecimal getAmount() { return amount; }
    public String getPaymentStatus() { return paymentStatus; }
}
