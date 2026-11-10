/**
============================================================
MEMBER   : IT26101132 - [Name]
MODULE   : Vehicles and Sample Data
FILE     : VehicleConfig.java
WHAT IT DOES (simple words): Keeps vehicles and sample data details clear and safe for the school.
REAL-LIFE EXAMPLE: Like a fleet logbook showing which teaching cars need attention.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for vehicles
============================================================
*/
package com.drivingschool.vehicle.config;

import com.drivingschool.vehicle.repository.VehicleRepository;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.vehicle.api.VehicleStore;
import com.drivingschool.vehicle.service.VehicleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.ObjectProvider;
import java.nio.file.Path;

@Configuration
public class VehicleConfig {

    // Chooses the vehicle file inside the configured data directory.
    @Bean
    public VehicleRepository vehicleRepository(
            @Value("${app.data-directory:./data}") String dataDirectory,
            ObjectMapper objectMapper) {

        return new VehicleRepository(
                Path.of(dataDirectory),
                objectMapper
        );
    }

    // Connects fleet rules to storage and the optional lesson-history check.
    @Bean
    public VehicleService vehicleService(
            VehicleStore vehicleRepository,
            ObjectProvider<BookingRepository> bookingRepositoryProvider) {

        return new VehicleService(vehicleRepository,
                bookingRepositoryProvider.getIfAvailable());
    }
}
