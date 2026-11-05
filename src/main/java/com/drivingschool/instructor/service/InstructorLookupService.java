package com.drivingschool.instructor.service;

import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.instructor.api.InstructorBookingInfo;
import com.drivingschool.instructor.api.InstructorLookup;
import com.drivingschool.instructor.model.Instructor;
import com.drivingschool.instructor.model.InstructorAvailability;
import com.drivingschool.instructor.repository.InstructorAvailabilityRepository;
import com.drivingschool.instructor.repository.InstructorRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class InstructorLookupService implements InstructorLookup {

    private final InstructorRepository instructorRepository;
    private final InstructorAvailabilityRepository availabilityRepository;

    public InstructorLookupService(
            InstructorRepository instructorRepository,
            InstructorAvailabilityRepository availabilityRepository) {

        this.instructorRepository = Objects.requireNonNull(instructorRepository);
        this.availabilityRepository = Objects.requireNonNull(availabilityRepository);
    }

    @Override
    public Optional<InstructorBookingInfo> findById(String instructorId) {
        validateInstructorId(instructorId);

        return instructorRepository.findByIdStrict(instructorId)
                .map(instructor -> new InstructorBookingInfo(
                        instructor.getId(),
                        instructor.getName(),
                        instructor.isActive()));
    }

    @Override
    public boolean isWorkingAvailable(
            String instructorId,
            LocalDateTime start,
            LocalDateTime end) {

        validateInstructorId(instructorId);
        validateInterval(start, end);

        Optional<Instructor> instructor =
                instructorRepository.findByIdStrict(instructorId);

        List<InstructorAvailability> intervals =
                availabilityRepository.readAllStrict();

        if (instructor.isEmpty() || !instructor.get().isActive()) {
            return false;
        }

        List<InstructorAvailability> matching = intervals.stream()
                .filter(item -> item.getInstructorId().equals(instructorId))
                .sorted(Comparator.comparing(
                        InstructorAvailability::getStartTime))
                .toList();

        LocalDateTime coveredUntil = start;

        for (InstructorAvailability interval : matching) {
            if (interval.getStartTime().isAfter(coveredUntil)) {
                break;
            }

            if (interval.getEndTime().isAfter(coveredUntil)) {
                coveredUntil = interval.getEndTime();
            }

            if (!coveredUntil.isBefore(end)) {
                return true;
            }
        }

        return false;
    }

    private static void validateInstructorId(String instructorId) {
        if (instructorId == null
                || !instructorId.matches("I(?!000)[0-9]{3}")) {
            throw new ValidationException(
                    "Instructor ID must be between I001 and I999.");
        }
    }

    private static void validateInterval(
            LocalDateTime start,
            LocalDateTime end) {

        if (start == null || end == null || !start.isBefore(end)) {
            throw new ValidationException(
                    "Start time must be before end time.");
        }
    }
}
