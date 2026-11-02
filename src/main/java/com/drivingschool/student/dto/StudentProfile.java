package com.drivingschool.student.dto;

/** Immutable profile output. Never contains authentication credentials. */
public final class StudentProfile {
    private final String id;
    private final String name;
    private final String email;
    private final String phone;

    public StudentProfile(String id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
}
