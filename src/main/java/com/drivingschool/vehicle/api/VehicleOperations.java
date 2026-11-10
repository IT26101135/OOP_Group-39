/**
============================================================
MEMBER   : IT26101132 - [Name]
MODULE   : Vehicles and Sample Data
FILE     : VehicleOperations.java
WHAT IT DOES (simple words): Lists the fleet tasks that the controller can ask for.
REAL-LIFE EXAMPLE: Like a fleet logbook showing which teaching cars need attention.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for vehicles
============================================================
*/
package com.drivingschool.vehicle.api;

import com.drivingschool.vehicle.model.*;
import java.time.LocalDate;
import java.util.List;

// OOP: ABSTRACTION - The web page asks for fleet tasks through this interface.
// SOLID: I - Only the five tasks needed by fleet management are exposed here.
public interface VehicleOperations {
    // CRUD: CREATE - adds a new vehicle and saves it
    // Checks identity and dates before adding a teaching vehicle.
    Vehicle createVehicle(String registrationNumber, String make, String model,
            TransmissionType transmissionType, VehicleCategory vehicleCategory,
            VehicleOperationalStatus operationalStatus, LocalDate insuranceExpiry,
            LocalDate lastServiceDate, LocalDate nextServiceDate);

    // CRUD: READ   - finds or lists vehicles
    // Supplies the saved fleet for the management cards.
    List<Vehicle> getAllVehicles();

    // CRUD: READ   - finds or lists vehicles
    // Loads a checked vehicle ID before the administrator edits its record.
    Vehicle getVehicleByIdStrict(String id);

    // CRUD: UPDATE - changes an existing vehicle
    // Validates replacement details before saving the fleet change.
    Vehicle updateVehicle(String id, String registrationNumber, String make, String model,
            TransmissionType transmissionType, VehicleCategory vehicleCategory,
            VehicleOperationalStatus operationalStatus, LocalDate insuranceExpiry,
            LocalDate lastServiceDate, LocalDate nextServiceDate);

    // CRUD: DELETE - removes a vehicle
    // Removes only a vehicle that no lesson history still refers to.
    void deleteVehicle(String id);
}
