package com.drivingschool.vehicle.service;


import com.drivingschool.common.exception.StorageException;

import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.vehicle.model.TransmissionType;
import com.drivingschool.vehicle.model.Vehicle;
import com.drivingschool.vehicle.model.VehicleCategory;
import com.drivingschool.vehicle.model.VehicleOperationalStatus;
import com.drivingschool.vehicle.repository.VehicleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VehicleServiceTest {

    @TempDir
    Path tempDirectory;

    @Test
    void shouldRejectDuplicateRegistrationAfterNormalization() {
        VehicleRepository repository =
                new VehicleRepository(tempDirectory, new ObjectMapper());

        VehicleService service = new VehicleService(repository);

        service.createVehicle(
                "CAB-1234",
                "Toyota",
                "Corolla",
                TransmissionType.MANUAL,
                VehicleCategory.CAR,
                VehicleOperationalStatus.IN_SERVICE
        );

        assertThrows(
                DuplicateRecordException.class,
                () -> service.createVehicle(
                        "CAB 1234",
                        "Honda",
                        "Civic",
                        TransmissionType.AUTOMATIC,
                        VehicleCategory.CAR,
                        VehicleOperationalStatus.IN_SERVICE
                )
        );
    }

    @Test
    void shouldRejectRegistrationBelongingToRetiredVehicle() {
        VehicleRepository repository =
                new VehicleRepository(tempDirectory, new ObjectMapper());

        VehicleService service = new VehicleService(repository);

        Vehicle retiredVehicle = service.createVehicle(
                "CAB-5678",
                "Toyota",
                "Corolla",
                TransmissionType.MANUAL,
                VehicleCategory.CAR,
                VehicleOperationalStatus.RETIRED
        );

        assertThrows(
                DuplicateRecordException.class,
                () -> service.createVehicle(
                        "CAB 5678",
                        "Honda",
                        "Civic",
                        TransmissionType.AUTOMATIC,
                        VehicleCategory.CAR,
                        VehicleOperationalStatus.IN_SERVICE
                )
        );
    }

    @Test
    void shouldAllowKeepingOwnRegistrationWhenUpdating() {
        VehicleRepository repository =
                new VehicleRepository(tempDirectory, new ObjectMapper());

        VehicleService service = new VehicleService(repository);

        Vehicle createdVehicle = service.createVehicle(
                "CAB-1111",
                "Toyota",
                "Corolla",
                TransmissionType.MANUAL,
                VehicleCategory.CAR,
                VehicleOperationalStatus.IN_SERVICE
        );

        Vehicle updatedVehicle = service.updateVehicle(
                createdVehicle.getId(),
                "CAB 1111",
                "Honda",
                "Civic"
        );

        assertEquals(
                createdVehicle.getId(),
                updatedVehicle.getId()
        );

        assertEquals(
                "CAB1111",
                updatedVehicle.getRegistrationNumber()
        );

        assertEquals(
                "Honda",
                updatedVehicle.getMake()
        );

        assertEquals(
                "Civic",
                updatedVehicle.getModel()
        );
    }

    @Test
    void shouldRejectAnotherVehiclesRegistrationWhenUpdating() {
        VehicleRepository repository =
                new VehicleRepository(tempDirectory, new ObjectMapper());

        VehicleService service = new VehicleService(repository);

        Vehicle firstVehicle = service.createVehicle(
                "CAB-1111",
                "Toyota",
                "Corolla",
                TransmissionType.MANUAL,
                VehicleCategory.CAR,
                VehicleOperationalStatus.IN_SERVICE
        );

        Vehicle secondVehicle = service.createVehicle(
                "CAB-2222",
                "Honda",
                "Civic",
                TransmissionType.AUTOMATIC,
                VehicleCategory.CAR,
                VehicleOperationalStatus.IN_SERVICE
        );

        assertThrows(
                DuplicateRecordException.class,
                () -> service.updateVehicle(
                        secondVehicle.getId(),
                        "CAB 1111",
                        "Nissan",
                        "Sunny"
                )
        );

        Vehicle unchangedVehicle =
                service.getVehicleById(secondVehicle.getId());

        assertEquals(
                "CAB2222",
                unchangedVehicle.getRegistrationNumber()
        );

        assertEquals(
                "Honda",
                unchangedVehicle.getMake()
        );

        assertEquals(
                "Civic",
                unchangedVehicle.getModel()
        );
    }

    @Test
    void shouldLeaveVehicleUnchangedWhenUpdateIsInvalid() {
        VehicleRepository repository =
                new VehicleRepository(tempDirectory, new ObjectMapper());

        VehicleService service = new VehicleService(repository);

        Vehicle createdVehicle = service.createVehicle(
                "CAB-3333",
                "Toyota",
                "Corolla",
                TransmissionType.MANUAL,
                VehicleCategory.CAR,
                VehicleOperationalStatus.IN_SERVICE
        );

        assertThrows(
                ValidationException.class,
                () -> service.updateVehicle(
                        createdVehicle.getId(),
                        "CAB-3333",
                        "",
                        "Civic"
                )
        );

        Vehicle unchangedVehicle =
                service.getVehicleById(createdVehicle.getId());

        assertEquals(
                "CAB3333",
                unchangedVehicle.getRegistrationNumber()
        );

        assertEquals(
                "Toyota",
                unchangedVehicle.getMake()
        );

        assertEquals(
                "Corolla",
                unchangedVehicle.getModel()
        );
    }

    @Test
    void shouldNotWriteWhenStrictReadFailsDuringCreate() throws Exception {
        VehicleRepository repository =
                new VehicleRepository(tempDirectory, new ObjectMapper());

        VehicleService service = new VehicleService(repository);

        service.createVehicle(
                "CAB-4444",
                "Toyota",
                "Corolla",
                TransmissionType.MANUAL,
                VehicleCategory.CAR,
                VehicleOperationalStatus.IN_SERVICE
        );

        Path vehiclesFile = tempDirectory.resolve("vehicles.txt");

        Files.writeString(
                vehiclesFile,
                "{malformed-json}\n",
                StandardOpenOption.APPEND
        );

        String contentsBeforeOperation =
                Files.readString(vehiclesFile);

        assertThrows(
                StorageException.class,
                () -> service.createVehicle(
                        "CAB-5555",
                        "Honda",
                        "Civic",
                        TransmissionType.AUTOMATIC,
                        VehicleCategory.CAR,
                        VehicleOperationalStatus.IN_SERVICE
                )
        );

        String contentsAfterOperation =
                Files.readString(vehiclesFile);

        assertEquals(
                contentsBeforeOperation,
                contentsAfterOperation
        );
    }

    @Test
    void shouldNotWriteWhenStrictReadFailsDuringUpdate() throws Exception {
        VehicleRepository repository =
                new VehicleRepository(tempDirectory, new ObjectMapper());

        VehicleService service = new VehicleService(repository);

        Vehicle createdVehicle = service.createVehicle(
                "CAB-6666",
                "Toyota",
                "Corolla",
                TransmissionType.MANUAL,
                VehicleCategory.CAR,
                VehicleOperationalStatus.IN_SERVICE
        );

        Path vehiclesFile = tempDirectory.resolve("vehicles.txt");

        Files.writeString(
                vehiclesFile,
                "{malformed-json}\n",
                StandardOpenOption.APPEND
        );

        String contentsBeforeOperation =
                Files.readString(vehiclesFile);

        assertThrows(
                StorageException.class,
                () -> service.updateVehicle(
                        createdVehicle.getId(),
                        "CAB-7777",
                        "Honda",
                        "Civic"
                )
        );

        String contentsAfterOperation =
                Files.readString(vehiclesFile);

        assertEquals(
                contentsBeforeOperation,
                contentsAfterOperation
        );
    }
}