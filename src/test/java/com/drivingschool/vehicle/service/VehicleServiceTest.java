package com.drivingschool.vehicle.service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.drivingschool.common.exception.RecordNotFoundException;
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
    void shouldCreateAndPersistVehicleAfterReopeningRepository() {
        VehicleRepository repository =
                new VehicleRepository(tempDirectory, new ObjectMapper());

        VehicleService service = new VehicleService(repository);

        Vehicle createdVehicle = service.createVehicle(
                "CAB-9000",
                "Toyota",
                "Corolla",
                TransmissionType.MANUAL,
                VehicleCategory.CAR,
                VehicleOperationalStatus.IN_SERVICE
        );

        VehicleRepository reopenedRepository =
                new VehicleRepository(tempDirectory, new ObjectMapper());

        Vehicle persistedVehicle =
                reopenedRepository.findByIdStrict(createdVehicle.getId())
                        .orElseThrow();

        assertEquals(createdVehicle.getId(), persistedVehicle.getId());
        assertEquals("CAB9000", persistedVehicle.getRegistrationNumber());
        assertEquals("Toyota", persistedVehicle.getMake());
        assertEquals("Corolla", persistedVehicle.getModel());
        assertEquals(
                TransmissionType.MANUAL,
                persistedVehicle.getTransmissionType()
        );
        assertEquals(
                VehicleCategory.CAR,
                persistedVehicle.getVehicleCategory()
        );
        assertEquals(
                VehicleOperationalStatus.IN_SERVICE,
                persistedVehicle.getOperationalStatus()
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
    void shouldUpdateAndPersistVehicleAfterReopeningRepository() {
        VehicleRepository repository =
                new VehicleRepository(tempDirectory, new ObjectMapper());

        VehicleService service = new VehicleService(repository);

        Vehicle createdVehicle = service.createVehicle(
                "CAB-7000",
                "Toyota",
                "Corolla",
                TransmissionType.MANUAL,
                VehicleCategory.CAR,
                VehicleOperationalStatus.MAINTENANCE
        );

        Vehicle updatedVehicle = service.updateVehicle(
                createdVehicle.getId(),
                "CAB 7001",
                "Honda",
                "Civic"
        );

        assertEquals(createdVehicle.getId(), updatedVehicle.getId());

        VehicleRepository reopenedRepository =
                new VehicleRepository(tempDirectory, new ObjectMapper());

        Vehicle persistedVehicle =
                reopenedRepository.findByIdStrict(createdVehicle.getId())
                        .orElseThrow();

        assertEquals(createdVehicle.getId(), persistedVehicle.getId());
        assertEquals("CAB7001", persistedVehicle.getRegistrationNumber());
        assertEquals("Honda", persistedVehicle.getMake());
        assertEquals("Civic", persistedVehicle.getModel());

        assertEquals(
                TransmissionType.MANUAL,
                persistedVehicle.getTransmissionType()
        );

        assertEquals(
                VehicleCategory.CAR,
                persistedVehicle.getVehicleCategory()
        );

        assertEquals(
                VehicleOperationalStatus.MAINTENANCE,
                persistedVehicle.getOperationalStatus()
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

    @Test
    void shouldThrowRecordNotFoundWhenUpdatingMissingVehicle() {
        VehicleRepository repository =
                new VehicleRepository(tempDirectory, new ObjectMapper());

        VehicleService service = new VehicleService(repository);

        assertThrows(
                RecordNotFoundException.class,
                () -> service.updateVehicle(
                        "V999",
                        "CAB-9999",
                        "Toyota",
                        "Corolla"
                )
        );
    }

    @Test
    void shouldAllowOnlyOneConcurrentCreateForEquivalentRegistration() throws Exception {
        VehicleRepository repository =
                new VehicleRepository(tempDirectory, new ObjectMapper());

        VehicleService service = new VehicleService(repository);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        Future<Vehicle> successfulCreate = executor.submit(() -> {
            startLatch.await();

            return service.createVehicle(
                    "CAB-8888",
                    "Toyota",
                    "Corolla",
                    TransmissionType.MANUAL,
                    VehicleCategory.CAR,
                    VehicleOperationalStatus.IN_SERVICE
            );
        });

        Future<Throwable> failedCreate = executor.submit(() -> {
            startLatch.await();

            try {
                service.createVehicle(
                        "CAB 8888",
                        "Honda",
                        "Civic",
                        TransmissionType.AUTOMATIC,
                        VehicleCategory.CAR,
                        VehicleOperationalStatus.IN_SERVICE
                );

                return null;
            } catch (Throwable exception) {
                return exception;
            }
        });

        startLatch.countDown();

        Vehicle createdVehicle = successfulCreate.get();
        Throwable exception = failedCreate.get();

        executor.shutdown();

        assertEquals("CAB8888", createdVehicle.getRegistrationNumber());
        assertEquals(DuplicateRecordException.class, exception.getClass());

        VehicleRepository reopenedRepository =
                new VehicleRepository(tempDirectory, new ObjectMapper());

        assertEquals(
                1,
                reopenedRepository.readAllStrict().stream()
                        .filter(vehicle ->
                                "CAB8888".equals(vehicle.getRegistrationNumber()))
                        .count()
        );
    }
}