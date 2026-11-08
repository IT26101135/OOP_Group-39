// Module owner: IT26101135
package com.drivingschool.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.drivingschool.learningpackage.service.LearningPackageService;
import com.drivingschool.learningpackage.model.LearningPackage;

@Controller
public class HomeController {

    private final LearningPackageService packageService;

    public HomeController() {
        this.packageService = null;
    }

    @Autowired
    public HomeController(LearningPackageService packageService) {
        this.packageService = packageService;
    }

    @GetMapping("/")
    public String showHomePage(Model model) {
        if (packageService != null) {
            model.addAttribute("featuredPackages", packageService.listPackages()
                    .stream().filter(LearningPackage::isActive).limit(3).toList());
        }
        return "home/index";
    }
}
