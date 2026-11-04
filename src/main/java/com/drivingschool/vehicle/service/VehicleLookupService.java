package com.drivingschool.vehicle.service;

import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.vehicle.api.VehicleBookingInfo;
import com.drivingschool.vehicle.api.VehicleLookup;
import com.drivingschool.vehicle.model.VehicleOperationalStatus;
import com.drivingschool.vehicle.repository.VehicleRepository;

import java.util.Optional;

public class VehicleLookupService implements VehicleLookup {

    private final VehicleRepository vehicleRepository;

    public VehicleLookupService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public Optional<VehicleBookingInfo> findBookingInfo(String vehicleId) {
        validateVehicleId(vehicleId);

        return vehicleRepository.findByIdStrict(vehicleId)
                .map(vehicle -> new VehicleBookingInfo(
                        vehicle.getId(),
                        vehicle.getVehicleCategory(),
                        vehicle.getTransmissionType(),
                        vehicle.getOperationalStatus()
                                == VehicleOperationalStatus.IN_SERVICE
                ));
    }

    private void validateVehicleId(String vehicleId) {
        if (vehicleId == null
                || !vehicleId.matches("V(?!000)[0-9]{3}")) {

            throw new ValidationException(
                    "Vehicle ID must be between V001 and V999."
            );
        }
    }
}