/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : AdminDashboardController.java
WHAT IT DOES (simple words): Prepares pages and accepts forms for admin dashboard.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.admin.controller;

import com.drivingschool.auth.annotation.RequireAdmin;
import jakarta.servlet.http.HttpServletResponse;
import com.drivingschool.common.service.DashboardSummaryProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequireAdmin
// SOLID: S - This class is responsible for web requests and pages.
public class AdminDashboardController {
    // SOLID: D - The web layer receives a small summary contract, not storage details.
    private final DashboardSummaryProvider summaries;

    // Receives the helpers this class needs so each job stays separate.
    public AdminDashboardController(DashboardSummaryProvider summaries) {
        this.summaries = summaries;
    }


    @GetMapping("/admin/dashboard")
    // Sends prepared totals to the dashboard instead of querying files inside JSP.
    public String showDashboard(HttpServletResponse response, Model model) {
        response.setHeader("Cache-Control", "no-store");
        model.addAttribute("navAdminDashboardActive", true);
        model.addAttribute("summary", summaries.readSummary());
        return "admin/dashboard";
    }
}