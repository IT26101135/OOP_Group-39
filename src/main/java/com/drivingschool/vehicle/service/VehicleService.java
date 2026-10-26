package com.drivingschool.vehicle.service;

import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.vehicle.model.Vehicle;
import com.drivingschool.vehicle.repository.VehicleRepository;

import java.util.List;

public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public Vehicle createVehicle(
            String registrationNumber,
            String make,
            String model,
            com.drivingschool.vehicle.model.TransmissionType transmissionType,
            com.drivingschool.vehicle.model.VehicleCategory vehicleCategory,
            com.drivingschool.vehicle.model.VehicleOperationalStatus operationalStatus) {

        String id = vehicleRepository.nextId("V");

        ensureRegistrationNumberIsUnique(registrationNumber, null);

        Vehicle vehicle = new Vehicle(
                id,
                registrationNumber,
                make,
                model,
                transmissionType,
                vehicleCategory,
                operationalStatus
        );

        vehicleRepository.save(vehicle);

        return vehicle;
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.readAll();
    }

    public Vehicle getVehicleById(String id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() ->
                        new RecordNotFoundException(
                                "Vehicle not found: " + id
                        )
                );
    }

    public Vehicle updateVehicle(
            String id,
            String registrationNumber,
            String make,
            String model) {

        Vehicle existingVehicle = getVehicleById(id);

        ensureRegistrationNumberIsUnique(
                registrationNumber,
                existingVehicle.getId()
        );

        Vehicle updatedVehicle = new Vehicle(
                existingVehicle.getId(),
                registrationNumber,
                make,
                model,
                existingVehicle.getTransmissionType(),
                existingVehicle.getVehicleCategory(),
                existingVehicle.getOperationalStatus()
        );

        vehicleRepository.update(updatedVehicle);

        return updatedVehicle;
    }


    private void ensureRegistrationNumberIsUnique(
            String registrationNumber,
            String currentVehicleId) {

        String normalizedRegistration =
                Vehicle.normalizeRegistrationNumber(registrationNumber);

        boolean duplicateExists = vehicleRepository.readAllStrict()
                .stream()
                .anyMatch(vehicle ->
                        normalizedRegistration.equals(
                                vehicle.getRegistrationNumber()
                        )
                                && !vehicle.getId().equals(currentVehicleId)
                );

        if (duplicateExists) {
            throw new DuplicateRecordException(
                    "Registration number already exists: "
                            + normalizedRegistration
            );
        }
    }
}