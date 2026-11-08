// Module owner: IT26101133
package com.drivingschool.instructor.service;

import com.drivingschool.auth.model.InstructorAccount;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.util.AccountWriteCoordinator;
import com.drivingschool.auth.util.PasswordHasher;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.instructor.model.Instructor;
import org.springframework.stereotype.Service;

import java.util.Locale;

/** Creates the instructor profile and its matching login identity together. */
@Service
public class InstructorAccountService {

    private final InstructorService instructorService;
    private final UserRepository users;
    private final PasswordHasher passwordHasher;
    private final BookingRepository bookings;

    public InstructorAccountService(InstructorService instructorService,
                                    UserRepository users,
                                    PasswordHasher passwordHasher,
                                    BookingRepository bookings) {
        this.instructorService = instructorService;
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.bookings = bookings;
    }

    public Instructor create(String name, String email, String phone,
                             int yearsOfExperience, String rawPassword) {
        return create(name, email, phone, yearsOfExperience, rawPassword,
                null, null, null, null, null);
    }

    public Instructor create(String name, String email, String phone,
                             int yearsOfExperience, String rawPassword,
                             String bio, String licenseNumber,
                             String qualifications, String photoPath,
                             java.util.List<String> vehicleTypes) {
        return AccountWriteCoordinator.execute(() -> {
            String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
            if (users.readAllStrict().stream()
                    .anyMatch(user -> user.getEmail().equalsIgnoreCase(normalizedEmail))) {
                throw new DuplicateRecordException("An account with this email already exists.");
            }

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

    public Instructor update(String id, String name, String email, String phone,
                             int yearsOfExperience) {
        return update(id, name, email, phone, yearsOfExperience,
                null, null, null, null, java.util.List.of());
    }

    public Instructor update(String id, String name, String email, String phone,
                             int yearsOfExperience, String bio,
                             String licenseNumber, String qualifications,
                             String photoPath, java.util.List<String> vehicleTypes) {
        return AccountWriteCoordinator.execute(() -> {
            String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
            if (users.readAllStrict().stream()
                    .anyMatch(user -> !user.getId().equals(id)
                            && user.getEmail().equalsIgnoreCase(normalizedEmail))) {
                throw new DuplicateRecordException("An account with this email already exists.");
            }
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

    public void delete(String id) {
        AccountWriteCoordinator.execute(() -> {
            instructorService.getInstructorById(id);
            User stored = users.findByIdStrict(id).orElseThrow(() ->
                    new IllegalStateException("Instructor login account is missing."));
            if (!(stored instanceof InstructorAccount)) {
                throw new IllegalStateException("Stored account is not an instructor.");
            }
            boolean hasBookings = bookings.readAllStrict().stream()
                    .anyMatch(booking -> id.equals(booking.getInstructorId()));
            if (hasBookings) {
                throw new com.drivingschool.common.exception.ValidationException(
                        "Instructor cannot be deleted while lesson history is attached.");
            }
            instructorService.deleteInstructor(id);
            users.delete(id);
            return null;
        });
    }
}
