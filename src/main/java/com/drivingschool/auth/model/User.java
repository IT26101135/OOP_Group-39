/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : User.java
WHAT IT DOES (simple words): Keeps the details and rules of user.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.auth.model;

import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.common.model.Identifiable;
import com.drivingschool.student.model.Student;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Locale;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Student.class, name = "STUDENT"),
        @JsonSubTypes.Type(value = InstructorAccount.class, name = "INSTRUCTOR"),
        @JsonSubTypes.Type(value = Admin.class, name = "ADMIN")
})
// OOP: ABSTRACTION - Each account must supply its role and dashboard destination.
// OOP: ENCAPSULATION - Private fields can only change through validated methods.
// SOLID: O - Another account type can extend User and supply its own answers.
public abstract class User implements Identifiable {

    // Checks the encoded format; it does not hash or verify passwords.
    private static final Pattern BCRYPT_FORMAT = Pattern.compile(
            "^\\$2[aby]\\$(0[4-9]|[12][0-9]|3[01])\\$[./A-Za-z0-9]{53}$"
    );

    private final String id;
    private String name;
    private String email;
    private String passwordHash;
    private AccountStatus status = AccountStatus.ACTIVE;
    private String createdAt = LocalDateTime.now().toString();

    // Receives the helpers this class needs so each job stays separate.
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
    // Shares the id through a public method while the field stays private.
    public final String getId() {
        return id;
    }

    // Shares the name through a public method while the field stays private.
    public final String getName() {
        return name;
    }

    // Stores the name after the checks required by this record.
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

    // Shares the email through a public method while the field stays private.
    public final String getEmail() {
        return email;
    }

    // Stores the email after the checks required by this record.
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

    // Shares the password hash through a public method while the field stays private.
    public final String getPasswordHash() {
        return passwordHash;
    }

    // Stores the password hash after the checks required by this record.
    public final void setPasswordHash(String passwordHash) {
        if (passwordHash == null
                || !BCRYPT_FORMAT.matcher(passwordHash).matches()) {
            throw new ValidationException(
                    "A valid BCrypt password hash is required."
            );
        }

        this.passwordHash = passwordHash;
    }

    // Shares the status through a public method while the field stays private.
    public final AccountStatus getStatus() {
        return status;
    }

    // Stores the status after the checks required by this record.
    public final void setStatus(AccountStatus status) {
        if (status == null) {
            throw new ValidationException("Account status is required.");
        }
        this.status = status;
    }

    // Shares the created at through a public method while the field stays private.
    public final String getCreatedAt() {
        return createdAt;
    }

    // Stores the created at after the checks required by this record.
    public final void setCreatedAt(String createdAt) {
        if (createdAt == null || createdAt.isBlank()) {
            throw new ValidationException("Account creation time is required.");
        }
        try {
            LocalDateTime.parse(createdAt);
        } catch (java.time.format.DateTimeParseException exception) {
            throw new ValidationException("Account creation time must be ISO date-time.");
        }
        this.createdAt = createdAt;
    }

    @JsonIgnore
    // Shares the role through a public method while the field stays private.
    public abstract Role getRole();

    @JsonIgnore
    // Shares the dashboard path through a public method while the field stays private.
    public abstract String getDashboardPath();
}
