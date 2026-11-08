// Module owner: IT26101135
package com.drivingschool.admin.controller;

import com.drivingschool.auth.annotation.RequireAdmin;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequireAdmin
public class AdminDashboardController {

    @GetMapping("/admin/dashboard")
    public String showDashboard(HttpServletResponse response, Model model) {
        response.setHeader("Cache-Control", "no-store");
        model.addAttribute("navAdminDashboardActive", true);
        return "admin/dashboard";
    }
}