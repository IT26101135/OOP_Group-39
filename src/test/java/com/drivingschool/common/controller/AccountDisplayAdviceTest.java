// Module owner: IT26101135 - Account display names and missing-record fallbacks.
package com.drivingschool.common.controller;

import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.instructor.model.Instructor;
import com.drivingschool.instructor.repository.InstructorRepository;
import com.drivingschool.learningpackage.repository.EnrollmentRepository;
import com.drivingschool.learningpackage.repository.LearningPackageRepository;
import com.drivingschool.vehicle.repository.VehicleRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountDisplayAdviceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final InstructorRepository instructors = mock(InstructorRepository.class);
    private final LearningPackageRepository packages = mock(LearningPackageRepository.class);
    private final VehicleRepository vehicles = mock(VehicleRepository.class);
    private final BookingRepository bookings = mock(BookingRepository.class);
    private final EnrollmentRepository enrollments = mock(EnrollmentRepository.class);

    @Test
    void namesAreLoadedOnlyWhenUsedAndSharedAcrossCardsWithinTheRequest() {
        var advice = new AccountDisplayAdvice(users, instructors, packages, vehicles, bookings, enrollments);
        var names = advice.display();
        verifyNoInteractions(users, instructors, packages, vehicles, bookings, enrollments);
        when(users.readAllStrict()).thenReturn(List.of());
        when(instructors.readAllStrict()).thenReturn(List.of(
                new Instructor("I001", "Ruwini Perera", "ruwini@example.test", "0770000000", 5, false)));
        assertEquals("Ruwini Perera", names.person("I001"));
        assertEquals("Ruwini Perera", names.reportLabel("I001 - Old name"));
        verify(users, times(1)).readAllStrict();
        verify(instructors, times(1)).readAllStrict();
        verifyNoInteractions(packages, vehicles, bookings, enrollments);
    }

    @Test
    void missingReferencesHaveUsefulLabelsRatherThanInternalIdentifiers() {
        var names = new AccountDisplayAdvice(users, instructors, packages, vehicles, bookings, enrollments).display();
        assertEquals("Not assigned", names.person(null));
        assertEquals("Name unavailable", names.person("S999"));
        assertEquals("No active package", names.course(null));
        assertEquals("Package no longer listed", names.course("P999"));
        assertEquals("Vehicle no longer listed", names.vehicle("V999"));
        assertEquals("Lesson date unavailable", names.lesson("B999"));
        assertEquals("Package no longer listed", names.enrollment("E999"));
    }

    @Test
    void formatsDisplayValuesWithoutChangingFormValues() {
        var display = new SchoolPresentationAdvice.PublicDisplay();
        assertEquals("12,500", display.money(new BigDecimal("12500.00")));
        assertEquals("12,500.5", display.money(new BigDecimal("12500.50")));
        assertEquals("16 Nov 2026", display.date(LocalDate.of(2026, 11, 16)));
        assertEquals("16 Nov 2026", display.date("2026-11-16"));
        assertEquals("Not recorded", display.date(null));
        assertEquals("9:00 am", display.time(LocalTime.of(9, 0)));
        assertEquals("5:40 pm", display.time(LocalTime.of(17, 40)));
        assertEquals("No-show", display.label("NO_SHOW"));
        assertEquals("Monday", display.label("MONDAY"));
        assertEquals("Manual car", display.label("MANUAL"));
    }
}
