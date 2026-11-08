/**
============================================================
MEMBER   : IT26101132 - [Name]
MODULE   : Vehicles and Sample Data
FILE     : VehicleCreateForm.java
WHAT IT DOES (simple words): Keeps vehicles and sample data details clear and safe for the school.
REAL-LIFE EXAMPLE: Like a fleet logbook showing which teaching cars need attention.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for vehicles
============================================================
*/
package com.drivingschool.vehicle.form;

import com.drivingschool.vehicle.model.TransmissionType;
import com.drivingschool.vehicle.model.VehicleCategory;
import com.drivingschool.vehicle.model.VehicleOperationalStatus;
import java.time.LocalDate;

public class VehicleCreateForm {

    private String registrationNumber;
    private String make;
    private String model;
    private TransmissionType transmissionType;
    private VehicleCategory vehicleCategory;
    private VehicleOperationalStatus operationalStatus;
    private LocalDate insuranceExpiry;
    private LocalDate lastServiceDate;
    private LocalDate nextServiceDate;
    // Returns the insurance expiry for display or school rules.
    public LocalDate getInsuranceExpiry() { return insuranceExpiry; }
    // Stores the insurance expiry supplied by the form or saved record.
    public void setInsuranceExpiry(LocalDate date) { insuranceExpiry = date; }
    // Returns the last service date for display or school rules.
    public LocalDate getLastServiceDate() { return lastServiceDate; }
    // Stores the last service date supplied by the form or saved record.
    public void setLastServiceDate(LocalDate date) { lastServiceDate = date; }
    // Returns the next service date for display or school rules.
    public LocalDate getNextServiceDate() { return nextServiceDate; }
    // Stores the next service date supplied by the form or saved record.
    public void setNextServiceDate(LocalDate date) { nextServiceDate = date; }

    // Returns the registration number for display or school rules.
    public String getRegistrationNumber() {
        return registrationNumber;
    }

    // Stores the registration number supplied by the form or saved record.
    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    // Returns the make for display or school rules.
    public String getMake() {
        return make;
    }

    // Stores the make supplied by the form or saved record.
    public void setMake(String make) {
        this.make = make;
    }

    // Returns the model for display or school rules.
    public String getModel() {
        return model;
    }

    // Stores the model supplied by the form or saved record.
    public void setModel(String model) {
        this.model = model;
    }

    // Returns the transmission type for display or school rules.
    public TransmissionType getTransmissionType() {
        return transmissionType;
    }

    // Stores the transmission type supplied by the form or saved record.
    public void setTransmissionType(TransmissionType transmissionType) {
        this.transmissionType = transmissionType;
    }

    // Returns the vehicle category for display or school rules.
    public VehicleCategory getVehicleCategory() {
        return vehicleCategory;
    }

    // Stores the vehicle category supplied by the form or saved record.
    public void setVehicleCategory(VehicleCategory vehicleCategory) {
        this.vehicleCategory = vehicleCategory;
    }

    // Returns the operational status for display or school rules.
    public VehicleOperationalStatus getOperationalStatus() {
        return operationalStatus;
    }

    // Stores the operational status supplied by the form or saved record.
    public void setOperationalStatus(
            VehicleOperationalStatus operationalStatus) {
        this.operationalStatus = operationalStatus;
    }
}
