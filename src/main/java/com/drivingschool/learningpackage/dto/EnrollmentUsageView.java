/**
 * MEMBER   : IT26101136 - [Name]
 * MODULE   : Learning packages and enrollment
 * FILE     : EnrollmentUsageView.java
 * WHAT IT DOES: Carries safe package and lesson labels to a page.
 * OOP USED : Encapsulation; presentation data stays separate from stored records.
 * CRUD     : READ only; no records are changed.
 */
package com.drivingschool.learningpackage.dto;

public class EnrollmentUsageView {
    private final String packageName;
    private final String type;
    private final String validity;
    private final String status;
    private final String lessonsLeft;
    private final String lessonCount;
    private final int completed;
    private final int requested;
    private final int confirmed;
    private final boolean bookable;
    private final String nextUrl;
    private final String nextLabel;
    private final String message;

    public EnrollmentUsageView(String packageName, String type, String validity, String status, String lessonsLeft, String lessonCount, int completed, int requested, int confirmed, boolean bookable, String nextUrl, String nextLabel, String message) {
        this.packageName = packageName;
        this.type = type;
        this.validity = validity;
        this.status = status;
        this.lessonsLeft = lessonsLeft;
        this.lessonCount = lessonCount;
        this.completed = completed;
        this.requested = requested;
        this.confirmed = confirmed;
        this.bookable = bookable;
        this.nextUrl = nextUrl;
        this.nextLabel = nextLabel;
        this.message = message;
    }
    public String getPackageName() { return packageName; }
    public String getType() { return type; }
    public String getValidity() { return validity; }
    public String getStatus() { return status; }
    public String getLessonsLeft() { return lessonsLeft; }
    public String getLessonCount() { return lessonCount; }
    public int getCompleted() { return completed; }
    public int getRequested() { return requested; }
    public int getConfirmed() { return confirmed; }
    public boolean isBookable() { return bookable; }
    public String getNextUrl() { return nextUrl; }
    public String getNextLabel() { return nextLabel; }
    public String getMessage() { return message; }
}
