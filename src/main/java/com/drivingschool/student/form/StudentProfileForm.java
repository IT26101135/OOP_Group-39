// Module owner: IT26101135
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

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getLicenseTypeWanted() { return licenseTypeWanted; }
    public void setLicenseTypeWanted(String licenseTypeWanted) { this.licenseTypeWanted = licenseTypeWanted; }
    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
}
