/**
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages and enrollment
FILE     : LearningPackage.java
WHAT IT DOES (simple words): Keeps the details and rules of learning package.
REAL-LIFE EXAMPLE: A course folder keeps the learning plan and enrollment details together.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.learningpackage.model;

import com.drivingschool.common.exception.ValidationException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;

// OOP: ENCAPSULATION - Private details are changed only through validation.
public class LearningPackage implements LearningRecord {

    private final String id;
    private String name;
    private String description;
    private BigDecimal price;
    private int lessonCount;
    private int validityDays;
    private boolean active;
    private PackageType type = PackageType.MANUAL;
    private int durationHours;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    // Receives the helpers this class needs so each job stays separate.
    public LearningPackage(
            @JsonProperty(value = "id", required = true) String id,
            @JsonProperty(value = "name", required = true) String name,
            @JsonProperty(value = "description", required = true)
            String description,
            @JsonProperty(value = "price", required = true)
            BigDecimal price,
            @JsonProperty(value = "lessonCount", required = true)
            Integer lessonCount,
            @JsonProperty(value = "validityDays", required = true)
            Integer validityDays,
            @JsonProperty(value = "active", required = true)
            Boolean active) {

        this.id = validateId(id);

        // Use setters so construction and later updates follow the same rules.
        setName(name);
        setDescription(description);
        setPrice(price);

        if (lessonCount == null) {
            throw new ValidationException("Lesson count is required.");
        }
        setLessonCount(lessonCount);
        this.durationHours = lessonCount;

        if (validityDays == null) {
            throw new ValidationException("Validity days are required.");
        }
        setValidityDays(validityDays);

        if (active == null) {
            throw new ValidationException("Active status is required.");
        }
        setActive(active);
    }

    // Stops the operation if validate id rules are not met.
    private static String validateId(String id) {
        if (id == null || !id.matches("P(?!000)[0-9]{3}")) {
            throw new ValidationException(
                    "Package ID must be between P001 and P999.");
        }

        return id;
    }

    @Override
    // Shares the id through a public method while the field stays private.
    public String getId() {
        return id;
    }

    // Shares the name through a public method while the field stays private.
    public String getName() {
        return name;
    }

    // Stores the name after the checks required by this record.
    public void setName(String name) {
        if (name == null) {
            throw new ValidationException("Package name is required.");
        }

        String cleaned = name.trim();

        if (cleaned.length() < 2 || cleaned.length() > 100) {
            throw new ValidationException(
                    "Package name must contain 2 to 100 characters.");
        }

        this.name = cleaned;
    }

    // Shares the description through a public method while the field stays private.
    public String getDescription() {
        return description;
    }

    // Stores the description after the checks required by this record.
    public void setDescription(String description) {
        if (description == null) {
            throw new ValidationException(
                    "Package description is required.");
        }

        String cleaned = description.trim();

        if (cleaned.length() < 10 || cleaned.length() > 1000) {
            throw new ValidationException(
                    "Package description must contain 10 to 1000 characters.");
        }

        this.description = cleaned;
    }

    // Shares the price through a public method while the field stays private.
    public BigDecimal getPrice() {
        return price;
    }

    // Stores the price after the checks required by this record.
    public void setPrice(BigDecimal price) {
        if (price == null) {
            throw new ValidationException("Package price is required.");
        }

        final BigDecimal normalizedPrice;

        try {
            normalizedPrice = price.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new ValidationException(
                    "Package price must not require rounding.");
        }

        if (normalizedPrice.compareTo(BigDecimal.ZERO) <= 0
                || normalizedPrice.compareTo(
                new BigDecimal("1000000.00")) > 0) {
            throw new ValidationException(
                    "Package price must be greater than 0 and "
                            + "not exceed LKR 1,000,000.00.");
        }

        this.price = normalizedPrice;
    }

    // Shares the lesson count through a public method while the field stays private.
    public int getLessonCount() {
        return lessonCount;
    }

    // Stores the lesson count after the checks required by this record.
    public void setLessonCount(int lessonCount) {
        if (lessonCount < 1 || lessonCount > 100) {
            throw new ValidationException(
                    "Lesson count must be between 1 and 100.");
        }

        this.lessonCount = lessonCount;
    }

    // Shares the validity days through a public method while the field stays private.
    public int getValidityDays() {
        return validityDays;
    }

    // Stores the validity days after the checks required by this record.
    public void setValidityDays(int validityDays) {
        if (validityDays < 1 || validityDays > 365) {
            throw new ValidationException(
                    "Validity days must be between 1 and 365.");
        }

        this.validityDays = validityDays;
    }

    // Handles is active for this class without changing another class's job.
    public boolean isActive() {
        return active;
    }

    // Stores the active after the checks required by this record.
    public void setActive(boolean active) {
        this.active = active;
    }

    // Shares the type through a public method while the field stays private.
    public PackageType getType() { return type; }

    // Stores the type after the checks required by this record.
    public void setType(PackageType type) {
        if (type == null) throw new ValidationException("Package type is required.");
        this.type = type;
    }

    // Shares the duration hours through a public method while the field stays private.
    public int getDurationHours() { return durationHours; }

    // Stores the duration hours after the checks required by this record.
    public void setDurationHours(int durationHours) {
        if (durationHours < 1 || durationHours > 1000) {
            throw new ValidationException("Duration must be between 1 and 1000 hours.");
        }
        this.durationHours = durationHours;
    }
    // OOP: POLYMORPHISM - LearningRecord headings differ for a course and an enrollment.
    // SOLID: L - Both records provide a readable heading through the same small contract.
    @Override
    @JsonIgnore
    // Shares the display name through a public method while the field stays private.
    public String getDisplayName() { return name; }
}
