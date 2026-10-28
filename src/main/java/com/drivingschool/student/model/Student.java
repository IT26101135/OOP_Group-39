package com.drivingschool.student.model;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.common.exception.ValidationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Student extends User {

    private String phone;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Student(
            @JsonProperty(value = "id", required = true)
            String id,

            @JsonProperty(value = "name", required = true)
            String name,

            @JsonProperty(value = "email", required = true)
            String email,

            @JsonProperty(value = "passwordHash", required = true)
            String passwordHash,

            @JsonProperty(value = "phone", required = true)
            String phone) {

        super(id, "S", name, email, passwordHash);
        setPhone(phone);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone == null) {
            throw new ValidationException(
                    "Phone number is required."
            );
        }

        String cleaned = phone.trim();

        if (!cleaned.matches("\\+?[0-9]{7,15}")) {
            throw new ValidationException(
                    "Phone number must contain 7 to 15 digits, "
                            + "optionally starting with +."
            );
        }

        this.phone = cleaned;
    }

    @Override
    public Role getRole() {
        return Role.STUDENT;
    }

    @Override
    public String getDashboardPath() {
        return "/student/profile";
    }
}