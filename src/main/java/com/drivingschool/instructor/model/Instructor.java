/**
============================================================
MEMBER   : IT26101133 - [Name]
MODULE   : Instructors and bubble sort
FILE     : Instructor.java
WHAT IT DOES (simple words): Keeps the details and validation rules of instructor.
REAL-LIFE EXAMPLE: A teacher diary keeps teaching hours, lesson notes and profile details organised.
OOP USED : Encapsulation (private fields and public methods)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/

package com.drivingschool.instructor.model;

import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.common.model.Identifiable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import java.util.ArrayList;
import java.util.List;

// OOP: ENCAPSULATION - Private fields protect details; constructors and methods validate changes.
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
    private String bio;
    private String licenseNumber;
    private String qualifications;
    private String photoPath;
    private List<String> vehicleTypes = new ArrayList<>();
    private double averageRating;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    // Receives the helpers or validates the details needed to create this object.
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
    // Lets callers read the id without exposing the private field.
    public String getId() {
        return id;
    }

    // Lets callers read the name without exposing the private field.
    public String getName() {
        return name;
    }

    // Changes the name through the record's validation rules.
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

    // Lets callers read the email without exposing the private field.
    public String getEmail() {
        return email;
    }

    // Changes the email through the record's validation rules.
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

    // Lets callers read the phone without exposing the private field.
    public String getPhone() {
        return phone;
    }

    // Changes the phone through the record's validation rules.
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

    // Lets callers read the years of experience without exposing the private field.
    public int getYearsOfExperience() {
        return yearsOfExperience;
    }

    // Changes the years of experience through the record's validation rules.
    public void setYearsOfExperience(int yearsOfExperience) {
        if (yearsOfExperience < 0 || yearsOfExperience > 60) {
            throw new ValidationException(
                    "Years of experience must be between 0 and 60.");
        }

        this.yearsOfExperience = yearsOfExperience;
    }

    // Keeps the is active step in one place for this module.
    public boolean isActive() {
        return active;
    }

    // Changes the active through the record's validation rules.
    public void setActive(boolean active) {
        this.active = active;
    }

    // Lets callers read the bio without exposing the private field.
    public String getBio() { return bio; }
    // Changes the bio through the record's validation rules.
    public void setBio(String bio) { this.bio = optionalText(bio, "Bio", 1000); }
    // Lets callers read the license number without exposing the private field.
    public String getLicenseNumber() { return licenseNumber; }
    // Changes the license number through the record's validation rules.
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = optionalText(licenseNumber, "License number", 40); }
    // Lets callers read the qualifications without exposing the private field.
    public String getQualifications() { return qualifications; }
    // Changes the qualifications through the record's validation rules.
    public void setQualifications(String qualifications) { this.qualifications = optionalText(qualifications, "Qualifications", 500); }
    // Lets callers read the photo path without exposing the private field.
    public String getPhotoPath() { return photoPath; }
    // Changes the photo path through the record's validation rules.
    public void setPhotoPath(String photoPath) { this.photoPath = optionalText(photoPath, "Photo path", 300); }
    // Lets callers read the vehicle types without exposing the private field.
    public List<String> getVehicleTypes() { return List.copyOf(vehicleTypes); }

    // Changes the vehicle types through the record's validation rules.
    public void setVehicleTypes(List<String> vehicleTypes) {
        if (vehicleTypes == null) {
            this.vehicleTypes = new ArrayList<>();
            return;
        }
        List<String> validated = new ArrayList<>();
        for (String type : vehicleTypes) {
            String cleaned = optionalText(type, "Vehicle type", 40);
            if (cleaned != null) validated.add(cleaned);
        }
        this.vehicleTypes = validated;
    }

    // Lets callers read the average rating without exposing the private field.
    public double getAverageRating() { return averageRating; }
    // Changes the average rating through the record's validation rules.
    public void setAverageRating(double averageRating) {
        if (!Double.isFinite(averageRating) || averageRating < 0 || averageRating > 5) {
            throw new ValidationException("Average rating must be between 0 and 5.");
        }
        this.averageRating = averageRating;
    }

    // Trims optional details and keeps them within the form's size limit.
    private String optionalText(String value, String label, int limit) {
        if (value == null || value.isBlank()) return null;
        String cleaned = value.trim();
        if (cleaned.length() > limit) {
            throw new ValidationException(label + " must be at most " + limit + " characters.");
        }
        return cleaned;
    }

    // Checks a saved teaching type without depending on how its letters were capitalised.
    public boolean teachesVehicleType(String requestedType) {
        for (String taughtType : vehicleTypes) {
            if (taughtType.equalsIgnoreCase(requestedType)) return true;
        }
        return false;
    }
}
