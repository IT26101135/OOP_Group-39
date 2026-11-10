/**
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages and enrollment
FILE     : LearningPackageController.java
WHAT IT DOES (simple words): Prepares pages and accepts forms for learning package.
REAL-LIFE EXAMPLE: A course folder keeps the learning plan and enrollment details together.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.learningpackage.controller;

import com.drivingschool.learningpackage.model.LearningPackage;
import com.drivingschool.learningpackage.service.LearningPackageCatalog;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/packages")
// SOLID: S - This class is responsible for web requests and pages.
public class LearningPackageController {

    // SOLID: D - The controller uses the catalog contract received in its constructor.
    private final LearningPackageCatalog packageService;

    // Receives the helpers this class needs so each job stays separate.
    public LearningPackageController(LearningPackageCatalog packageService) {
        this.packageService = packageService;
    }

    @GetMapping
    // Handles list active packages for this class without changing another class's job.
    // CRUD: READ   - finds or lists packages
    public String listActivePackages(Model model) {
        List<LearningPackage> activePackages = new ArrayList<>();
        for (LearningPackage learningPackage : packageService.listPackages()) {
            if (learningPackage.isActive()) activePackages.add(learningPackage);
        }
        model.addAttribute("packages", activePackages);
        return "learningpackage/list";
    }

    @GetMapping("/{id}")
    // Handles details for this class without changing another class's job.
    // CRUD: READ   - finds or lists packages
    public String details(@PathVariable String id, Model model) {
        LearningPackage selected = packageService.findPackageById(id)
                .orElseThrow(() -> new com.drivingschool.common.exception.RecordNotFoundException(
                        "Package not found."));
        if (!selected.isActive()) {
            throw new com.drivingschool.common.exception.RecordNotFoundException(
                    "Package not found.");
        }
        model.addAttribute("learningPackage", selected);
        return "learningpackage/detail";
    }
}
