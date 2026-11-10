/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : Student.java
WHAT IT DOES (simple words): Keeps the details and rules of student.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.student.model;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.common.exception.ValidationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

// OOP: INHERITANCE - This account reuses the name, email and validation in User.
// SOLID: L - This child works wherever a User account is expected.
public final class Student extends User {

    private String phone;
    private String nic;
    private LocalDate dateOfBirth;
    private String address;
    private String licenseTypeWanted;
    private String emergencyContact;
    private String activePackageId;
    private int lessonsRemaining;
    private String activeEnrollmentId;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    // Receives the helpers this class needs so each job stays separate.
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

    // Shares the phone through a public method while the field stays private.
    public String getPhone() {
        return phone;
    }

    // Stores the phone after the checks required by this record.
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

    // Shares the nic through a public method while the field stays private.
    public String getNic() { return nic; }

    // Stores the nic after the checks required by this record.
    public void setNic(String nic) {
        if (nic == null || nic.isBlank()) {
            this.nic = null;
            return;
        }
        String cleaned = nic.trim().toUpperCase(java.util.Locale.ROOT);
        if (!cleaned.matches("(?:[0-9]{9}[VX]|[0-9]{12})")) {
            throw new ValidationException("Enter a valid NIC (9 digits plus V/X or 12 digits).");
        }
        this.nic = cleaned;
    }

    // Shares the date of birth through a public method while the field stays private.
    public LocalDate getDateOfBirth() { return dateOfBirth; }

    // Stores the date of birth after the checks required by this record.
    public void setDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now())) {
            throw new ValidationException("Date of birth cannot be in the future.");
        }
        this.dateOfBirth = dateOfBirth;
    }

    // Shares the address through a public method while the field stays private.
    public String getAddress() { return address; }

    // Stores the address after the checks required by this record.
    public void setAddress(String address) {
        this.address = optionalText(address, "Address", 500);
    }

    // Shares the license type wanted through a public method while the field stays private.
    public String getLicenseTypeWanted() { return licenseTypeWanted; }

    // Stores the license type wanted after the checks required by this record.
    public void setLicenseTypeWanted(String licenseTypeWanted) {
        this.licenseTypeWanted = optionalText(licenseTypeWanted, "License type", 80);
    }

    // Shares the emergency contact through a public method while the field stays private.
    public String getEmergencyContact() { return emergencyContact; }

    // Stores the emergency contact after the checks required by this record.
    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = optionalText(emergencyContact, "Emergency contact", 120);
    }

    // Shares the active package id through a public method while the field stays private.
    public String getActivePackageId() { return activePackageId; }

    // Stores the active package id after the checks required by this record.
    public void setActivePackageId(String activePackageId) {
        if (activePackageId != null
                && !activePackageId.matches("P(?!000)[0-9]{3}")) {
            throw new ValidationException("Active package ID is invalid.");
        }
        this.activePackageId = activePackageId;
    }

    // Shares the lessons remaining through a public method while the field stays private.
    public int getLessonsRemaining() { return lessonsRemaining; }

    // Stores the lessons remaining after the checks required by this record.
    public void setLessonsRemaining(int lessonsRemaining) {
        if (lessonsRemaining < 0 || lessonsRemaining > 100) {
            throw new ValidationException("Remaining lessons must be between 0 and 100.");
        }
        this.lessonsRemaining = lessonsRemaining;
        if (lessonsRemaining == 0) {
            this.activePackageId = null;
            this.activeEnrollmentId = null;
        }
    }

    // Shares the active enrollment id through a public method while the field stays private.
    public String getActiveEnrollmentId() { return activeEnrollmentId; }
    // Stores the active enrollment id after the checks required by this record.
    public void setActiveEnrollmentId(String activeEnrollmentId) {
        if (activeEnrollmentId != null
                && !activeEnrollmentId.matches("E(?!000)[0-9]{3}")) {
            throw new ValidationException("Active enrollment ID is invalid.");
        }
        this.activeEnrollmentId = activeEnrollmentId;
    }

    // Handles optional text for this class without changing another class's job.
    private String optionalText(String value, String label, int maxLength) {
        if (value == null || value.isBlank()) return null;
        String cleaned = value.trim();
        if (cleaned.length() > maxLength) {
            throw new ValidationException(label + " must be at most " + maxLength + " characters.");
        }
        return cleaned;
    }

    // OOP: POLYMORPHISM - The same User method supplies this account type's answer.
    @Override
    // Shares the role through a public method while the field stays private.
    public Role getRole() {
        return Role.STUDENT;
    }

    // OOP: POLYMORPHISM - The same User method supplies this account type's answer.
    @Override
    // Shares the dashboard path through a public method while the field stays private.
    public String getDashboardPath() {
        return "/student/profile";
    }
}
