/**
============================================================
MEMBER   : IT26101137 - [Name]
MODULE   : Reviews and Reports
FILE     : AdminReportController.java
WHAT IT DOES (simple words): Shows report charts and exports the same figures to CSV.
REAL-LIFE EXAMPLE: Like a feedback book and a school manager counting its records.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for reviews; reports READ saved data
============================================================
*/
package com.drivingschool.report.controller;

import com.drivingschool.auth.annotation.RequireAdmin;
import com.drivingschool.report.service.ReportCatalog;
import com.drivingschool.report.service.ReportSection;
import com.drivingschool.report.service.ReportRow;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.io.IOException;
import java.io.PrintWriter;

@Controller
@RequireAdmin
@RequestMapping("/admin/reports")
public class AdminReportController {
    // SOLID: D - The controller receives the small report interface through its constructor.
    private final ReportCatalog reports;

    // Accepts the report provider chosen by Spring.
    public AdminReportController(ReportCatalog reports) { this.reports = reports; }

    // CRUD: READ   - finds or lists report totals
    // Loads the same report rows that the CSV download will use.
    @GetMapping
    public String show(Model model, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        model.addAttribute("reports", reports.all());
        return "admin/reports";
    }

    // CRUD: READ   - finds or lists report totals
    // Writes each report row with quoted CSV cells; no files or records are changed.
    @GetMapping("/export.csv")
    public void export(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Content-Disposition", "attachment; filename=drivespark-reports.csv");
        PrintWriter writer = response.getWriter();
        writer.write("report,label,value\r\n");
        for (ReportSection report : reports.all()) {
            for (ReportRow row : report.getRows()) {
                writer.write(csv(report.getTitle()) + "," + csv(row.getLabel()) + "," + csv(row.getValue()) + "\r\n");
            }
        }
        writer.flush();
    }

    // Protects spreadsheet cells from formula prefixes and escapes embedded quotation marks.
    private String csv(String value) {
        String safeValue = value;
        String trimmed = value.stripLeading();
        if (!trimmed.isEmpty() && "=+-@".indexOf(trimmed.charAt(0)) >= 0) {
            safeValue = "'" + value;
        }
        return "\"" + safeValue.replace("\"", "\"\"") + "\"";
    }
}
