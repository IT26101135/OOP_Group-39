/**
 * ============================================================
 * MEMBER   : IT26101133 - [Name]
 * MODULE   : Instructors and bubble sort
 * FILE     : InstructorScheduleServiceTest.java
 * WHAT IT DOES (simple words): Checks schedule edits and history using temporary files.
 * REAL-LIFE EXAMPLE: Practise with spare diary pages before changing real schedules.
 * OOP USED : Encapsulation, abstraction and exception handling
 * CRUD     : CREATE, READ, UPDATE, DELETE
 * ============================================================
 */
package com.drivingschool.instructor.service;

import com.drivingschool.auth.model.InstructorAccount;
import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.common.exception.*;
import com.drivingschool.instructor.model.Availability;
import com.drivingschool.instructor.model.InstructorLeave;
import com.drivingschool.instructor.repository.AvailabilityRepository;
import com.drivingschool.instructor.repository.InstructorLeaveRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import java.nio.file.Path;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InstructorScheduleServiceTest {
    @TempDir Path directory;
    private AvailabilityRepository hours;
    private InstructorLeaveRepository leaves;
    private BookingRepository bookings;
    private AuthSessionService auth;
    private InstructorScheduleService service;
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final Clock clock = Clock.fixed(Instant.parse("2030-01-01T08:00:00Z"), ZoneOffset.UTC);

    // Uses a fixed school date and spare files, so the date rules are repeatable.
    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        hours = new AvailabilityRepository(directory.toString(), mapper);
        leaves = new InstructorLeaveRepository(directory.toString(), mapper);
        bookings = new BookingRepository(directory.toString(), mapper);
        auth = mock(AuthSessionService.class);
        when(auth.requireLogin(request)).thenReturn(new InstructorAccount("I001", "Teacher One",
                "teacher@example.test", "$2a$10$" + "A".repeat(53)));
        service = new InstructorScheduleService(hours, leaves, bookings, auth, clock);
    }

    // Adds, reads, edits and removes one owned block without changing its identity.
    @Test
    void supportsAvailabilityCrud() {
        Availability created = service.addAvailability(request, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(12, 0));
        assertEquals(1, service.availability(request).size());
        service.updateAvailability(request, created.getId(), DayOfWeek.TUESDAY,
                LocalTime.of(10, 0), LocalTime.of(13, 0));
        assertEquals(DayOfWeek.TUESDAY, hours.findByIdStrict(created.getId()).orElseThrow().getDayOfWeek());
        service.deleteAvailability(request, created.getId());
        assertTrue(service.availability(request).isEmpty());
    }

    // An overlapping edit must leave the previously saved hours unchanged.
    @Test
    void rejectsOverlappingUpdate() {
        service.addAvailability(request, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0));
        Availability later = service.addAvailability(request, DayOfWeek.MONDAY,
                LocalTime.of(13, 0), LocalTime.of(15, 0));
        assertThrows(DuplicateRecordException.class, () -> service.updateAvailability(request,
                later.getId(), DayOfWeek.MONDAY, LocalTime.of(11, 0), LocalTime.of(14, 0)));
        assertEquals(LocalTime.of(13, 0), hours.findByIdStrict(later.getId()).orElseThrow().getStartTime());
    }

    // A confirmed student promise must be preserved when working hours are edited.
    @Test
    void protectsConfirmedLessonsDuringHoursEdit() {
        Availability block = service.addAvailability(request, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(12, 0));
        saveConfirmedLesson(LocalDate.of(2030, 1, 7));
        assertThrows(ValidationException.class, () -> service.updateAvailability(request,
                block.getId(), DayOfWeek.MONDAY, LocalTime.of(14, 0), LocalTime.of(17, 0)));
        assertEquals(LocalTime.of(9, 0), hours.findByIdStrict(block.getId()).orElseThrow().getStartTime());
    }

    // A day-off record keeps its ID when the instructor moves it to another free day.
    @Test
    void supportsLeaveCrud() {
        InstructorLeave leave = service.addLeave(request, LocalDate.of(2030, 1, 2), "Appointment");
        assertEquals(1, service.leaves(request).size());
        service.updateLeave(request, leave.getId(), LocalDate.of(2030, 1, 3), "Family appointment");
        assertEquals("Family appointment", leaves.findByIdStrict(leave.getId()).orElseThrow().getReason());
        service.deleteLeave(request, leave.getId());
        assertTrue(service.leaves(request).isEmpty());
    }

    // Yesterday's valid record must still be readable after the calendar moves forward.
    @Test
    void readsPastLeaveButRejectsNewPastRequest() {
        leaves.save(new InstructorLeave("L001", "I001", LocalDate.of(1999, 1, 1), "Historical leave"));
        assertEquals(LocalDate.of(1999, 1, 1), service.leaves(request).get(0).getDate());
        assertThrows(ValidationException.class, () -> service.addLeave(request,
                LocalDate.of(2029, 12, 31), "New past request"));
    }

    // Moving leave onto another leave day or a promised lesson is refused.
    @Test
    void rejectsDuplicateAndConfirmedLeaveDates() {
        InstructorLeave first = service.addLeave(request, LocalDate.of(2030, 1, 2), "First day");
        service.addLeave(request, LocalDate.of(2030, 1, 3), "Second day");
        assertThrows(DuplicateRecordException.class, () -> service.updateLeave(request,
                first.getId(), LocalDate.of(2030, 1, 3), "Conflicting day"));
        saveConfirmedLesson(LocalDate.of(2030, 1, 7));
        assertThrows(ValidationException.class, () -> service.addLeave(request,
                LocalDate.of(2030, 1, 7), "Would cancel a promise"));
    }

    // Changing an ID in the browser must not allow another instructor's hours to be edited.
    @Test
    void rejectsAnotherOwnersBlockAndMissingRecord() {
        hours.save(new Availability("H001", "I002", DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(12, 0)));
        assertEquals(403, assertThrows(ResponseStatusException.class, () -> service.updateAvailability(
                request, "H001", DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(12, 0))).getStatusCode().value());
        assertThrows(RecordNotFoundException.class, () -> service.deleteLeave(request, "L999"));
        assertTrue(hours.findByIdStrict("H001").isPresent());
    }

    // Student accounts cannot use the instructor schedule service.
    @Test
    void rejectsWrongRole() {
        User student = mock(User.class);
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(auth.requireLogin(request)).thenReturn(student);
        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> service.availability(request)).getStatusCode().value());
    }

    // Saves a real confirmed lesson so safeguards are checked against file data.
    private void saveConfirmedLesson(LocalDate date) {
        LocalDateTime start = date.atTime(10, 0);
        bookings.save(new Booking(bookings.nextId("B"), "S001", "E001", start,
                start.plusHours(1), "I001", "V001", BookingStatus.CONFIRMED, LocalDateTime.now(clock)));
    }
}
