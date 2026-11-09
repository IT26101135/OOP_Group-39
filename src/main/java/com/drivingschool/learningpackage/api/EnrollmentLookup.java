package com.drivingschool.learningpackage.api;

import java.util.Optional;

public interface EnrollmentLookup {

    Optional<EnrollmentSummary> findById(String enrollmentId);
}