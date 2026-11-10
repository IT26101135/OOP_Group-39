/**
============================================================
MEMBER   : IT26101132 - [Name]
MODULE   : Vehicles and Sample Data
FILE     : VehicleService.java
WHAT IT DOES (simple words): Checks the school rules before saving a vehicle.
REAL-LIFE EXAMPLE: Like a fleet logbook showing which teaching cars need attention.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for vehicles
============================================================
*/
package com.drivingschool.vehicle.service;

import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.vehicle.model.Vehicle;
import com.drivingschool.vehicle.api.VehicleStore;
import com.drivingschool.booking.repository.BookingRepository;

import java.util.List;
import java.util.Optional;
import com.drivingschool.booking.model.Booking;
import com.drivingschool.vehicle.api.VehicleOperations;

// OOP: EXCEPTION HANDLING - Invalid or duplicate details throw friendly custom exceptions.
// SOLID: S - This class checks fleet rules; the repository handles files.
public class VehicleService implements VehicleOperations {

    private final VehicleStore vehicleRepository;
    private final BookingRepository bookingRepository;

    // Receives the details this class needs and keeps them ready for its own job.
    public VehicleService(VehicleStore vehicleRepository) {
        this(vehicleRepository, null);
    }

    // Receives the details this class needs and keeps them ready for its own job.
    public VehicleService(VehicleStore vehicleRepository,
                          BookingRepository bookingRepository) {
        this.vehicleRepository = vehicleRepository;
        this.bookingRepository = bookingRepository;
    }

    // Checks the new vehicle and prevents a duplicate registration before saving.
    // CRUD: CREATE - adds a new vehicle and saves it
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

    // Checks the new vehicle and prevents a duplicate registration before saving.
    // CRUD: CREATE - adds a new vehicle and saves it
    public synchronized Vehicle createVehicle(
            String registrationNumber, String make, String model,
            com.drivingschool.vehicle.model.TransmissionType transmissionType,
            com.drivingschool.vehicle.model.VehicleCategory vehicleCategory,
            com.drivingschool.vehicle.model.VehicleOperationalStatus operationalStatus,
            java.time.LocalDate insuranceExpiry, java.time.LocalDate lastServiceDate,
            java.time.LocalDate nextServiceDate) {

        // Step 1: Allocate an ID and check that the registration is not already in use.
        String id = vehicleRepository.nextId("V");

        ensureRegistrationNumberIsUnique(registrationNumber, null);

        // Step 2: Build a validated vehicle and check its maintenance dates.
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

        // Step 3: Save only after every detail has passed its checks.
        vehicleRepository.save(vehicle);

        return vehicle;
    }

    // Lists saved fleet records for the administrator.
    // CRUD: READ   - finds or lists vehicles
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.readAll();
    }

    // Finds a vehicle or explains why the record is missing.
    // CRUD: READ   - finds or lists vehicles
    public Vehicle getVehicleById(String id) {
        return requireVehicle(vehicleRepository.findById(id), id);
    }

    // Checks the chosen ID and refuses damaged storage before editing.
    // CRUD: READ   - finds or lists vehicles
    public Vehicle getVehicleByIdStrict(String id) {
        if (id == null || !id.matches("V\\d{3}")) {
            throw new ValidationException(
                    "Choose a vehicle from the vehicle list and try again."
            );
        }

        int number = Integer.parseInt(id.substring(1));

        if (number < 1 || number > 999) {
            throw new ValidationException(
                    "Choose a vehicle from the vehicle list and try again."
            );
        }

        return requireVehicle(vehicleRepository.findByIdStrict(id), id);
    }

    // Checks a replacement record before changing the stored vehicle.
    // CRUD: UPDATE - changes an existing vehicle
    public synchronized Vehicle updateVehicle(
            String id,
            String registrationNumber,
            String make,
            String model) {
        Vehicle existing = getVehicleByIdStrict(id);
        return updateVehicle(id, registrationNumber, make, model, existing.getTransmissionType(),
                existing.getVehicleCategory(), existing.getOperationalStatus(),
                existing.getInsuranceExpiry(), existing.getLastServiceDate(), existing.getNextServiceDate());
    }

    // Checks a replacement record before changing the stored vehicle.
    // CRUD: UPDATE - changes an existing vehicle
    public synchronized Vehicle updateVehicle(String id, String registrationNumber,
            String make, String model,
            com.drivingschool.vehicle.model.TransmissionType transmissionType,
            com.drivingschool.vehicle.model.VehicleCategory vehicleCategory,
            com.drivingschool.vehicle.model.VehicleOperationalStatus operationalStatus,
            java.time.LocalDate insuranceExpiry, java.time.LocalDate lastServiceDate,
            java.time.LocalDate nextServiceDate) {

        // Step 1: Load the existing record and keep its permanent ID.
        Vehicle existingVehicle = getVehicleByIdStrict(id);
        ensureRegistrationNumberIsUnique(
                registrationNumber,
                existingVehicle.getId()
        );

        // Step 2: Validate a separate replacement so a rejected edit leaves the old record intact.
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

        // Step 3: Replace the file record only after identity and date checks pass.
        vehicleRepository.update(updatedVehicle);

        return updatedVehicle;
    }

    // Keeps lesson history intact and removes only an unreferenced vehicle.
    // CRUD: DELETE - removes a vehicle
    public synchronized void deleteVehicle(String id) {
        getVehicleByIdStrict(id);
        // Step 1: Keep vehicles that still explain a student's lesson history.
        if (bookingRepository != null) {
            for (Booking booking : bookingRepository.readAllStrict()) {
                if (id.equals(booking.getVehicleId())) {
                    throw new ValidationException(
                            "Vehicle cannot be deleted because it is referenced by a lesson record.");
                }
            }
        }
        // Step 2: Remove the vehicle only when no lesson refers to it.
        vehicleRepository.delete(id);
    }


    // Handles the ensure registration number is unique step for this page or record.
    private void ensureRegistrationNumberIsUnique(
            String registrationNumber,
            String currentVehicleId) {

        String normalizedRegistration =
                Vehicle.normalizeRegistrationNumber(registrationNumber);

        for (Vehicle vehicle : vehicleRepository.readAllStrict()) {
            boolean sameRegistration = normalizedRegistration.equals(vehicle.getRegistrationNumber());
            boolean anotherVehicle = !vehicle.getId().equals(currentVehicleId);
            if (sameRegistration && anotherVehicle) {
                throw new DuplicateRecordException("Registration number already exists: " + normalizedRegistration);
            }
        }
    }

    // Gives the caller a friendly error when the requested vehicle no longer exists.
    private Vehicle requireVehicle(Optional<Vehicle> result, String vehicleId) {
        if (result.isEmpty()) {
            throw new RecordNotFoundException("This vehicle is no longer listed. Return to the vehicle list.");
        }
        return result.get();
    }
}
