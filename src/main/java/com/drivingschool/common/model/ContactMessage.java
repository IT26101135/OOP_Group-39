// Module owner: IT26101135
package com.drivingschool.common.model;

import com.drivingschool.common.exception.ValidationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Locale;

public class ContactMessage implements Identifiable {
    private final String id;
    private final String name;
    private final String email;
    private final String message;
    private final LocalDateTime createdAt;

    @JsonCreator
    public ContactMessage(@JsonProperty("id") String id,
                          @JsonProperty("name") String name,
                          @JsonProperty("email") String email,
                          @JsonProperty("message") String message,
                          @JsonProperty("createdAt") LocalDateTime createdAt) {
        if (id == null || !id.matches("M(?!000)[0-9]{3}")) {
            throw new ValidationException("Message ID is invalid.");
        }
        if (name == null || name.trim().length() < 2 || name.trim().length() > 100) {
            throw new ValidationException("Enter a name between 2 and 100 characters.");
        }
        if (email == null || !email.trim().matches("^[^\\s@]+@[^\\s@.]+(\\.[^\\s@.]+)+$")) {
            throw new ValidationException("Enter a valid email address.");
        }
        if (message == null || message.trim().length() < 10 || message.trim().length() > 2000) {
            throw new ValidationException("Message must contain 10 to 2000 characters.");
        }
        if (createdAt == null) throw new ValidationException("Message date is required.");
        this.id = id;
        this.name = name.trim();
        this.email = email.trim().toLowerCase(Locale.ROOT);
        this.message = message.trim();
        this.createdAt = createdAt;
    }

    @Override public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
