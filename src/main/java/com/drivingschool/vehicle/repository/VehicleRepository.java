package com.drivingschool.vehicle.repository;

import com.drivingschool.common.repository.FileRepository;
import com.drivingschool.vehicle.model.Vehicle;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;

public class VehicleRepository extends FileRepository<Vehicle> {

    public VehicleRepository(Path dataDirectory, ObjectMapper objectMapper) {
        super(
                dataDirectory,
                "vehicles.txt",
                Vehicle.class,
                objectMapper
        );
    }
}