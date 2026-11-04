package com.drivingschool.vehicle.config;

import com.drivingschool.vehicle.repository.VehicleRepository;
import com.drivingschool.vehicle.service.VehicleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.drivingschool.vehicle.api.VehicleLookup;
import com.drivingschool.vehicle.service.VehicleLookupService;
import java.nio.file.Path;

@Configuration
public class VehicleConfig {

    @Bean
    public VehicleLookup vehicleLookup(
            VehicleRepository vehicleRepository) {

        return new VehicleLookupService(vehicleRepository);
    }

    @Bean
    public VehicleRepository vehicleRepository(
            @Value("${app.data-directory:./data}") String dataDirectory,
            ObjectMapper objectMapper) {

        return new VehicleRepository(
                Path.of(dataDirectory),
                objectMapper
        );
    }

    @Bean
    public VehicleService vehicleService(
            VehicleRepository vehicleRepository) {

        return new VehicleService(vehicleRepository);
    }
}