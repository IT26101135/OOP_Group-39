/**
============================================================
MEMBER   : IT26101132 - [Name]
MODULE   : Vehicles and Sample Data
FILE     : VehicleLookup.java
WHAT IT DOES (simple words): Keeps vehicles and sample data details clear and safe for the school.
REAL-LIFE EXAMPLE: Like a fleet logbook showing which teaching cars need attention.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for vehicles
============================================================
*/
package com.drivingschool.vehicle.api;

import java.util.Optional;

// SOLID: I - Scheduling receives only its one lookup operation.
public interface VehicleLookup {
    // CRUD: READ   - finds or lists vehicles
    // Shares only the vehicle details needed by lesson scheduling.
    Optional<VehicleBookingInfo> findBookingInfo(String vehicleId);
}