/**
============================================================
MEMBER   : IT26101137 - [Name]
MODULE   : Reviews and Reports
FILE     : ReportRow.java
WHAT IT DOES (simple words): Keeps reviews and reports details clear and safe for the school.
REAL-LIFE EXAMPLE: Like a feedback book and a school manager counting its records.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for reviews; reports READ saved data
============================================================
*/
package com.drivingschool.report.service;

// OOP: ENCAPSULATION - A report row exposes its label, value and safe bar width without setters.
public class ReportRow {
    private final String label;
    private final String value;
    private final int percentage;

    // Keeps the displayed number next to the length of its chart bar.
    public ReportRow(String label, String value, int percentage) {
        this.label = label;
        this.value = value;
        this.percentage = Math.max(0, Math.min(100, percentage));
    }

    // Names the group represented by this row.
    public String getLabel() { return label; }

    // Gives the exact stored-data total for the table and SVG label.
    public String getValue() { return value; }

    // Returns a width relative to this chart's largest value, never a percentage of all records.
    public int getPercentage() { return percentage; }
}
