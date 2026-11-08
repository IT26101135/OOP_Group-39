// Module owner: IT26101132
package com.drivingschool.vehicle.service;

import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.vehicle.model.Vehicle;
import com.drivingschool.vehicle.api.VehicleStore;
import com.drivingschool.booking.repository.BookingRepository;

import java.util.List;

public class VehicleService {

    private final VehicleStore vehicleRepository;
    private final BookingRepository bookingRepository;

    public VehicleService(VehicleStore vehicleRepository) {
        this(vehicleRepository, null);
    }

    public VehicleService(VehicleStore vehicleRepository,
                          BookingRepository bookingRepository) {
        this.vehicleRepository = vehicleRepository;
        this.bookingRepository = bookingRepository;
    }

    public synchronized Vehicle createVehicle(
            String registrationNumber,
            String make,
            String model,
            com.drivingschool.vehicle.model.TransmissionType transmissionType,
            com.drivingschool.vehicle.model.VehicleCategory vehicleCategory,
            com.drivingschool.vehicle.model.VehicleOperationalStatus operationalStatus) {
        return createVehicle(registrationNumber, make, model, transmissionType,
                vehicleCategory, operationalStatus, null, null, null);
    }

    public synchronized Vehicle createVehicle(
            String registrationNumber, String make, String model,
            com.drivingschool.vehicle.model.TransmissionType transmissionType,
            com.drivingschool.vehicle.model.VehicleCategory vehicleCategory,
            com.drivingschool.vehicle.model.VehicleOperationalStatus operationalStatus,
            java.time.LocalDate insuranceExpiry, java.time.LocalDate lastServiceDate,
            java.time.LocalDate nextServiceDate) {

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
        vehicle.setInsuranceExpiry(insuranceExpiry);
        vehicle.setLastServiceDate(lastServiceDate);
        vehicle.setNextServiceDate(nextServiceDate);

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

    public Vehicle getVehicleByIdStrict(String id) {
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

        return vehicleRepository.findByIdStrict(id)
                .orElseThrow(() ->
                        new RecordNotFoundException("Vehicle not found: " + id)
                );
    }

    public synchronized Vehicle updateVehicle(
            String id,
            String registrationNumber,
            String make,
            String model) {
        Vehicle existing = vehicleRepository.findByIdStrict(id)
                .orElseThrow(() -> new RecordNotFoundException("Vehicle not found: " + id));
        return updateVehicle(id, registrationNumber, make, model, existing.getTransmissionType(),
                existing.getVehicleCategory(), existing.getOperationalStatus(),
                existing.getInsuranceExpiry(), existing.getLastServiceDate(), existing.getNextServiceDate());
    }

    public synchronized Vehicle updateVehicle(String id, String registrationNumber,
            String make, String model,
            com.drivingschool.vehicle.model.TransmissionType transmissionType,
            com.drivingschool.vehicle.model.VehicleCategory vehicleCategory,
            com.drivingschool.vehicle.model.VehicleOperationalStatus operationalStatus,
            java.time.LocalDate insuranceExpiry, java.time.LocalDate lastServiceDate,
            java.time.LocalDate nextServiceDate) {

        Vehicle existingVehicle = vehicleRepository.findByIdStrict(id)
                .orElseThrow(() ->
                        new RecordNotFoundException(
                                "Vehicle not found: " + id
                        )
                );
        ensureRegistrationNumberIsUnique(
                registrationNumber,
                existingVehicle.getId()
        );

        Vehicle updatedVehicle = new Vehicle(
                existingVehicle.getId(),
                registrationNumber,
                make,
                model,
                transmissionType, vehicleCategory, operationalStatus
        );
        updatedVehicle.setInsuranceExpiry(insuranceExpiry);
        updatedVehicle.setLastServiceDate(lastServiceDate);
        updatedVehicle.setNextServiceDate(nextServiceDate);

        vehicleRepository.update(updatedVehicle);

        return updatedVehicle;
    }

    public synchronized void deleteVehicle(String id) {
        getVehicleByIdStrict(id);
        if (bookingRepository != null && bookingRepository.readAllStrict().stream()
                .anyMatch(booking -> id.equals(booking.getVehicleId()))) {
            throw new ValidationException(
                    "Vehicle cannot be deleted because it is referenced by a lesson record.");
        }
        vehicleRepository.delete(id);
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
