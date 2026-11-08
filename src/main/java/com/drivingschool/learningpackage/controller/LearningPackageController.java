// Module owner: IT26101136
package com.drivingschool.learningpackage.controller;

import com.drivingschool.learningpackage.model.LearningPackage;
import com.drivingschool.learningpackage.service.LearningPackageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequestMapping("/packages")
public class LearningPackageController {

    private final LearningPackageService packageService;

    public LearningPackageController(LearningPackageService packageService) {
        this.packageService = packageService;
    }

    @GetMapping
    public String listActivePackages(Model model) {
        List<LearningPackage> activePackages = packageService.listPackages()
                .stream()
                .filter(LearningPackage::isActive)
                .toList();
        model.addAttribute("packages", activePackages);
        return "learningpackage/list";
    }

    @GetMapping("/{id}")
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
