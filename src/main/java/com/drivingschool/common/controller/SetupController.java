// Module owner: IT26101135
package com.drivingschool.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SetupController {

    @GetMapping("/setup-check")
    public String showSetupPage(Model model) {
        // Send these values to the JSP page.
        model.addAttribute("projectName", "DriveSpark Academy");
        model.addAttribute("message", "Our website is available.");

        // Spring adds the configured prefix and suffix.
        return "common/setup-check";
    }
}