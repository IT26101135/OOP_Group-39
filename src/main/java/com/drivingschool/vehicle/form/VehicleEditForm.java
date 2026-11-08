// Module owner: IT26101132
package com.drivingschool.vehicle.form;

public class VehicleEditForm {

    private String registrationNumber;
    private String make;
    private String model;
    private com.drivingschool.vehicle.model.TransmissionType transmissionType;
    private com.drivingschool.vehicle.model.VehicleCategory vehicleCategory;
    private com.drivingschool.vehicle.model.VehicleOperationalStatus operationalStatus;
    private java.time.LocalDate insuranceExpiry, lastServiceDate, nextServiceDate;

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
    public com.drivingschool.vehicle.model.TransmissionType getTransmissionType() { return transmissionType; }
    public void setTransmissionType(com.drivingschool.vehicle.model.TransmissionType value) { transmissionType = value; }
    public com.drivingschool.vehicle.model.VehicleCategory getVehicleCategory() { return vehicleCategory; }
    public void setVehicleCategory(com.drivingschool.vehicle.model.VehicleCategory value) { vehicleCategory = value; }
    public com.drivingschool.vehicle.model.VehicleOperationalStatus getOperationalStatus() { return operationalStatus; }
    public void setOperationalStatus(com.drivingschool.vehicle.model.VehicleOperationalStatus value) { operationalStatus = value; }
    public java.time.LocalDate getInsuranceExpiry() { return insuranceExpiry; }
    public void setInsuranceExpiry(java.time.LocalDate value) { insuranceExpiry = value; }
    public java.time.LocalDate getLastServiceDate() { return lastServiceDate; }
    public void setLastServiceDate(java.time.LocalDate value) { lastServiceDate = value; }
    public java.time.LocalDate getNextServiceDate() { return nextServiceDate; }
    public void setNextServiceDate(java.time.LocalDate value) { nextServiceDate = value; }
}
