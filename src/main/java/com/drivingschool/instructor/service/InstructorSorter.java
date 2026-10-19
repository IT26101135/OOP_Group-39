
package com.drivingschool.instructor.service;

import com.drivingschool.instructor.model.Instructor;

import java.util.ArrayList;
import java.util.List;

public class InstructorSorter {

    public List<Instructor> sortByExperience(
            List<Instructor> instructors) {

        if (instructors == null) {
            throw new IllegalArgumentException(
                    "Instructor list cannot be null.");
        }

        List<Instructor> sorted =
                new ArrayList<>(instructors);

        int n = sorted.size();

        for (int i = 0; i < n - 1; i++) {

            boolean swapped = false;

            for (int j = 0; j < n - i - 1; j++) {

                Instructor current = sorted.get(j);
                Instructor next = sorted.get(j + 1);

                if (current.getYearsOfExperience()
                        < next.getYearsOfExperience()) {

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
}
