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

    public RegistrationForm() {
        // Spring MVC needs an empty constructor for form binding.
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        // Preserve the exact password entered by the user.
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    /**
     * Removes password values before redisplaying an unsuccessful form.
     * This does not guarantee erasure of Java Strings from memory.
     */
    public void clearPasswords() {
        this.password = null;
        this.confirmPassword = null;
    }
}