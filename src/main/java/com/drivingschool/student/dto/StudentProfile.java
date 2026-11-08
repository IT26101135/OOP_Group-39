// Module owner: IT26101135
package com.drivingschool.student.dto;

import java.time.LocalDate;

/** Immutable profile output. Never contains authentication credentials. */
public final class StudentProfile {
    private final String id;
    private final String name;
    private final String email;
    private final String phone;
    private final String nic;
    private final LocalDate dateOfBirth;
    private final String address;
    private final String licenseTypeWanted;
    private final String emergencyContact;
    private final String activePackageId;
    private final int lessonsRemaining;

    public StudentProfile(String id, String name, String email, String phone) {
        this(id, name, email, phone, null, null, null, null, null, null, 0);
    }

    public StudentProfile(String id, String name, String email, String phone,
                          String nic, LocalDate dateOfBirth, String address,
                          String licenseTypeWanted, String emergencyContact,
                          String activePackageId, int lessonsRemaining) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.nic = nic;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.licenseTypeWanted = licenseTypeWanted;
        this.emergencyContact = emergencyContact;
        this.activePackageId = activePackageId;
        this.lessonsRemaining = lessonsRemaining;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getNic() { return nic; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getAddress() { return address; }
    public String getLicenseTypeWanted() { return licenseTypeWanted; }
    public String getEmergencyContact() { return emergencyContact; }
    public String getActivePackageId() { return activePackageId; }
    public int getLessonsRemaining() { return lessonsRemaining; }
}
