package com.drivingschool.auth.model;

import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.common.model.Identifiable;
import com.drivingschool.student.model.Student;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Locale;
import java.util.regex.Pattern;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Student.class, name = "STUDENT"),
        @JsonSubTypes.Type(value = Admin.class, name = "ADMIN")
})
public abstract class User implements Identifiable {

    // Checks the encoded format; it does not hash or verify passwords.
    private static final Pattern BCRYPT_FORMAT = Pattern.compile(
            "^\\$2[aby]\\$(0[4-9]|[12][0-9]|3[01])\\$[./A-Za-z0-9]{53}$"
    );

    private final String id;
    private String name;
    private String email;
    private String passwordHash;

    protected User(
            String id,
            String requiredPrefix,
            String name,
            String email,
            String passwordHash) {

        if (id == null
                || !id.matches(requiredPrefix + "(?!000)[0-9]{3}")) {
            throw new ValidationException(
                    "User ID must be between "
                            + requiredPrefix + "001 and "
                            + requiredPrefix + "999."
            );
        }

        this.id = id;
        setName(name);
        setEmail(email);
        setPasswordHash(passwordHash);
    }

    @Override
    public final String getId() {
        return id;
    }

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        if (name == null) {
            throw new ValidationException("Name is required.");
        }

        String cleaned = name.trim();

        if (cleaned.length() < 2 || cleaned.length() > 100) {
            throw new ValidationException(
                    "Name must contain 2 to 100 characters."
            );
        }

        this.name = cleaned;
    }

    public final String getEmail() {
        return email;
    }

    public final void setEmail(String email) {
        if (email == null) {
            throw new ValidationException("Email is required.");
        }

        String cleaned = email.trim().toLowerCase(Locale.ROOT);

        // Basic application-level format validation.
        // This does not prove that the mailbox exists.
        boolean validFormat = cleaned.matches(
                "^[^\\s@]+@[^\\s@.]+(\\.[^\\s@.]+)+$"
        );

        if (cleaned.length() > 254
                || !validFormat
                || cleaned.startsWith(".")
                || cleaned.contains("..")
                || cleaned.contains(".@")) {
            throw new ValidationException(
                    "Enter a valid email address."
            );
        }

        this.email = cleaned;
    }

    public final String getPasswordHash() {
        return passwordHash;
    }

    public final void setPasswordHash(String passwordHash) {
        if (passwordHash == null
                || !BCRYPT_FORMAT.matcher(passwordHash).matches()) {
            throw new ValidationException(
                    "A valid BCrypt password hash is required."
            );
        }

        this.passwordHash = passwordHash;
    }

    @JsonIgnore
    public abstract Role getRole();

    @JsonIgnore
    public abstract String getDashboardPath();
}