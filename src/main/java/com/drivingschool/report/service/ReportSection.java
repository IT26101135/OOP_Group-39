/**
============================================================
MEMBER   : IT26101137 - [Name]
MODULE   : Reviews and Reports
FILE     : ReportSection.java
WHAT IT DOES (simple words): Keeps reviews and reports details clear and safe for the school.
REAL-LIFE EXAMPLE: Like a feedback book and a school manager counting its records.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for reviews; reports READ saved data
============================================================
*/
package com.drivingschool.report.service;

import java.util.List;

// SOLID: S - This small view object carries one report; it does not read files or calculate totals.
public class ReportSection {
    private final String title;
    private final List<ReportRow> rows;
    private final boolean chart;

    // Groups rows under one heading and records whether numeric chart bars are useful.
    public ReportSection(String title, List<ReportRow> rows, boolean chart) {
        this.title = title;
        this.rows = List.copyOf(rows);
        this.chart = chart;
    }

    // Names this part of the school report.
    public String getTitle() { return title; }

    // Returns a read-only list so the page cannot alter the calculated totals.
    public List<ReportRow> getRows() { return rows; }

    // Text-only sections keep their data tables without pretending text is a numeric chart.
    public boolean isChart() { return chart; }
}
