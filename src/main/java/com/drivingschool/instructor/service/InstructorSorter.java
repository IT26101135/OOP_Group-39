// Module owner: IT26101133

package com.drivingschool.instructor.service;

import com.drivingschool.instructor.model.Instructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InstructorSorter {

    public List<Instructor> sortByExperience(
            List<Instructor> instructors) {

        return sort(instructors, "experience", false);
    }

    /** Manual bubble sort with an early exit after any already ordered pass. */
    public List<Instructor> sort(List<Instructor> instructors,
                                 String sortBy,
                                 boolean ascending) {

        if (instructors == null) {
            throw new IllegalArgumentException(
                    "Instructor list cannot be null.");
        }

        List<Instructor> sorted =
                new ArrayList<>(instructors);

        String key = sortBy == null ? "name" : sortBy.toLowerCase(Locale.ROOT);
        if (!List.of("name", "experience", "rating").contains(key)) key = "name";

        int n = sorted.size();

        for (int i = 0; i < n - 1; i++) {

            boolean swapped = false;

            for (int j = 0; j < n - i - 1; j++) {

                Instructor current = sorted.get(j);
                Instructor next = sorted.get(j + 1);

                int comparison = compare(current, next, key);
                boolean shouldSwap = ascending
                        ? comparison > 0
                        : comparison < 0;
                if (shouldSwap) {

                    sorted.set(j, next);
                    sorted.set(j + 1, current);

                    swapped = true;
                }
            }

            if (!swapped) {
                break;
            }
        }

        return sorted;
    }

    private int compare(Instructor first, Instructor second, String key) {
        return switch (key) {
            case "experience" -> Integer.compare(first.getYearsOfExperience(),
                    second.getYearsOfExperience());
            case "rating" -> Double.compare(first.getAverageRating(),
                    second.getAverageRating());
            default -> first.getName().compareToIgnoreCase(second.getName());
        };
    }
}
