// Module owner: IT26101132
package com.drivingschool.vehicle.repository;

import com.drivingschool.common.repository.FileRepository;
import com.drivingschool.vehicle.api.VehicleStore;
import com.drivingschool.vehicle.model.Vehicle;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;

public class VehicleRepository extends FileRepository<Vehicle> implements VehicleStore {

    public VehicleRepository(Path dataDirectory, ObjectMapper objectMapper) {
        super(
                dataDirectory,
                "vehicles.txt",
                Vehicle.class,
                objectMapper
        );
    }
}
