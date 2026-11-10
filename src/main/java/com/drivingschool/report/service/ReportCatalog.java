/**
============================================================
MEMBER   : IT26101137 - [Name]
MODULE   : Reviews and Reports
FILE     : ReportCatalog.java
WHAT IT DOES (simple words): Keeps reviews and reports details clear and safe for the school.
REAL-LIFE EXAMPLE: Like a feedback book and a school manager counting its records.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for reviews; reports READ saved data
============================================================
*/
package com.drivingschool.report.service;

import java.util.List;

// SOLID: I - The report page and CSV download need only this read operation.
public interface ReportCatalog {
    // CRUD: READ   - finds or lists report totals
    // Builds the same seven sections for the screen and the download.
    List<ReportSection> all();

    // CRUD: READ - business tables shared by the report page, print and CSV.
    List<BusinessReportSection> business();
}
