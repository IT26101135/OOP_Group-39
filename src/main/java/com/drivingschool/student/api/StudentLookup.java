package com.drivingschool.student.api;

import com.drivingschool.student.dto.StudentSummary;

import java.util.Optional;

public interface StudentLookup {

    boolean exists(String studentId);

    Optional<StudentSummary> findById(String studentId);
}