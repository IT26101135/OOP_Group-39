package com.drivingschool.vehicle.form;

import com.drivingschool.vehicle.model.TransmissionType;
import com.drivingschool.vehicle.model.VehicleCategory;
import com.drivingschool.vehicle.model.VehicleOperationalStatus;

public class VehicleCreateForm {

    private String registrationNumber;
    private String make;
    private String model;
    private TransmissionType transmissionType;
    private VehicleCategory vehicleCategory;
    private VehicleOperationalStatus operationalStatus;

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

    public TransmissionType getTransmissionType() {
        return transmissionType;
    }

    public void setTransmissionType(TransmissionType transmissionType) {
        this.transmissionType = transmissionType;
    }

    public VehicleCategory getVehicleCategory() {
        return vehicleCategory;
    }

    public void setVehicleCategory(VehicleCategory vehicleCategory) {
        this.vehicleCategory = vehicleCategory;
    }

    public VehicleOperationalStatus getOperationalStatus() {
        return operationalStatus;
    }

    public void setOperationalStatus(
            VehicleOperationalStatus operationalStatus) {
        this.operationalStatus = operationalStatus;
    }
}