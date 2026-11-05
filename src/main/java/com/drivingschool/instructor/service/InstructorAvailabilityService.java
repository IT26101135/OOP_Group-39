package com.drivingschool.instructor.service;

import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.instructor.model.Instructor;
import com.drivingschool.instructor.model.InstructorAvailability;
import com.drivingschool.instructor.repository.InstructorAvailabilityRepository;
import com.drivingschool.instructor.repository.InstructorRepository;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.Objects;

public class InstructorAvailabilityService {

    private static final Object AVAILABILITY_WRITE_LOCK = new Object();

    private final InstructorRepository instructorRepository;
    private final InstructorAvailabilityRepository availabilityRepository;
    private final AuthSessionService authSessionService;

    public InstructorAvailabilityService(
            InstructorRepository instructorRepository,
            InstructorAvailabilityRepository availabilityRepository,
            AuthSessionService authSessionService) {

        this.instructorRepository = Objects.requireNonNull(instructorRepository);
        this.availabilityRepository = Objects.requireNonNull(availabilityRepository);
        this.authSessionService = Objects.requireNonNull(authSessionService);
    }

    public InstructorAvailability addAvailability(
            HttpServletRequest request,
            String instructorId,
            LocalDateTime start,
            LocalDateTime end) {

        authSessionService.requireAdmin(request);

        if (instructorId == null
                || !instructorId.matches("I(?!000)[0-9]{3}")) {
            throw new ValidationException(
                    "Instructor ID must be between I001 and I999.");
        }

        if (start == null || end == null || !start.isBefore(end)) {
            throw new ValidationException(
                    "Start time must be before end time.");
        }

        synchronized (AVAILABILITY_WRITE_LOCK) {

            Instructor instructor = instructorRepository
                    .findByIdStrict(instructorId)
                    .orElseThrow(() -> new RecordNotFoundException(
                            "Instructor not found: " + instructorId));

            if (!instructor.isActive()) {
                throw new ValidationException(
                        "Cannot add availability for an inactive instructor.");
            }

            availabilityRepository.readAllStrict();

            String availabilityId = availabilityRepository.nextId("T");

            InstructorAvailability availability =
                    new InstructorAvailability(
                            availabilityId,
                            instructorId,
                            start,
                            end);

            availabilityRepository.save(availability);

            return availability;
        }
    }
}
