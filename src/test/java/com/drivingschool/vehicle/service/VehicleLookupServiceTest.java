// Module owner: IT26101132
package com.drivingschool.vehicle.service;

import com.drivingschool.common.exception.StorageException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.vehicle.api.VehicleBookingInfo;
import com.drivingschool.vehicle.api.VehicleLookup;
import com.drivingschool.vehicle.model.TransmissionType;
import com.drivingschool.vehicle.model.Vehicle;
import com.drivingschool.vehicle.model.VehicleCategory;
import com.drivingschool.vehicle.model.VehicleOperationalStatus;
import com.drivingschool.vehicle.repository.VehicleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class VehicleLookupServiceTest {

    @TempDir
    Path tempDir;

    private VehicleRepository repository;
    private VehicleLookup lookup;

    @BeforeEach
    void setUp() {
        repository = new VehicleRepository(
                tempDir,
                new ObjectMapper()
        );

        lookup = new VehicleLookupService(repository);
    }

    private Vehicle vehicle(
            String id,
            VehicleOperationalStatus status) {

        return new Vehicle(
                id,
                "CAB-1234",
                "Toyota",
                "Aqua",
                TransmissionType.AUTOMATIC,
                VehicleCategory.CAR,
                status
        );
    }

    @Test
    void findsEligibleVehicleThroughInterface() {
        repository.save(
                vehicle("V001", VehicleOperationalStatus.IN_SERVICE)
        );

        Optional<VehicleBookingInfo> result =
                lookup.findBookingInfo("V001");

        assertTrue(result.isPresent());

        VehicleBookingInfo info = result.orElseThrow();

        assertEquals("V001", info.id());
        assertEquals(VehicleCategory.CAR, info.vehicleCategory());
        assertEquals(TransmissionType.AUTOMATIC, info.transmissionType());
        assertTrue(info.operationallyEligible());
    }

    @Test
    void maintenanceVehicleIsReturnedAsNotEligible() {
        repository.save(
                vehicle("V001", VehicleOperationalStatus.MAINTENANCE)
        );

        VehicleBookingInfo info =
                lookup.findBookingInfo("V001").orElseThrow();

        assertFalse(info.operationallyEligible());
    }

    @Test
    void retiredVehicleIsReturnedAsNotEligible() {
        repository.save(
                vehicle("V001", VehicleOperationalStatus.RETIRED)
        );

        VehicleBookingInfo info =
                lookup.findBookingInfo("V001").orElseThrow();

        assertFalse(info.operationallyEligible());
    }

    @Test
    void validMissingVehicleReturnsEmpty() {
        assertTrue(lookup.findBookingInfo("V999").isEmpty());
    }

    @Test
    void rejectsInvalidVehicleIds() {
        for (String invalid : List.of(
                "",
                " ",
                "V000",
                "V1000",
                "v001",
                "V01",
                " V001",
                "V001 ",
                "A001"
        )) {
            assertThrows(
                    ValidationException.class,
                    () -> lookup.findBookingInfo(invalid),
                    "Expected invalid ID to be rejected: " + invalid
            );
        }

        assertThrows(
                ValidationException.class,
                () -> lookup.findBookingInfo(null)
        );
    }

    @Test
    void corruptedStoragePropagatesFailure() throws Exception {
        repository.save(
                vehicle("V001", VehicleOperationalStatus.IN_SERVICE)
        );

        Path records = tempDir.resolve("vehicles.txt");

        Files.writeString(
                records,
                "invalid-json\n",
                StandardOpenOption.APPEND
        );

        String before = Files.readString(records);

        assertThrows(
                StorageException.class,
                () -> lookup.findBookingInfo("V001")
        );

        assertThrows(
                StorageException.class,
                () -> lookup.findBookingInfo("V999")
        );

        assertEquals(before, Files.readString(records));
    }
}