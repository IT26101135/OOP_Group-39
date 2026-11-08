/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : InstructorAccount.java
WHAT IT DOES (simple words): Keeps the details and rules of instructor account.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.auth.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Login identity for an instructor profile stored in the instructor module. */
// OOP: INHERITANCE - This account reuses the name, email and validation in User.
// SOLID: L - This child works wherever a User account is expected.
public final class InstructorAccount extends User {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    // Receives the helpers this class needs so each job stays separate.
    public InstructorAccount(
            @JsonProperty(value = "id", required = true) String id,
            @JsonProperty(value = "name", required = true) String name,
            @JsonProperty(value = "email", required = true) String email,
            @JsonProperty(value = "passwordHash", required = true) String passwordHash) {
        super(id, "I", name, email, passwordHash);
    }

    // OOP: POLYMORPHISM - The same User method supplies this account type's answer.
    @Override
    // Shares the role through a public method while the field stays private.
    public Role getRole() {
        return Role.INSTRUCTOR;
    }

    // OOP: POLYMORPHISM - The same User method supplies this account type's answer.
    @Override
    // Shares the dashboard path through a public method while the field stays private.
    public String getDashboardPath() {
        return "/instructor/dashboard";
    }
}
