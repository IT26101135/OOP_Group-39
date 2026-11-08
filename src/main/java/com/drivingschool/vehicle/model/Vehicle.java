/**
============================================================
MEMBER   : IT26101132 - [Name]
MODULE   : Vehicles and Sample Data
FILE     : Vehicle.java
WHAT IT DOES (simple words): Keeps a vehicle identity, type and maintenance dates together.
REAL-LIFE EXAMPLE: Like a fleet logbook showing which teaching cars need attention.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for vehicles
============================================================
*/
package com.drivingschool.vehicle.model;

import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.common.model.Identifiable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Locale;
import java.time.LocalDate;

// OOP: ENCAPSULATION - Private details are checked before a vehicle can be saved.
public class Vehicle implements Identifiable {

    private final String id;

    private String registrationNumber;
    private String make;
    private String model;
    private TransmissionType transmissionType;
    private VehicleCategory vehicleCategory;
    private VehicleOperationalStatus operationalStatus;
    private LocalDate insuranceExpiry;
    private LocalDate lastServiceDate;
    private LocalDate nextServiceDate;

    // Receives the details this class needs and keeps them ready for its own job.
    @JsonCreator
    public Vehicle(
            @JsonProperty("id") String id,
            @JsonProperty("registrationNumber") String registrationNumber,
            @JsonProperty("make") String make,
            @JsonProperty("model") String model,
            @JsonProperty("transmissionType") TransmissionType transmissionType,
            @JsonProperty("vehicleCategory") VehicleCategory vehicleCategory,
            @JsonProperty("operationalStatus") VehicleOperationalStatus operationalStatus) {

        this.id = validateId(id);
        this.registrationNumber = normalizeRegistrationNumber(registrationNumber);
        this.make = validateText(make, "Make");
        this.model = validateText(model, "Model");
        this.transmissionType = validateEnum(transmissionType, "Transmission type");
        this.vehicleCategory = validateEnum(vehicleCategory, "Vehicle category");
        this.operationalStatus = validateEnum(
                operationalStatus,
                "Operational status"
        );
    }

    // Returns the id for display or school rules.
    @Override
    public String getId() {
        return id;
    }

    // Returns the registration number for display or school rules.
    public String getRegistrationNumber() {
        return registrationNumber;
    }

    // Returns the make for display or school rules.
    public String getMake() {
        return make;
    }

    // Returns the model for display or school rules.
    public String getModel() {
        return model;
    }

    // Returns the transmission type for display or school rules.
    public TransmissionType getTransmissionType() {
        return transmissionType;
    }

    // Returns the vehicle category for display or school rules.
    public VehicleCategory getVehicleCategory() {
        return vehicleCategory;
    }

    // Returns the operational status for display or school rules.
    public VehicleOperationalStatus getOperationalStatus() {
        return operationalStatus;
    }
    // Returns the insurance expiry for display or school rules.
    public LocalDate getInsuranceExpiry() { return insuranceExpiry; }
    // Returns the last service date for display or school rules.
    public LocalDate getLastServiceDate() { return lastServiceDate; }
    // Returns the next service date for display or school rules.
    public LocalDate getNextServiceDate() { return nextServiceDate; }
    // Stores the insurance expiry supplied by the form or saved record.
    public void setInsuranceExpiry(LocalDate date) { insuranceExpiry = date; }
    // Stores the last service date supplied by the form or saved record.
    public void setLastServiceDate(LocalDate date) {
        if (date != null && date.isAfter(LocalDate.now()))
            throw new ValidationException("Last service date cannot be in the future.");
        if (date != null && nextServiceDate != null && !nextServiceDate.isAfter(date))
            throw new ValidationException("Next service date must be after the last service date.");
        lastServiceDate = date;
    }
    // Stores the next service date supplied by the form or saved record.
    public void setNextServiceDate(LocalDate date) {
        if (date != null && lastServiceDate != null && !date.isAfter(lastServiceDate))
            throw new ValidationException("Next service date must be after the last service date.");
        nextServiceDate = date;
    }
    // Shows a stronger warning when the insurance date has already passed.
    @JsonIgnore
    public boolean isInsuranceExpired() {
        return insuranceExpiry != null && insuranceExpiry.isBefore(LocalDate.now());
    }
    // Shows a stronger warning when the planned service date has already passed.
    @JsonIgnore
    public boolean isServiceOverdue() {
        return nextServiceDate != null && nextServiceDate.isBefore(LocalDate.now());
    }
    // Warns about overdue dates as well as dates due within the next month.
    @JsonIgnore public boolean isInsuranceWarning() { return withinThirtyDays(insuranceExpiry); }
    @JsonIgnore public boolean isServiceWarning() { return withinThirtyDays(nextServiceDate); }
    private boolean withinThirtyDays(LocalDate date) {
        return date != null && !date.isAfter(LocalDate.now().plusDays(30));
    }

