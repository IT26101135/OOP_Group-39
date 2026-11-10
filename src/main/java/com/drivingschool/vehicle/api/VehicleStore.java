/**
============================================================
MEMBER   : IT26101132 - [Name]
MODULE   : Vehicles and Sample Data
FILE     : VehicleStore.java
WHAT IT DOES (simple words): Keeps vehicles and sample data details clear and safe for the school.
REAL-LIFE EXAMPLE: Like a fleet logbook showing which teaching cars need attention.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for vehicles
============================================================
*/
package com.drivingschool.vehicle.api;

import com.drivingschool.vehicle.model.Vehicle;

import java.util.List;
import java.util.Optional;

/** The vehicle storage operations used by application services. */
public interface VehicleStore {

    // Asks storage for the next unused permanent record ID.
    String nextId(String prefix);

    // CRUD: CREATE - adds a new vehicle and saves it
    // Adds the new record after service validation.
    void save(Vehicle vehicle);

    // CRUD: READ   - finds or lists vehicles
    // Lists saved records for display.
    List<Vehicle> readAll();

    // CRUD: READ   - finds or lists vehicles
    // Reads the full file and refuses damaged data before making decisions.
    List<Vehicle> readAllStrict();

    // CRUD: READ   - finds or lists vehicles
    // Looks up one saved record by its permanent ID.
    Optional<Vehicle> findById(String id);

    // CRUD: READ   - finds or lists vehicles
    // Looks up one saved record without hiding file damage.
    Optional<Vehicle> findByIdStrict(String id);

    // CRUD: UPDATE - changes an existing vehicle
    // Replaces the saved record after service validation.
    void update(Vehicle vehicle);

    // CRUD: DELETE - removes a vehicle
    // Removes the saved record after service validation.
    void delete(String id);
}
