package com.drivingschool.auth.form;

/**
 * Holds browser input for login.
 * Credential verification belongs to AuthService.
 * Session management belongs to AuthSessionService.
 */
public class LoginForm {

    private String email;
    private String password;

    public LoginForm() {
        // Spring MVC needs an empty constructor for form binding.
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        // Do not trim or otherwise modify passwords.
        this.password = password;
    }

    public void clearPassword() {
        this.password = null;
    }
}