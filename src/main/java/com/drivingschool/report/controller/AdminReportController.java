// Module owner: IT26101137
package com.drivingschool.report.controller;

import com.drivingschool.auth.annotation.RequireAdmin;
import com.drivingschool.report.service.AdminReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
@RequireAdmin
@RequestMapping("/admin/reports")
public class AdminReportController {
    private final AdminReportService reports;
    public AdminReportController(AdminReportService reports) { this.reports = reports; }

    @GetMapping
    public String show(Model model) { model.addAttribute("reports", reports.all()); return "admin/reports"; }

    @GetMapping("/export.csv")
    public void export(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=drivespark-reports.csv");
        var writer = response.getWriter();
        writer.write("report,label,value\r\n");
        reports.all().forEach((report, rows) -> rows.forEach((label, value) ->
                writer.write(csv(report) + "," + csv(label) + "," + csv(String.valueOf(value)) + "\r\n")));
        writer.flush();
    }
    private String csv(String value) { return "\"" + value.replace("\"", "\"\"") + "\""; }
}
