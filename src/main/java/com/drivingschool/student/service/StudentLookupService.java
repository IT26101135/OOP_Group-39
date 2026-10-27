package com.drivingschool.student.service;

import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.student.api.StudentLookup;
import com.drivingschool.student.dto.StudentSummary;
import com.drivingschool.student.model.Student;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StudentLookupService implements StudentLookup {

    private final UserRepository userRepository;

    public StudentLookupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean exists(String studentId) {
        return findById(studentId).isPresent();
    }

    @Override
    public Optional<StudentSummary> findById(String studentId) {
        validateStudentId(studentId);

        return userRepository.findByIdStrict(studentId)
                .filter(Student.class::isInstance)
                .map(Student.class::cast)
                .map(student -> new StudentSummary(
                        student.getId(),
                        student.getName()
                ));
    }

    private void validateStudentId(String studentId) {
        if (studentId == null
                || !studentId.matches("S(?!000)[0-9]{3}")) {
            throw new ValidationException(
                    "Student ID must be between S001 and S999."
            );
        }
    }
}