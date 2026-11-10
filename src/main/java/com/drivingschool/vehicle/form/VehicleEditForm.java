/**
============================================================
MEMBER   : IT26101132 - [Name]
MODULE   : Vehicles and Sample Data
FILE     : VehicleEditForm.java
WHAT IT DOES (simple words): Keeps vehicles and sample data details clear and safe for the school.
REAL-LIFE EXAMPLE: Like a fleet logbook showing which teaching cars need attention.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for vehicles
============================================================
*/
package com.drivingschool.vehicle.form;

public class VehicleEditForm {

    private String registrationNumber;
    private String make;
    private String model;
    private com.drivingschool.vehicle.model.TransmissionType transmissionType;
    private com.drivingschool.vehicle.model.VehicleCategory vehicleCategory;
    private com.drivingschool.vehicle.model.VehicleOperationalStatus operationalStatus;
    private java.time.LocalDate insuranceExpiry, lastServiceDate, nextServiceDate;

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
    public com.drivingschool.vehicle.model.TransmissionType getTransmissionType() { return transmissionType; }
    // Stores the transmission type supplied by the form or saved record.
    public void setTransmissionType(com.drivingschool.vehicle.model.TransmissionType value) { transmissionType = value; }
    // Returns the vehicle category for display or school rules.
    public com.drivingschool.vehicle.model.VehicleCategory getVehicleCategory() { return vehicleCategory; }
    // Stores the vehicle category supplied by the form or saved record.
    public void setVehicleCategory(com.drivingschool.vehicle.model.VehicleCategory value) { vehicleCategory = value; }
    // Returns the operational status for display or school rules.
    public com.drivingschool.vehicle.model.VehicleOperationalStatus getOperationalStatus() { return operationalStatus; }
    // Stores the operational status supplied by the form or saved record.
    public void setOperationalStatus(com.drivingschool.vehicle.model.VehicleOperationalStatus value) { operationalStatus = value; }
    // Returns the insurance expiry for display or school rules.
    public java.time.LocalDate getInsuranceExpiry() { return insuranceExpiry; }
    // Stores the insurance expiry supplied by the form or saved record.
    public void setInsuranceExpiry(java.time.LocalDate value) { insuranceExpiry = value; }
    // Returns the last service date for display or school rules.
    public java.time.LocalDate getLastServiceDate() { return lastServiceDate; }
    // Stores the last service date supplied by the form or saved record.
    public void setLastServiceDate(java.time.LocalDate value) { lastServiceDate = value; }
    // Returns the next service date for display or school rules.
    public java.time.LocalDate getNextServiceDate() { return nextServiceDate; }
    // Stores the next service date supplied by the form or saved record.
    public void setNextServiceDate(java.time.LocalDate value) { nextServiceDate = value; }
}
