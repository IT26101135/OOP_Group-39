package com.drivingschool.vehicle.model;

import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.common.model.Identifiable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Locale;

public class Vehicle implements Identifiable {

    private final String id;

    private String registrationNumber;
    private String make;
    private String model;
    private TransmissionType transmissionType;
    private VehicleCategory vehicleCategory;
    private VehicleOperationalStatus operationalStatus;

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

    @Override
    public String getId() {
        return id;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public TransmissionType getTransmissionType() {
        return transmissionType;
    }

    public VehicleCategory getVehicleCategory() {
        return vehicleCategory;
    }

    public VehicleOperationalStatus getOperationalStatus() {
        return operationalStatus;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = normalizeRegistrationNumber(registrationNumber);
    }

    public void setMake(String make) {
        this.make = validateText(make, "Make");
    }

    public void setModel(String model) {
        this.model = validateText(model, "Model");
    }

    public void setTransmissionType(TransmissionType transmissionType) {
        this.transmissionType = validateEnum(
                transmissionType,
                "Transmission type"
        );
    }

    public void setVehicleCategory(VehicleCategory vehicleCategory) {
        this.vehicleCategory = validateEnum(
                vehicleCategory,
                "Vehicle category"
        );
    }

    public void setOperationalStatus(
            VehicleOperationalStatus operationalStatus) {

        this.operationalStatus = validateEnum(
                operationalStatus,
                "Operational status"
        );
    }

    public static String normalizeRegistrationNumber(String value) {

        if (value == null || value.isBlank()) {
            throw new ValidationException(
                    "Registration number is required."
            );
        }

        String normalized = value
                .trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[\\s-]", "");

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

    private static <T> T validateEnum(T value, String fieldName) {

        if (value == null) {
            throw new ValidationException(
                    fieldName + " is required."
            );
        }

        return value;
    }
}