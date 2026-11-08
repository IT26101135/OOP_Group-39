// Module owner: IT26101132
package com.drivingschool.vehicle.config;

import com.drivingschool.vehicle.api.VehicleBookingInfo;
import com.drivingschool.vehicle.api.VehicleLookup;
import com.drivingschool.vehicle.model.TransmissionType;
import com.drivingschool.vehicle.model.Vehicle;
import com.drivingschool.vehicle.model.VehicleCategory;
import com.drivingschool.vehicle.model.VehicleOperationalStatus;
import com.drivingschool.vehicle.repository.VehicleRepository;
import com.drivingschool.vehicle.service.VehicleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VehicleConfigTest {

    @TempDir
    Path tempDirectory;

    @Test
    void shouldRegisterSingletonBeansAndPersistVehicle() {

        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext()) {

            context.getEnvironment()
                    .getPropertySources()
                    .addFirst(
                            new MapPropertySource(
                                    "testProperties",
                                    Map.of(
                                            "app.data-directory",
                                            tempDirectory.toString()
                                    )
                            )
                    );

            context.registerBean(
                    ObjectMapper.class,
                    () -> new ObjectMapper()
            );

            context.register(VehicleConfig.class);
            context.refresh();

            assertEquals(
                    1,
                    context.getBeansOfType(VehicleRepository.class).size()
            );

            assertEquals(
                    1,
                    context.getBeansOfType(VehicleService.class).size()
            );

            VehicleRepository repository =
                    context.getBean(VehicleRepository.class);

            VehicleService service =
                    context.getBean(VehicleService.class);

            assertSame(
                    repository,
                    context.getBean(VehicleRepository.class)
            );

            assertSame(
                    service,
                    context.getBean(VehicleService.class)
            );

            assertEquals(
                    1,
                    context.getBeansOfType(VehicleLookup.class).size()
            );

            VehicleLookup lookup = context.getBean(VehicleLookup.class);

            assertSame(
                    lookup,
                    context.getBean(VehicleLookup.class)
            );

            Vehicle createdVehicle = service.createVehicle(
                    "CAB-1234",
                    "Toyota",
                    "Aqua",
                    TransmissionType.AUTOMATIC,
                    VehicleCategory.CAR,
                    VehicleOperationalStatus.IN_SERVICE
            );

            VehicleBookingInfo bookingInfo =
                    lookup.findBookingInfo(createdVehicle.getId()).orElseThrow();

            assertEquals(createdVehicle.getId(), bookingInfo.id());
            assertEquals(VehicleCategory.CAR, bookingInfo.vehicleCategory());
            assertEquals(
                    TransmissionType.AUTOMATIC,
                    bookingInfo.transmissionType()
            );
            assertTrue(bookingInfo.operationallyEligible());

            VehicleRepository freshRepository =
                    new VehicleRepository(
                            tempDirectory,
                            context.getBean(ObjectMapper.class)
                    );

            Vehicle savedVehicle = freshRepository
                    .readAllStrict()
                    .stream()
                    .filter(vehicle ->
                            vehicle.getId().equals(createdVehicle.getId())
                    )
                    .findFirst()
                    .orElseThrow();

            assertEquals(
                    createdVehicle.getId(),
                    savedVehicle.getId()
            );

            assertEquals(
                    "CAB1234",
                    savedVehicle.getRegistrationNumber()
            );

            assertEquals("Toyota", savedVehicle.getMake());
            assertEquals("Aqua", savedVehicle.getModel());

            assertEquals(
                    TransmissionType.AUTOMATIC,
                    savedVehicle.getTransmissionType()
            );

            assertEquals(
                    VehicleCategory.CAR,
                    savedVehicle.getVehicleCategory()
            );

            assertEquals(
                    VehicleOperationalStatus.IN_SERVICE,
                    savedVehicle.getOperationalStatus()
            );
        }
    }
}