    // Stores the registration number supplied by the form or saved record.
    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = normalizeRegistrationNumber(registrationNumber);
    }

    // Stores the make supplied by the form or saved record.
    public void setMake(String make) {
        this.make = validateText(make, "Make");
    }

    // Stores the model supplied by the form or saved record.
    public void setModel(String model) {
        this.model = validateText(model, "Model");
    }

    // Stores the transmission type supplied by the form or saved record.
    public void setTransmissionType(TransmissionType transmissionType) {
        this.transmissionType = validateEnum(
                transmissionType,
                "Transmission type"
        );
    }

    // Stores the vehicle category supplied by the form or saved record.
    public void setVehicleCategory(VehicleCategory vehicleCategory) {
        this.vehicleCategory = validateEnum(
                vehicleCategory,
                "Vehicle category"
        );
    }

    // Stores the operational status supplied by the form or saved record.
    public void setOperationalStatus(
            VehicleOperationalStatus operationalStatus) {

        this.operationalStatus = validateEnum(
                operationalStatus,
                "Operational status"
        );
    }

    // Makes plate spacing and letter case consistent before duplicate checks.
    public static String normalizeRegistrationNumber(String value) {

        if (value == null || value.isBlank()) {
            throw new ValidationException(
                    "Registration number is required."
            );
        }

        // Step 1: Treat spacing, hyphens and letter case as the same plate.
        String normalized = value
                .trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[\\s-]", "");

        // Step 2: Reject plates whose length or characters cannot identify a vehicle.
        if (normalized.length() < 4 || normalized.length() > 12) {
            throw new ValidationException(
                    "Registration number must contain 4 to 12 characters."
            );
        }

        if (!normalized.matches("[A-Z0-9]+")) {
            throw new ValidationException(
                    "Registration number may contain only letters and digits."
            );
        }

        // Step 3: Require both a letter and a number for the school plate format.
        if (!normalized.matches(".*[A-Z].*")) {
            throw new ValidationException(
                    "Registration number must contain at least one letter."
            );
        }

        if (!normalized.matches(".*[0-9].*")) {
            throw new ValidationException(
                    "Registration number must contain at least one digit."
            );
        }

        return normalized;
    }

    // Refuses malformed IDs before looking in the filing drawer.
    private static String validateId(String id) {

        if (id == null || !id.matches("V\\d{3}")) {
            throw new ValidationException(
                    "Vehicle ID must use the format V001 to V999."
            );
        }

        int number = Integer.parseInt(id.substring(1));

        if (number < 1 || number > 999) {
            throw new ValidationException(
                    "Vehicle ID must be between V001 and V999."
            );
        }

        return id;
    }

    // Checks that the entered detail is present and short enough to store.
    private static String validateText(String value, String fieldName) {

        if (value == null) {
            throw new ValidationException(
                    fieldName + " is required."
            );
        }

        String trimmed = value.trim();

        if (trimmed.length() < 1 || trimmed.length() > 50) {
            throw new ValidationException(
                    fieldName + " must contain 1 to 50 characters."
            );
        }

        return trimmed;
    }

    // Checks a required choice; the simple type parameter returns the same choice type.
    private static <T> T validateEnum(T value, String fieldName) {

        if (value == null) {
            throw new ValidationException(
                    fieldName + " is required."
            );
        }

        return value;
    }
}
