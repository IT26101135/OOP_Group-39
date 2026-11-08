// Module owner: IT26101135
package com.drivingschool.auth.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Login identity for an instructor profile stored in the instructor module. */
public final class InstructorAccount extends User {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public InstructorAccount(
            @JsonProperty(value = "id", required = true) String id,
            @JsonProperty(value = "name", required = true) String name,
            @JsonProperty(value = "email", required = true) String email,
            @JsonProperty(value = "passwordHash", required = true) String passwordHash) {
        super(id, "I", name, email, passwordHash);
    }

    @Override
    public Role getRole() {
        return Role.INSTRUCTOR;
    }

    @Override
    public String getDashboardPath() {
        return "/instructor/dashboard";
    }
}
