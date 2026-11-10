/**
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages and shared UI
FILE     : PackageNavigationAdviceTest.java
WHAT IT DOES (simple words): Checks navigation prices without changing saved packages.
REAL-LIFE EXAMPLE: Checks the reception price display against the course list.
OOP USED : Abstraction through the catalog interface
CRUD     : READ packages
============================================================
*/
package com.drivingschool.learningpackage.service;

import com.drivingschool.learningpackage.controller.PackageNavigationAdvice;
import com.drivingschool.learningpackage.model.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PackageNavigationAdviceTest {
    // The display uses the cheapest active course in each separate category.
    @Test void lowestPricePerType() {
        LearningPackageCatalog catalog = mock(LearningPackageCatalog.class);
        when(catalog.listPackages()).thenReturn(List.of(course("P001", "2000", true, PackageType.MANUAL),
                course("P002", "1000", true, PackageType.MANUAL), course("P003", "3000", true, PackageType.AUTOMATIC)));
        List<PackageNavigationCard> cards = new PackageNavigationAdvice(catalog).choices();
        assertEquals(new BigDecimal("1000.00"), cards.get(0).getPrice());
        assertEquals("P002", cards.get(0).getPackageId());
        assertEquals(new BigDecimal("3000.00"), cards.get(1).getPrice());
        verify(catalog, times(1)).listPackages();
    }
    // Missing categories stay visible, with no invented zero price.
    @Test void missingTypeIsComingSoon() {
        LearningPackageCatalog catalog = mock(LearningPackageCatalog.class);
        when(catalog.listPackages()).thenReturn(List.of(course("P001", "1000", true, PackageType.MANUAL)));
        List<PackageNavigationCard> cards = new PackageNavigationAdvice(catalog).choices();
        assertEquals(4, cards.size());
        assertFalse(cards.get(1).isAvailable());
        assertNull(cards.get(1).getPrice());
        assertNull(cards.get(1).getPackageId());
    }
    // No active courses means a normal Packages link without a panel.
    @Test void noPackagesMeansNoPanel() {
        LearningPackageCatalog catalog = mock(LearningPackageCatalog.class);
        when(catalog.listPackages()).thenReturn(List.of());
        assertTrue(new PackageNavigationAdvice(catalog).choices().isEmpty());
    }
    // A cheaper inactive course must never lower the displayed offer.
    @Test void inactivePackagesAreIgnored() {
        LearningPackageCatalog catalog = mock(LearningPackageCatalog.class);
        when(catalog.listPackages()).thenReturn(List.of(course("P001", "100", false, PackageType.MANUAL), course("P002", "1000", true, PackageType.MANUAL)));
        assertEquals(new BigDecimal("1000.00"), new PackageNavigationAdvice(catalog).choices().get(0).getPrice());
        when(catalog.listPackages()).thenReturn(List.of(course("P001", "100", false, PackageType.MANUAL)));
        assertTrue(new PackageNavigationAdvice(catalog).choices().isEmpty());
    }
    // Supplies a small course record for the price-display checks.
    private LearningPackage course(String id, String price, boolean active, PackageType type) {
        LearningPackage course = new LearningPackage(id, "Practice course", "Practical driving lessons", new BigDecimal(price), 5, 30, active);
        course.setType(type);
        return course;
    }
}
