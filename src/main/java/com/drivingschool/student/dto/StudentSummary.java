package com.drivingschool.student.dto;

import com.drivingschool.common.exception.ValidationException;

public final class StudentSummary {

    private final String id;
    private final String name;

    public StudentSummary(String id, String name) {
        if (id == null || !id.matches("S(?!000)[0-9]{3}")) {
            throw new ValidationException(
                    "Student ID must be between S001 and S999."
            );
        }

        if (name == null
                || name.trim().length() < 2
                || name.trim().length() > 100) {
            throw new ValidationException(
                    "Student name must contain 2 to 100 characters."
            );
        }

        this.id = id;
        this.name = name.trim();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}