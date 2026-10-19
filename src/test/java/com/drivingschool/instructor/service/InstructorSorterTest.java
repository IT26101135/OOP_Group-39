
package com.drivingschool.instructor.service;

import com.drivingschool.instructor.model.Instructor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InstructorSorterTest {

    private final InstructorSorter sorter = new InstructorSorter();

    private Instructor instructor(String id, int years) {
        return new Instructor(
                id,
                "Kumar Silva",
                "kumar@example.com",
                "0771234567",
                years,
                true
        );
    }

    @Test
    void shouldSortEmptyList() {
        assertTrue(sorter.sortByExperience(List.of()).isEmpty());
    }

    @Test
    void shouldHandleSingleInstructor() {
        Instructor a = instructor("I001", 5);

        assertEquals(List.of(a),
                sorter.sortByExperience(List.of(a)));
    }

    @Test
    void shouldKeepAlreadySortedList() {
        Instructor a = instructor("I001", 10);
        Instructor b = instructor("I002", 5);
        Instructor c = instructor("I003", 2);

        assertEquals(List.of(a, b, c),
                sorter.sortByExperience(List.of(a, b, c)));
    }

    @Test
    void shouldSortReverseOrder() {
        Instructor a = instructor("I001", 2);
        Instructor b = instructor("I002", 5);
        Instructor c = instructor("I003", 10);

        assertEquals(List.of(c, b, a),
                sorter.sortByExperience(List.of(a, b, c)));
    }

    @Test
    void shouldSortMixedExperience() {
        Instructor a = instructor("I001", 3);
        Instructor b = instructor("I002", 8);
        Instructor c = instructor("I003", 5);

        assertEquals(List.of(b, c, a),
                sorter.sortByExperience(List.of(a, b, c)));
    }

    @Test
    void shouldPreserveEqualExperienceOrder() {
        Instructor a = instructor("I001", 8);
        Instructor b = instructor("I002", 3);
        Instructor c = instructor("I003", 8);

        assertEquals(List.of(a, c, b),
                sorter.sortByExperience(List.of(a, b, c)));
    }

    @Test
    void shouldNotModifyOriginalList() {
        Instructor a = instructor("I001", 2);
        Instructor b = instructor("I002", 10);

        List<Instructor> original =
                new ArrayList<>(List.of(a, b));

        List<Instructor> sorted =
                sorter.sortByExperience(original);

        assertEquals(List.of(a, b), original);
        assertEquals(List.of(b, a), sorted);
        assertNotSame(original, sorted);
    }
}
