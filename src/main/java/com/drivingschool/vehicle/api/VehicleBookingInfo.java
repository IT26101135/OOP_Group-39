/**
============================================================
MEMBER   : IT26101132 - [Name]
MODULE   : Vehicles and Sample Data
FILE     : VehicleBookingInfo.java
WHAT IT DOES (simple words): Keeps vehicles and sample data details clear and safe for the school.
REAL-LIFE EXAMPLE: Like a fleet logbook showing which teaching cars need attention.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for vehicles
============================================================
*/
package com.drivingschool.vehicle.api;

import com.drivingschool.vehicle.model.TransmissionType;
import com.drivingschool.vehicle.model.VehicleCategory;

// A record is a small read-only data card; Java generates its constructor and accessors.
public record VehicleBookingInfo(
        String id,
        VehicleCategory vehicleCategory,
        TransmissionType transmissionType,
        boolean operationallyEligible
) {
}