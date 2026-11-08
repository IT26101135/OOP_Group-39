/**
 * ============================================================
 * MEMBER   : IT26101133 - [Name]
 * MODULE   : Instructors and bubble sort
 * FILE     : InstructorAccountServiceTest.java
 * WHAT IT DOES (simple words): Checks profile/account CRUD and protection of requested instructors.
 * REAL-LIFE EXAMPLE: Practise with spare diary pages before changing real schedules.
 * OOP USED : Encapsulation, abstraction and exception handling
 * CRUD     : CREATE, READ, UPDATE, DELETE
 * ============================================================
 */
package com.drivingschool.instructor.service;

import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.util.PasswordHasher;
import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.instructor.model.Instructor;
import com.drivingschool.instructor.repository.InstructorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InstructorAccountServiceTest {
    @TempDir Path directory;
    private InstructorService profiles;
    private InstructorAccountService service;
    private UserRepository users;
    private BookingRepository bookings;

    // Uses spare files and a synthetic valid-format hash, never a real credential.
    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        profiles = new InstructorService(new InstructorRepository(directory, mapper));
        users = new UserRepository(directory.toString(), mapper);
        bookings = new BookingRepository(directory.toString(), mapper);
        PasswordHasher passwords = mock(PasswordHasher.class);
        when(passwords.hash("ExamplePass123!")).thenReturn("$2a$10$" + "A".repeat(53));
        service = new InstructorAccountService(profiles, users, passwords, bookings);
    }

    // Profile and login identity must agree after creation, editing and deletion.
    @Test
    void supportsProfileAndAccountCrud() {
        Instructor created = service.create("Teacher One", "teacher@example.test", "0771234567", 5,
                "ExamplePass123!", "Patient teacher", "DL123", "Certified", null, List.of("Manual"));
        assertEquals(created.getId(), profiles.getAllInstructors().get(0).getId());
        service.update(created.getId(), "Teacher Updated", "updated@example.test", "0771234567", 6,
                "Updated bio", "DL123", "Certified", null, List.of("Manual"));
        assertEquals("Teacher Updated", users.findByIdStrict(created.getId()).orElseThrow().getName());
        assertEquals("Updated bio", profiles.getInstructorById(created.getId()).getBio());
        service.delete(created.getId());
        assertTrue(profiles.getAllInstructors().isEmpty());
        assertTrue(users.findByIdStrict(created.getId()).isEmpty());
    }

    // A queued student preference must remain attached to an existing instructor.
    @Test
    void protectsPendingRequestedInstructor() {
        Instructor created = service.create("Teacher One", "teacher@example.test", "0771234567", 5, "ExamplePass123!");
        LocalDateTime start = LocalDateTime.of(2030, 1, 7, 10, 0);
        Booking pending = new Booking("B001", "S001", "E001", start, start.plusHours(1),
                null, null, BookingStatus.PENDING, start.minusDays(3));
        pending.setRequestedInstructorId(created.getId());
        bookings.save(pending);
        assertThrows(ValidationException.class, () -> service.delete(created.getId()));
        assertTrue(users.findByIdStrict(created.getId()).isPresent());
        assertEquals(1, profiles.getAllInstructors().size());
    }

    // Missing emails should produce a friendly validation error before any file changes.
    @Test
    void rejectsMissingEmailBeforeCreatingRecords() {
        assertThrows(ValidationException.class, () -> service.create("Teacher One", null,
                "0771234567", 5, "ExamplePass123!"));
        assertTrue(profiles.getAllInstructors().isEmpty());
        assertTrue(users.readAllStrict().isEmpty());
    }
}
