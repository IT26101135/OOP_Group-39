/**
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages and shared UI
FILE     : PackageNavigationCard.java
WHAT IT DOES (simple words): Prepares the package choices shown in navigation.
REAL-LIFE EXAMPLE: A reception display shows available courses and their starting prices.
OOP USED : Encapsulation and abstraction
CRUD     : READ package choices
============================================================
*/
package com.drivingschool.learningpackage.model;

import java.math.BigDecimal;

// OOP: ENCAPSULATION - Pages read these choices through getters.
public class PackageNavigationCard {
    private final PackageType type;
    private LearningPackage cheapest;
    // Starts an empty course category, including categories coming soon.
    public PackageNavigationCard(PackageType type) { this.type = type; }
    // CRUD: READ   - finds or lists packages
    // Keeps only the cheapest active course seen in this category.
    public void consider(LearningPackage course) {
        if (course.isActive() && course.getType() == type
                && (cheapest == null || course.getPrice().compareTo(cheapest.getPrice()) < 0)) cheapest = course;
    }
    // Gives the filter its existing saved category name.
    public PackageType getType() { return type; }
    // Supplies a readable title without a second font or external asset.
    public String getTitle() {
        if (type == PackageType.AUTOMATIC) return "Automatic";
        if (type == PackageType.MOTORCYCLE) return "Motorcycle";
        if (type == PackageType.REFRESHER) return "Refresher";
        return "Manual";
    }
    // Explains the learning choice in one short sentence.
    public String getTagline() {
        if (type == PackageType.AUTOMATIC) return "Practise driving an automatic car.";
        if (type == PackageType.MOTORCYCLE) return "Build balance and road awareness.";
        if (type == PackageType.REFRESHER) return "Return to driving with confidence.";
        return "Practise clutch control and changing gears.";
    }
    // Distinguishes coming soon from a real priced offer.
    public boolean isAvailable() { return cheapest != null; }
    // Never invents a zero price for an unavailable category.
    public BigDecimal getPrice() { return cheapest == null ? null : cheapest.getPrice(); }
    // Links enrollment to the actual lowest-price course.
    public String getPackageId() { return cheapest == null ? null : cheapest.getId(); }
}
