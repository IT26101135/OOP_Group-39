package com.drivingschool.learningpackage.model;

import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.common.model.Identifiable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LearningPackage implements Identifiable {

    private final String id;
    private String name;
    private String description;
    private BigDecimal price;
    private int lessonCount;
    private int validityDays;
    private boolean active;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
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

        if (validityDays == null) {
            throw new ValidationException("Validity days are required.");
        }
        setValidityDays(validityDays);

        if (active == null) {
            throw new ValidationException("Active status is required.");
        }
        setActive(active);
    }

    private static String validateId(String id) {
        if (id == null || !id.matches("P(?!000)[0-9]{3}")) {
            throw new ValidationException(
                    "Package ID must be between P001 and P999.");
        }

        return id;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

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

    public String getDescription() {
        return description;
    }

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

    public BigDecimal getPrice() {
        return price;
    }

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

    public int getLessonCount() {
        return lessonCount;
    }

    public void setLessonCount(int lessonCount) {
        if (lessonCount < 1 || lessonCount > 100) {
            throw new ValidationException(
                    "Lesson count must be between 1 and 100.");
        }

        this.lessonCount = lessonCount;
    }

    public int getValidityDays() {
        return validityDays;
    }

    public void setValidityDays(int validityDays) {
        if (validityDays < 1 || validityDays > 365) {
            throw new ValidationException(
                    "Validity days must be between 1 and 365.");
        }

        this.validityDays = validityDays;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}