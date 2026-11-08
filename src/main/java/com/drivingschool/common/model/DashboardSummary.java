/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : DashboardSummary.java
WHAT IT DOES (simple words): Keeps the details and rules of dashboard summary.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.common.model;

// OOP: ENCAPSULATION - Counts stay private; the page reads them through getters.
public class DashboardSummary {
    private int studentCount;
    private int instructorCount;
    private int vehicleCount;
    private int packageCount;
    private int pendingBookings;
    private int confirmedBookings;
    private int completedBookings;
    private int awaitingPayments;
    private java.math.BigDecimal paidRevenue = java.math.BigDecimal.ZERO;

    // Shares the student count through a public method while the field stays private.
    public int getStudentCount() { return studentCount; }
    // Stores the student count after the checks required by this record.
    public void setStudentCount(int value) {
        if (value < 0) throw new IllegalArgumentException("Dashboard totals cannot be negative.");
        studentCount = value;
    }

    // Shares the instructor count through a public method while the field stays private.
    public int getInstructorCount() { return instructorCount; }
    // Stores the instructor count after the checks required by this record.
    public void setInstructorCount(int value) {
        if (value < 0) throw new IllegalArgumentException("Dashboard totals cannot be negative.");
        instructorCount = value;
    }

    // Shares the vehicle count through a public method while the field stays private.
    public int getVehicleCount() { return vehicleCount; }
    // Stores the vehicle count after the checks required by this record.
    public void setVehicleCount(int value) {
        if (value < 0) throw new IllegalArgumentException("Dashboard totals cannot be negative.");
        vehicleCount = value;
    }

    // Shares the package count through a public method while the field stays private.
    public int getPackageCount() { return packageCount; }
    // Stores the package count after the checks required by this record.
    public void setPackageCount(int value) {
        if (value < 0) throw new IllegalArgumentException("Dashboard totals cannot be negative.");
        packageCount = value;
    }

    // Shares the pending bookings through a public method while the field stays private.
    public int getPendingBookings() { return pendingBookings; }
    // Stores the pending bookings after the checks required by this record.
    public void setPendingBookings(int value) {
        if (value < 0) throw new IllegalArgumentException("Dashboard totals cannot be negative.");
        pendingBookings = value;
    }

    // Shares the confirmed bookings through a public method while the field stays private.
    public int getConfirmedBookings() { return confirmedBookings; }
    // Stores the confirmed bookings after the checks required by this record.
    public void setConfirmedBookings(int value) {
        if (value < 0) throw new IllegalArgumentException("Dashboard totals cannot be negative.");
        confirmedBookings = value;
    }

    // Shares the completed bookings through a public method while the field stays private.
    public int getCompletedBookings() { return completedBookings; }
    // Stores the completed bookings after the checks required by this record.
    public void setCompletedBookings(int value) {
        if (value < 0) throw new IllegalArgumentException("Dashboard totals cannot be negative.");
        completedBookings = value;
    }

    // Shows the cashier how many requests still need a payment decision.
    public int getAwaitingPayments() { return awaitingPayments; }

    // Keeps the dashboard count valid even when there are no waiting receipts.
    public void setAwaitingPayments(int value) {
        if (value < 0) throw new IllegalArgumentException("Dashboard totals cannot be negative.");
        awaitingPayments = value;
    }

    // Shares the paid revenue through a public method while the field stays private.
    public java.math.BigDecimal getPaidRevenue() { return paidRevenue; }
    // Stores the paid revenue after the checks required by this record.
    public void setPaidRevenue(java.math.BigDecimal value) {
        if (value == null || value.signum() < 0) throw new IllegalArgumentException("Dashboard totals cannot be negative.");
        paidRevenue = value;
    }
}
