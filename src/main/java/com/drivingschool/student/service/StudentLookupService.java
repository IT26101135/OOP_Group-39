/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : StudentLookupService.java
WHAT IT DOES (simple words): Applies the rules for student lookup.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.student.service;

import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.model.User;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.student.api.StudentLookup;
import com.drivingschool.student.dto.StudentSummary;
import com.drivingschool.student.model.Student;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
// SOLID: S - This class is responsible for application rules.
public class StudentLookupService implements StudentLookup {

    private final UserRepository userRepository;

    // Receives the helpers this class needs so each job stays separate.
    public StudentLookupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    // Handles exists for this class without changing another class's job.
    public boolean exists(String studentId) {
        return findById(studentId).isPresent();
    }

    @Override
    // Handles find by id for this class without changing another class's job.
    public Optional<StudentSummary> findById(String studentId) {
        validateStudentId(studentId);

        Optional<User> account = userRepository.findByIdStrict(studentId);
        if (account.isEmpty() || !(account.get() instanceof Student)) {
            return Optional.empty();
        }
        Student student = (Student) account.get();
        return Optional.of(new StudentSummary(student.getId(), student.getName()));
    }

    // Stops the operation if validate student id rules are not met.
    private void validateStudentId(String studentId) {
        if (studentId == null
                || !studentId.matches("S(?!000)[0-9]{3}")) {
            throw new ValidationException(
                    "Choose a student from the student list and try again."
            );
        }
    }
}