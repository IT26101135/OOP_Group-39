/**
============================================================
MEMBER   : IT26101133 - [Name]
MODULE   : Instructors and bubble sort
FILE     : InstructorAccountService.java
WHAT IT DOES (simple words): Applies the instructor account rules.
REAL-LIFE EXAMPLE: A teacher diary keeps teaching hours, lesson notes and profile details organised.
OOP USED : Encapsulation (private fields and public methods)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/
package com.drivingschool.instructor.service;

import com.drivingschool.auth.model.InstructorAccount;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.util.AccountWriteCoordinator;
import com.drivingschool.auth.util.PasswordHasher;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.instructor.model.Instructor;
import org.springframework.stereotype.Service;

import java.util.Locale;

/** Creates the instructor profile and its matching login identity together. */
@Service
// SOLID: S - This class is responsible for application rules.
public class InstructorAccountService {

    private final InstructorService instructorService;
    private final UserRepository users;
    private final PasswordHasher passwordHasher;
    private final BookingRepository bookings;

    // Receives the helpers or validates the details needed to create this object.
    public InstructorAccountService(InstructorService instructorService,
                                    UserRepository users,
                                    PasswordHasher passwordHasher,
                                    BookingRepository bookings) {
        this.instructorService = instructorService;
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.bookings = bookings;
    }

    // Creates the teaching profile and matching login account together.
    // CRUD: CREATE - adds a new instructor profile and saves it
    public Instructor create(String name, String email, String phone,
                             int yearsOfExperience, String rawPassword) {
        return create(name, email, phone, yearsOfExperience, rawPassword,
                null, null, null, null, null);
    }

    // Creates the teaching profile and matching login account together.
    // CRUD: CREATE - adds a new instructor profile and saves it
    public Instructor create(String name, String email, String phone,
                             int yearsOfExperience, String rawPassword,
                             String bio, String licenseNumber,
                             String qualifications, String photoPath,
                             java.util.List<String> vehicleTypes) {
        return AccountWriteCoordinator.execute(() -> {
            String normalizedEmail = normalizeEmail(email);
            ensureAccountEmailAvailable(normalizedEmail, null);

            Instructor profile = instructorService.createInstructor(
                    name, normalizedEmail, phone, yearsOfExperience,
                    bio, licenseNumber, qualifications, photoPath, vehicleTypes);
            try {
                users.save(new InstructorAccount(profile.getId(), profile.getName(),
                        profile.getEmail(), passwordHasher.hash(rawPassword)));
                return profile;
            } catch (RuntimeException failure) {
                // Avoid leaving an instructor profile without its matching login.
                try {
                    instructorService.deleteInstructor(profile.getId());
                } catch (RuntimeException rollbackFailure) {
                    failure.addSuppressed(rollbackFailure);
                }
                throw failure;
            }
        });
    }

    // Replaces the selected record while keeping its permanent ID.
    // CRUD: UPDATE - changes an existing instructor profile
    public Instructor update(String id, String name, String email, String phone,
                             int yearsOfExperience) {
        return update(id, name, email, phone, yearsOfExperience,
                null, null, null, null, java.util.List.of());
    }

    // Replaces the selected record while keeping its permanent ID.
    // CRUD: UPDATE - changes an existing instructor profile
    public Instructor update(String id, String name, String email, String phone,
                             int yearsOfExperience, String bio,
                             String licenseNumber, String qualifications,
                             String photoPath, java.util.List<String> vehicleTypes) {
        return AccountWriteCoordinator.execute(() -> {
            String normalizedEmail = normalizeEmail(email);
            ensureAccountEmailAvailable(normalizedEmail, id);
            User stored = users.findByIdStrict(id).orElseThrow(() ->
                    new IllegalStateException("Instructor login account is missing."));
            if (!(stored instanceof InstructorAccount account)) {
                throw new IllegalStateException("Stored account is not an instructor.");
            }
            Instructor profile = instructorService.updateInstructor(
                    id, name, normalizedEmail, phone, yearsOfExperience,
                    bio, licenseNumber, qualifications, photoPath, vehicleTypes);
            users.update(new InstructorAccount(id, profile.getName(),
                    profile.getEmail(), account.getPasswordHash()));
            return profile;
        });
    }

    // Removes a record only after the service has checked its rules.
    // CRUD: DELETE - removes a instructor profile
    public void delete(String id) {
        AccountWriteCoordinator.execute(() -> {
            instructorService.getInstructorById(id);
            User stored = users.findByIdStrict(id).orElseThrow(() ->
                    new IllegalStateException("Instructor login account is missing."));
            if (!(stored instanceof InstructorAccount)) {
                throw new IllegalStateException("Stored account is not an instructor.");
            }
            // Keep both assigned lessons and pending preferences linked to a real profile.
            for (com.drivingschool.booking.model.Booking booking : bookings.readAllStrict()) {
                if (id.equals(booking.getInstructorId()) || id.equals(booking.getRequestedInstructorId())) {
                    throw new ValidationException("Keep this instructor because they have lesson history. You can mark them inactive instead.");
                }
            }
            instructorService.deleteInstructor(id);
            users.delete(id);
            return null;
        });
    }
    // Rejects a missing email and stores it consistently for duplicate checks.
    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) throw new ValidationException("Instructor email is required.");
        return email.trim().toLowerCase(Locale.ROOT);
    }

    // Prevents an instructor from taking another account's email address.
    private void ensureAccountEmailAvailable(String email, String excludedId) {
        for (User account : users.readAllStrict()) {
            if (!account.getId().equals(excludedId) && account.getEmail().equalsIgnoreCase(email)) {
                throw new DuplicateRecordException("An account with this email already exists.");
            }
        }
    }
}
