/**
============================================================
MEMBER   : IT26101132 - [Name]
MODULE   : Vehicles and Sample Data
FILE     : VehicleRepository.java
WHAT IT DOES (simple words): Saves vehicle records using the shared file storage.
REAL-LIFE EXAMPLE: Like a fleet logbook showing which teaching cars need attention.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for vehicles
============================================================
*/
package com.drivingschool.vehicle.repository;

import com.drivingschool.common.repository.FileRepository;
import com.drivingschool.vehicle.api.VehicleStore;
import com.drivingschool.vehicle.model.Vehicle;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;

// OOP: INHERITANCE - VehicleRepository reuses safe file storage from FileRepository.
// SOLID: O - A new record type can select its own file without rewriting shared storage.
// SOLID: L - VehicleRepository keeps the storage contract expected by VehicleStore callers.
public class VehicleRepository extends FileRepository<Vehicle> implements VehicleStore {

    // Receives the details this class needs and keeps them ready for its own job.
    public VehicleRepository(Path dataDirectory, ObjectMapper objectMapper) {
        super(
                dataDirectory,
                "vehicles.txt",
                Vehicle.class,
                objectMapper
        );
    }

    // CRUD: CREATE - adds a new vehicle and saves it
    // OOP: POLYMORPHISM - The fleet repository uses the parent's safe file writer.
    // Adds the new record through the shared safe file writer.
    @Override
    public void save(Vehicle vehicle) { super.save(vehicle); }

    // CRUD: READ   - finds or lists vehicles
    // Reads all records and refuses damaged data before a safety decision.
    @Override
    public java.util.List<Vehicle> readAllStrict() { return super.readAllStrict(); }

    // CRUD: UPDATE - changes an existing vehicle
    // Replaces an existing record after service checks have passed.
    @Override
    public void update(Vehicle vehicle) { super.update(vehicle); }

    // CRUD: DELETE - removes a vehicle
    // Removes the chosen record after service checks have passed.
    @Override
    public void delete(String id) { super.delete(id); }
}
