/**
============================================================
MEMBER   : IT26101132 - [Name]
MODULE   : Vehicles and Sample Data
FILE     : VehicleLookupService.java
WHAT IT DOES (simple words): Shares only the vehicle details needed to schedule a lesson.
REAL-LIFE EXAMPLE: Like a fleet logbook showing which teaching cars need attention.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for vehicles
============================================================
*/
package com.drivingschool.vehicle.service;

import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.vehicle.api.VehicleBookingInfo;
import com.drivingschool.vehicle.api.VehicleLookup;
import com.drivingschool.vehicle.model.VehicleOperationalStatus;
import com.drivingschool.vehicle.api.VehicleStore;

import java.util.Optional;

public class VehicleLookupService implements VehicleLookup {

    private final VehicleStore vehicleRepository;

    // Receives the details this class needs and keeps them ready for its own job.
    public VehicleLookupService(VehicleStore vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    // Shares a small read-only snapshot so scheduling does not edit fleet details.
    @Override
    public Optional<VehicleBookingInfo> findBookingInfo(String vehicleId) {
        validateVehicleId(vehicleId);

        Optional<com.drivingschool.vehicle.model.Vehicle> result = vehicleRepository.findByIdStrict(vehicleId);
        if (result.isEmpty()) {
            return Optional.empty();
        }
        com.drivingschool.vehicle.model.Vehicle vehicle = result.get();
        boolean ready = vehicle.getOperationalStatus() == VehicleOperationalStatus.IN_SERVICE;
        return Optional.of(new VehicleBookingInfo(vehicle.getId(), vehicle.getVehicleCategory(),
                vehicle.getTransmissionType(), ready));
    }

    // Handles the validate vehicle id step for this page or record.
    private void validateVehicleId(String vehicleId) {
        if (vehicleId == null
                || !vehicleId.matches("V(?!000)[0-9]{3}")) {

            throw new ValidationException(
                    "Vehicle ID must be between V001 and V999."
            );
        }
    }
}
