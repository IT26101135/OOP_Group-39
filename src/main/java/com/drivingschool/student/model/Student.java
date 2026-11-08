// Module owner: IT26101135
package com.drivingschool.student.model;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.common.exception.ValidationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

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

    public String getNic() { return nic; }

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

    public LocalDate getDateOfBirth() { return dateOfBirth; }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now())) {
            throw new ValidationException("Date of birth cannot be in the future.");
        }
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() { return address; }

    public void setAddress(String address) {
        this.address = optionalText(address, "Address", 500);
    }

    public String getLicenseTypeWanted() { return licenseTypeWanted; }

    public void setLicenseTypeWanted(String licenseTypeWanted) {
        this.licenseTypeWanted = optionalText(licenseTypeWanted, "License type", 80);
    }

    public String getEmergencyContact() { return emergencyContact; }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = optionalText(emergencyContact, "Emergency contact", 120);
    }

    public String getActivePackageId() { return activePackageId; }

    public void setActivePackageId(String activePackageId) {
        if (activePackageId != null
                && !activePackageId.matches("P(?!000)[0-9]{3}")) {
            throw new ValidationException("Active package ID is invalid.");
        }
        this.activePackageId = activePackageId;
    }

    public int getLessonsRemaining() { return lessonsRemaining; }

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

    public String getActiveEnrollmentId() { return activeEnrollmentId; }
    public void setActiveEnrollmentId(String activeEnrollmentId) {
        if (activeEnrollmentId != null
                && !activeEnrollmentId.matches("E(?!000)[0-9]{3}")) {
            throw new ValidationException("Active enrollment ID is invalid.");
        }
        this.activeEnrollmentId = activeEnrollmentId;
    }

    private String optionalText(String value, String label, int maxLength) {
        if (value == null || value.isBlank()) return null;
        String cleaned = value.trim();
        if (cleaned.length() > maxLength) {
            throw new ValidationException(label + " must be at most " + maxLength + " characters.");
        }
        return cleaned;
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
