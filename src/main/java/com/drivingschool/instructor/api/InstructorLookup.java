package com.drivingschool.instructor.api;

import java.time.LocalDateTime;
import java.util.Optional;

public interface InstructorLookup {

    Optional<InstructorBookingInfo> findById(String instructorId);

    boolean isWorkingAvailable(
            String instructorId,
            LocalDateTime start,
            LocalDateTime end);
}
