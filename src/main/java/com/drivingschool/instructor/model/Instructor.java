
package com.drivingschool.instructor.model;

import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.common.model.Identifiable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;

public class Instructor implements Identifiable {

    private static final Validator EMAIL_VALIDATOR =
            Validation.buildDefaultValidatorFactory().getValidator();

    private final String id;
    private String name;

    @Email(regexp =
            "^[A-Za-z0-9_+%-]+(?:\\.[A-Za-z0-9_+%-]+)*" +
                    "@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?" +
                    "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+$")
    private String email;

    private String phone;
    private int yearsOfExperience;
    private boolean active;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Instructor(
            @JsonProperty(value = "id", required = true) String id,
            @JsonProperty(value = "name", required = true) String name,
            @JsonProperty(value = "email", required = true) String email,
            @JsonProperty(value = "phone", required = true) String phone,
            @JsonProperty(value = "yearsOfExperience", required = true)
            Integer yearsOfExperience,
            @JsonProperty(value = "active", required = true)
            Boolean active) {

        if (id == null || !id.matches("I(?!000)[0-9]{3}")) {
            throw new ValidationException(
                    "Instructor ID must be between I001 and I999.");
        }

        this.id = id;

        setName(name);
        setEmail(email);
        setPhone(phone);

        if (yearsOfExperience == null) {
            throw new ValidationException(
                    "Years of experience is required.");
        }

        setYearsOfExperience(yearsOfExperience);

        if (active == null) {
            throw new ValidationException(
                    "Instructor active status is required.");
        }

        setActive(active);
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            throw new ValidationException(
                    "Instructor name is required.");
        }

        String cleaned = name.trim();

        if (cleaned.length() < 2 || cleaned.length() > 100) {
            throw new ValidationException(
                    "Instructor name must contain 2 to 100 characters.");
        }

        this.name = cleaned;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null) {
            throw new ValidationException(
                    "Instructor email is required.");
        }

        String cleaned = email.trim();

        if (cleaned.isEmpty()
                || cleaned.length() > 254
                || !EMAIL_VALIDATOR
                .validateValue(
                        Instructor.class,
                        "email",
                        cleaned)
                .isEmpty()) {

            throw new ValidationException(
                    "Enter a valid instructor email address.");
        }

        this.email = cleaned;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone == null) {
            throw new ValidationException(
                    "Instructor phone number is required.");
        }

        String cleaned = phone.trim();

        if (!cleaned.matches("\\+?[0-9]{7,15}")) {
            throw new ValidationException(
                    "Phone number must contain 7 to 15 digits, "
                            + "optionally starting with +.");
        }

        this.phone = cleaned;
    }

    public int getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(int yearsOfExperience) {
        if (yearsOfExperience < 0 || yearsOfExperience > 60) {
            throw new ValidationException(
                    "Years of experience must be between 0 and 60.");
        }

        this.yearsOfExperience = yearsOfExperience;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
