/**
============================================================
MEMBER   : IT26101132 - [Name]
MODULE   : Vehicles
FILE     : VehicleMaintenanceTest.java
WHAT IT DOES (simple words): Checks saved vehicle dates and fleet validation.
REAL-LIFE EXAMPLE: Checking a car logbook after someone corrects its details.
OOP USED : Encapsulation and exception handling
CRUD     : CREATE, READ, UPDATE, DELETE
============================================================
*/
package com.drivingschool.vehicle.service;

import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.vehicle.model.*;
import com.drivingschool.vehicle.repository.VehicleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class VehicleMaintenanceTest {
    @TempDir Path folder;

    // A complete fleet change must survive reopening the file, then allow safe deletion.
    @Test
    void datesSurviveCreateReadUpdateAndDelete() {
        VehicleService service = service();
        Vehicle vehicle = service.createVehicle("CAB1234", "Toyota", "Aqua", TransmissionType.AUTOMATIC,
                VehicleCategory.CAR, VehicleOperationalStatus.IN_SERVICE, LocalDate.now().plusDays(10),
                LocalDate.now().minusDays(10), LocalDate.now().plusDays(60));
        assertTrue(service().getVehicleByIdStrict(vehicle.getId()).isInsuranceWarning());
        service.updateVehicle(vehicle.getId(), "CAB1234", "Toyota", "Corolla");
        Vehicle saved = service().getVehicleByIdStrict(vehicle.getId());
        assertEquals("Corolla", saved.getModel());
        assertEquals(vehicle.getInsuranceExpiry(), saved.getInsuranceExpiry());
        service.deleteVehicle(vehicle.getId());
        assertThrows(RecordNotFoundException.class, () -> service().getVehicleByIdStrict(vehicle.getId()));
    }

    // An unknown renewal date must not pretend to be a known expiry or overdue date.
    @Test
    void missingDatesAreNotReportedAsExpired() {
        Vehicle vehicle = vehicle();
        assertNull(vehicle.getInsuranceExpiry());
        assertFalse(vehicle.isInsuranceExpired());
        assertFalse(vehicle.isServiceOverdue());
    }

    // The thirty-day warning includes today and the boundary, but excludes day thirty-one.
    @Test
    void renewalBoundaryIncludesThirtyDays() {
        Vehicle vehicle = vehicle();
        vehicle.setInsuranceExpiry(LocalDate.now().plusDays(30));
        assertTrue(vehicle.isInsuranceWarning());
        vehicle.setInsuranceExpiry(LocalDate.now().plusDays(31));
        assertFalse(vehicle.isInsuranceWarning());
        vehicle.setInsuranceExpiry(LocalDate.now());
        assertTrue(vehicle.isInsuranceWarning());
        assertFalse(vehicle.isInsuranceExpired());
    }

    // Past renewal and service dates get the stronger expired/overdue messages.
    @Test
    void pastDatesAreExpiredOrOverdue() {
        Vehicle vehicle = vehicle();
        vehicle.setInsuranceExpiry(LocalDate.now().minusDays(1));
        vehicle.setNextServiceDate(LocalDate.now().minusDays(1));
        assertTrue(vehicle.isInsuranceExpired());
        assertTrue(vehicle.isServiceOverdue());
    }

    // A failed date edit must leave the original vehicle in the file unchanged.
    @Test
    void invalidServiceDateDoesNotChangeStoredVehicle() {
        VehicleService service = service();
        Vehicle vehicle = service.createVehicle("CAB1234", "Toyota", "Aqua", TransmissionType.AUTOMATIC,
                VehicleCategory.CAR, VehicleOperationalStatus.IN_SERVICE);
        assertThrows(ValidationException.class, () -> service.updateVehicle(vehicle.getId(),
                "CAB1234", "Toyota", "Changed", TransmissionType.AUTOMATIC, VehicleCategory.CAR,
                VehicleOperationalStatus.IN_SERVICE, null, LocalDate.now().minusDays(1),
                LocalDate.now().minusDays(2)));
        assertEquals("Aqua", service().getVehicleByIdStrict(vehicle.getId()).getModel());
    }

    // Each service opens the same temporary file to check real persistence.
    private VehicleService service() {
        return new VehicleService(new VehicleRepository(folder, new ObjectMapper().findAndRegisterModules()));
    }

    // Makes a plain car whose dates can be checked independently of file storage.
    private Vehicle vehicle() {
        return new Vehicle("V001", "CAB1234", "Toyota", "Aqua", TransmissionType.AUTOMATIC,
                VehicleCategory.CAR, VehicleOperationalStatus.IN_SERVICE);
    }
}
