/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : StudentProfileForm.java
WHAT IT DOES (simple words): Carries the details typed into the student profile form form.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.student.form;

import java.time.LocalDate;

/** Browser input only. Identity and credentials are deliberately absent. */
public class StudentProfileForm {
    private String name;
    private String email;
    private String phone;
    private String nic;
    private LocalDate dateOfBirth;
    private String address;
    private String licenseTypeWanted;
    private String emergencyContact;

    // Shares the name through a public method while the field stays private.
    public String getName() { return name; }
    // Stores the name after the checks required by this record.
    public void setName(String name) { this.name = name; }
    // Shares the email through a public method while the field stays private.
    public String getEmail() { return email; }
    // Stores the email after the checks required by this record.
    public void setEmail(String email) { this.email = email; }
    // Shares the phone through a public method while the field stays private.
    public String getPhone() { return phone; }
    // Stores the phone after the checks required by this record.
    public void setPhone(String phone) { this.phone = phone; }
    // Shares the nic through a public method while the field stays private.
    public String getNic() { return nic; }
    // Stores the nic after the checks required by this record.
    public void setNic(String nic) { this.nic = nic; }
    // Shares the date of birth through a public method while the field stays private.
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    // Stores the date of birth after the checks required by this record.
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    // Shares the address through a public method while the field stays private.
    public String getAddress() { return address; }
    // Stores the address after the checks required by this record.
    public void setAddress(String address) { this.address = address; }
    // Shares the license type wanted through a public method while the field stays private.
    public String getLicenseTypeWanted() { return licenseTypeWanted; }
    // Stores the license type wanted after the checks required by this record.
    public void setLicenseTypeWanted(String licenseTypeWanted) { this.licenseTypeWanted = licenseTypeWanted; }
    // Shares the emergency contact through a public method while the field stays private.
    public String getEmergencyContact() { return emergencyContact; }
    // Stores the emergency contact after the checks required by this record.
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
}
