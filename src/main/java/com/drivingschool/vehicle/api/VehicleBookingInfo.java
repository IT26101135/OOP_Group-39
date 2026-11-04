package com.drivingschool.vehicle.api;

import com.drivingschool.vehicle.model.TransmissionType;
import com.drivingschool.vehicle.model.VehicleCategory;

public record VehicleBookingInfo(
        String id,
        VehicleCategory vehicleCategory,
        TransmissionType transmissionType,
        boolean operationallyEligible
) {
}