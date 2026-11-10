/**
============================================================
MEMBER   : IT26101133 - [Name]
MODULE   : Instructors and bubble sort
FILE     : InstructorSorter.java
WHAT IT DOES (simple words): Applies the instructor sorter rules.
REAL-LIFE EXAMPLE: A teacher diary keeps teaching hours, lesson notes and profile details organised.
OOP USED : Encapsulation (private fields and public methods)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/
package com.drivingschool.instructor.service;

import com.drivingschool.instructor.model.Instructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// SOLID: S - This class only orders a copy of the instructor list.
// SOLID: O - Another InstructorOrder can supply a new comparison without rewriting bubble sort.
public class InstructorSorter {
    // Lists the most experienced instructors first using the same bubble sort.
    public List<Instructor> sortByExperience(List<Instructor> instructors) {
        return sort(instructors, "experience", false);
    }

    // Orders a copy with hand-written bubble-sort passes so the original list stays unchanged.
    public List<Instructor> sort(List<Instructor> instructors, String sortBy, boolean ascending) {
        // Step 1: Keep the caller's original list safe by sorting a copy.
        if (instructors == null) throw new IllegalArgumentException("Instructor list cannot be null.");
        List<Instructor> sorted = new ArrayList<>(instructors);
        InstructorOrder order = chooseOrder(sortBy);
        // Step 2: Each pass moves one out-of-place card toward the end.
        for (int pass = 0; pass < sorted.size() - 1; pass++) {
            boolean swapped = swapNeighbours(sorted, sorted.size() - pass - 1, order, ascending);
            // Step 3: Stop early when a whole pass needed no swaps.
            if (!swapped) break;
        }
        return sorted;
    }

    // Swaps only adjacent cards that are out of order; equal cards keep their order.
    private boolean swapNeighbours(List<Instructor> sorted, int limit,
                                    InstructorOrder order, boolean ascending) {
        boolean swapped = false;
        for (int index = 0; index < limit; index++) {
            Instructor current = sorted.get(index);
            Instructor next = sorted.get(index + 1);
            // OOP: POLYMORPHISM - Name, experience and rating orders answer differently here.
            int comparison = order.compare(current, next);
            boolean outOfOrder = comparison > 0;
            if (!ascending) outOfOrder = comparison < 0;
            if (outOfOrder) {
                sorted.set(index, next);
                sorted.set(index + 1, current);
                swapped = true;
            }
        }
        return swapped;
    }

    // Chooses which instructor detail the bubble sort compares.
    private InstructorOrder chooseOrder(String sortBy) {
        String key = sortBy == null ? "name" : sortBy.toLowerCase(Locale.ROOT);
        if ("experience".equals(key)) return new InstructorExperienceOrder();
        if ("rating".equals(key)) return new InstructorRatingOrder();
        return new InstructorNameOrder();
    }
}
