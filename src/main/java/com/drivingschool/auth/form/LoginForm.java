/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : LoginForm.java
WHAT IT DOES (simple words): Carries the details typed into the login form form.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.auth.form;

/**
 * Holds browser input for login.
 * Credential verification belongs to AuthService.
 * Session management belongs to AuthSessionService.
 */
public class LoginForm {

    private String email;
    private String password;

    // Receives the helpers this class needs so each job stays separate.
    public LoginForm() {
        // Spring MVC needs an empty constructor for form binding.
    }

    // Shares the email through a public method while the field stays private.
    public String getEmail() {
        return email;
    }

    // Stores the email after the checks required by this record.
    public void setEmail(String email) {
        this.email = email;
    }

    // Shares the password through a public method while the field stays private.
    public String getPassword() {
        return password;
    }

    // Stores the password after the checks required by this record.
    public void setPassword(String password) {
        // Do not trim or otherwise modify passwords.
        this.password = password;
    }

    // Handles clear password for this class without changing another class's job.
    public void clearPassword() {
        this.password = null;
    }
}