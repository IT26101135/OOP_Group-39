/**
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages and shared UI
FILE     : PackageNavigationAdvice.java
WHAT IT DOES (simple words): Prepares the package choices shown in navigation.
REAL-LIFE EXAMPLE: A reception display shows available courses and their starting prices.
OOP USED : Encapsulation and abstraction
CRUD     : READ package choices
============================================================
*/
package com.drivingschool.learningpackage.controller;

import com.drivingschool.learningpackage.model.*;
import com.drivingschool.learningpackage.service.LearningPackageCatalog;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
// SOLID: S - One helper prepares navigation data once per page request.
public class PackageNavigationAdvice {
    private final LearningPackageCatalog catalog;
    // SOLID: D - The helper receives the existing catalog interface.
    // Receives the school's existing course catalog.
    public PackageNavigationAdvice(LearningPackageCatalog catalog) { this.catalog = catalog; }
    // CRUD: READ   - finds or lists packages
    // Builds four choices with one catalog read; clicks need no further requests.
    @ModelAttribute("packageNavigation")
    public List<PackageNavigationCard> choices() {
        List<PackageNavigationCard> cards = new ArrayList<>();
        List<LearningPackage> courses = catalog.listPackages();
        boolean available = false;
        // Step 1: Keep missing categories visible as Coming soon.
        for (PackageType type : PackageType.values()) {
            PackageNavigationCard card = new PackageNavigationCard(type);
            for (LearningPackage course : courses) card.consider(course);
            if (card.isAvailable()) available = true;
            cards.add(card);
        }
        // Step 2: An empty catalog uses the normal Packages link alone.
        if (!available) cards.clear();
        return cards;
    }
}
