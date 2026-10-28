package com.drivingschool.auth.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Admin extends User {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Admin(
            @JsonProperty(value = "id", required = true)
            String id,

            @JsonProperty(value = "name", required = true)
            String name,

            @JsonProperty(value = "email", required = true)
            String email,

            @JsonProperty(value = "passwordHash", required = true)
            String passwordHash) {

        super(id, "A", name, email, passwordHash);
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }

    @Override
    public String getDashboardPath() {
        return "/admin/dashboard";
    }
}