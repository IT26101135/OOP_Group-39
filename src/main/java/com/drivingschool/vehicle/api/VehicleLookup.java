// Module owner: IT26101132
package com.drivingschool.vehicle.api;

import java.util.Optional;

public interface VehicleLookup {
    Optional<VehicleBookingInfo> findBookingInfo(String vehicleId);
}