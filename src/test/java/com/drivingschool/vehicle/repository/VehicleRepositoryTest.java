package com.drivingschool.vehicle.repository;

import com.drivingschool.vehicle.model.TransmissionType;
import com.drivingschool.vehicle.model.Vehicle;
import com.drivingschool.vehicle.model.VehicleCategory;
import com.drivingschool.vehicle.model.VehicleOperationalStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleRepositoryTest {

    @TempDir
    Path tempDirectory;

    private ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    private Vehicle createVehicle(String id) {
        return new Vehicle(
                id,
                "CAB1234",
                "Toyota",
                "Aqua",
                TransmissionType.AUTOMATIC,
                VehicleCategory.CAR,
                VehicleOperationalStatus.IN_SERVICE
        );
    }

    @Test
    void shouldSaveAndReloadVehicleWithAllFieldsPreserved() {

        Vehicle vehicle = createVehicle("V001");

        VehicleRepository firstRepository =
                new VehicleRepository(tempDirectory, objectMapper());

        firstRepository.save(vehicle);

        VehicleRepository secondRepository =
                new VehicleRepository(tempDirectory, objectMapper());

        Vehicle loadedVehicle =
                secondRepository.findById("V001").orElseThrow();

        assertEquals("V001", loadedVehicle.getId());
        assertEquals("CAB1234", loadedVehicle.getRegistrationNumber());
        assertEquals("Toyota", loadedVehicle.getMake());
        assertEquals("Aqua", loadedVehicle.getModel());
        assertEquals(
                TransmissionType.AUTOMATIC,
                loadedVehicle.getTransmissionType()
        );
        assertEquals(
                VehicleCategory.CAR,
                loadedVehicle.getVehicleCategory()
        );
        assertEquals(
                VehicleOperationalStatus.IN_SERVICE,
                loadedVehicle.getOperationalStatus()
        );
    }

    @Test
    void shouldFindVehicleByIdAndReturnEmptyForMissingId() {

        VehicleRepository repository =
                new VehicleRepository(tempDirectory, objectMapper());

        Vehicle vehicle = createVehicle("V001");
        repository.save(vehicle);

        assertTrue(repository.findById("V001").isPresent());
        assertTrue(repository.findById("V999").isEmpty());
    }

    @Test
    void shouldUpdateVehicleAndReloadChanges() {

        VehicleRepository firstRepository =
                new VehicleRepository(tempDirectory, objectMapper());

        Vehicle vehicle = createVehicle("V001");
        firstRepository.save(vehicle);

        vehicle.setMake("Honda");
        vehicle.setModel("Fit");
        vehicle.setTransmissionType(TransmissionType.MANUAL);
        vehicle.setVehicleCategory(VehicleCategory.MOTORCYCLE);
        vehicle.setOperationalStatus(VehicleOperationalStatus.MAINTENANCE);

        firstRepository.update(vehicle);

        VehicleRepository secondRepository =
                new VehicleRepository(tempDirectory, objectMapper());

        Vehicle loadedVehicle =
                secondRepository.findById("V001").orElseThrow();

        assertEquals("Honda", loadedVehicle.getMake());
        assertEquals("Fit", loadedVehicle.getModel());
        assertEquals(
                TransmissionType.MANUAL,
                loadedVehicle.getTransmissionType()
        );
        assertEquals(
                VehicleCategory.MOTORCYCLE,
                loadedVehicle.getVehicleCategory()
        );
        assertEquals(
                VehicleOperationalStatus.MAINTENANCE,
                loadedVehicle.getOperationalStatus()
        );
    }

    @Test
    void shouldDeleteVehicleAndConfirmRemovalAfterReopening() {

        VehicleRepository firstRepository =
                new VehicleRepository(tempDirectory, objectMapper());

        Vehicle vehicle = createVehicle("V001");
        firstRepository.save(vehicle);

        firstRepository.delete("V001");

        VehicleRepository secondRepository =
                new VehicleRepository(tempDirectory, objectMapper());

        assertTrue(secondRepository.findById("V001").isEmpty());
    }

    @Test
    void shouldContinueVPrefixedIdAllocationAcrossRepositoryInstances() {

        VehicleRepository firstRepository =
                new VehicleRepository(tempDirectory, objectMapper());

        String firstId = firstRepository.nextId("V");

        Vehicle vehicle = createVehicle(firstId);
        firstRepository.save(vehicle);

        VehicleRepository secondRepository =
                new VehicleRepository(tempDirectory, objectMapper());

        String secondId = secondRepository.nextId("V");

        assertEquals("V001", firstId);
        assertEquals("V002", secondId);
    }

    @Test
    void shouldStoreOneJsonObjectPerLine() throws Exception {

        VehicleRepository repository =
                new VehicleRepository(tempDirectory, objectMapper());

        repository.save(createVehicle("V001"));
        repository.save(createVehicle("V002"));

        Path vehiclesFile = tempDirectory.resolve("vehicles.txt");

        java.util.List<String> lines = Files.readAllLines(vehiclesFile);

        assertEquals(2, lines.size());

        ObjectMapper mapper = objectMapper();

        for (String line : lines) {
            assertTrue(mapper.readTree(line).isObject());
        }
    }


}