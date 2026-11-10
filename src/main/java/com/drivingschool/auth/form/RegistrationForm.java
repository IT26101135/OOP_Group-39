/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : RegistrationForm.java
WHAT IT DOES (simple words): Carries the details typed into the registration form form.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.auth.form;

/**
 * Holds browser input for student registration.
 * IDs, roles and password hashes are deliberately excluded.
 * AuthService and the domain models enforce registration rules.
 */
public class RegistrationForm {

    private String name;
    private String email;
    private String phone;
    private String password;
    private String confirmPassword;

    // Receives the helpers this class needs so each job stays separate.
    public RegistrationForm() {
        // Spring MVC needs an empty constructor for form binding.
    }

    // Shares the name through a public method while the field stays private.
    public String getName() {
        return name;
    }

    // Stores the name after the checks required by this record.
    public void setName(String name) {
        this.name = name;
    }

    // Shares the email through a public method while the field stays private.
    public String getEmail() {
        return email;
    }

    // Stores the email after the checks required by this record.
    public void setEmail(String email) {
        this.email = email;
    }

    // Shares the phone through a public method while the field stays private.
    public String getPhone() {
        return phone;
    }

    // Stores the phone after the checks required by this record.
    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Shares the password through a public method while the field stays private.
    public String getPassword() {
        return password;
    }

    // Stores the password after the checks required by this record.
    public void setPassword(String password) {
        // Preserve the exact password entered by the user.
        this.password = password;
    }

    // Shares the confirm password through a public method while the field stays private.
    public String getConfirmPassword() {
        return confirmPassword;
    }

    // Stores the confirm password after the checks required by this record.
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    /**
     * Removes password values before redisplaying an unsuccessful form.
     * This does not guarantee erasure of Java Strings from memory.
     */
    // Handles clear passwords for this class without changing another class's job.
    public void clearPasswords() {
        this.password = null;
        this.confirmPassword = null;
    }
}