/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : StudentProfile.java
WHAT IT DOES (simple words): Shares safe student profile details with pages.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
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

    // Receives the helpers this class needs so each job stays separate.
    public StudentProfile(String id, String name, String email, String phone) {
        this(id, name, email, phone, null, null, null, null, null, null, 0);
    }

    // Receives the helpers this class needs so each job stays separate.
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

    // Shares the id through a public method while the field stays private.
    public String getId() { return id; }
    // Shares the name through a public method while the field stays private.
    public String getName() { return name; }
    // Shares the email through a public method while the field stays private.
    public String getEmail() { return email; }
    // Shares the phone through a public method while the field stays private.
    public String getPhone() { return phone; }
    // Shares the nic through a public method while the field stays private.
    public String getNic() { return nic; }
    // Shares the date of birth through a public method while the field stays private.
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    // Shares the address through a public method while the field stays private.
    public String getAddress() { return address; }
    // Shares the license type wanted through a public method while the field stays private.
    public String getLicenseTypeWanted() { return licenseTypeWanted; }
    // Shares the emergency contact through a public method while the field stays private.
    public String getEmergencyContact() { return emergencyContact; }
    // Shares the active package id through a public method while the field stays private.
    public String getActivePackageId() { return activePackageId; }
    // Shares the lessons remaining through a public method while the field stays private.
    public int getLessonsRemaining() { return lessonsRemaining; }
}
