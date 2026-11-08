/**
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages and shared UI
FILE     : PackageEnrollmentEntryController.java
WHAT IT DOES (simple words): Prepares the package choices shown in navigation.
REAL-LIFE EXAMPLE: A reception display shows available courses and their starting prices.
OOP USED : Encapsulation and abstraction
CRUD     : READ package choices
============================================================
*/
package com.drivingschool.learningpackage.controller;

import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.learningpackage.service.LearningPackageCatalog;
import com.drivingschool.learningpackage.model.LearningPackage;
import com.drivingschool.common.exception.RecordNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
// SOLID: S - This entry point remembers only a safe package destination before login.
public class PackageEnrollmentEntryController {
    private final AuthSessionService auth;
    private final LearningPackageCatalog catalog;
    // Receives existing authentication and catalog helpers.
    public PackageEnrollmentEntryController(AuthSessionService auth, LearningPackageCatalog catalog) {
        this.auth = auth;
        this.catalog = catalog;
    }
    // CRUD: READ   - finds or lists packages
    // Sends visitors through login, then back to this active package's existing form.
    @GetMapping("/packages/{id}/start")
    public String start(@PathVariable String id, HttpServletRequest request) {
        if (!id.matches("P(?!000)[0-9]{3}")) throw new RecordNotFoundException("Package not found.");
        LearningPackage course = catalog.findPackageById(id)
                .orElseThrow(() -> new RecordNotFoundException("Package not found."));
        if (!course.isActive()) throw new RecordNotFoundException("Package not found.");
        String destination = "/packages/" + id;
        if (auth.getCurrentUser(request).isPresent()) return "redirect:" + destination;
        request.getSession(true).setAttribute("packageLoginReturn", destination);
        return "redirect:/login";
    }
}
