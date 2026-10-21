package com.drivingschool.vehicle.model;

import com.drivingschool.common.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VehicleTest {

    private Vehicle createValidVehicle() {
        return new Vehicle(
                "V001",
                "CAB-1234",
                "Toyota",
                "Aqua",
                TransmissionType.AUTOMATIC,
                VehicleCategory.CAR,
                VehicleOperationalStatus.IN_SERVICE
        );
    }

    @Test
    void shouldCreateValidVehicle() {

        Vehicle vehicle = createValidVehicle();

        assertEquals("V001", vehicle.getId());
        assertEquals("CAB1234", vehicle.getRegistrationNumber());
        assertEquals("Toyota", vehicle.getMake());
        assertEquals("Aqua", vehicle.getModel());
        assertEquals(
                TransmissionType.AUTOMATIC,
                vehicle.getTransmissionType()
        );
        assertEquals(VehicleCategory.CAR, vehicle.getVehicleCategory());
        assertEquals(
                VehicleOperationalStatus.IN_SERVICE,
                vehicle.getOperationalStatus()
        );
    }

    @Test
    void shouldAcceptV001AndV999() {

        Vehicle first = new Vehicle(
                "V001",
                "CAR123",
                "Toyota",
                "Aqua",
                TransmissionType.MANUAL,
                VehicleCategory.CAR,
                VehicleOperationalStatus.IN_SERVICE
        );

        Vehicle last = new Vehicle(
                "V999",
                "CAR124",
                "Honda",
                "Fit",
                TransmissionType.AUTOMATIC,
                VehicleCategory.CAR,
                VehicleOperationalStatus.IN_SERVICE
        );

        assertEquals("V001", first.getId());
        assertEquals("V999", last.getId());
    }

    @Test
    void shouldRejectV000() {

        assertThrows(
                ValidationException.class,
                () -> new Vehicle(
                        "V000",
                        "CAR123",
                        "Toyota",
                        "Aqua",
                        TransmissionType.MANUAL,
                        VehicleCategory.CAR,
                        VehicleOperationalStatus.IN_SERVICE
                )
        );
    }

    @Test
    void shouldRejectMalformedVehicleId() {

        assertThrows(
                ValidationException.class,
                () -> new Vehicle(
                        "V01",
                        "CAR123",
                        "Toyota",
                        "Aqua",
                        TransmissionType.MANUAL,
                        VehicleCategory.CAR,
                        VehicleOperationalStatus.IN_SERVICE
                )
        );
    }

    @Test
    void shouldNormalizeRegistrationNumber() {

        assertEquals(
                "CAB1234",
                Vehicle.normalizeRegistrationNumber(" cab-1234 ")
        );

        assertEquals(
                "CAB1234",
                Vehicle.normalizeRegistrationNumber("CAB 1234")
        );

        assertEquals(
                "CAB1234",
                Vehicle.normalizeRegistrationNumber("cab1234")
        );
    }

    @Test
    void shouldRejectBlankRegistrationNumber() {

        assertThrows(
                ValidationException.class,
                () -> Vehicle.normalizeRegistrationNumber("   ")
        );
    }

    @Test
    void shouldRejectRegistrationWithOnlyLetters() {

        assertThrows(
                ValidationException.class,
                () -> Vehicle.normalizeRegistrationNumber("ABCDEFG")
        );
    }

    @Test
    void shouldRejectRegistrationWithOnlyDigits() {

        assertThrows(
                ValidationException.class,
                () -> Vehicle.normalizeRegistrationNumber("123456")
        );
    }

    @Test
    void shouldRejectRegistrationWithInvalidCharacters() {

        assertThrows(
                ValidationException.class,
                () -> Vehicle.normalizeRegistrationNumber("ABC@123")
        );
    }

    @Test
    void shouldRejectRegistrationOutsideLengthRange() {

        assertThrows(
                ValidationException.class,
                () -> Vehicle.normalizeRegistrationNumber("A1")
        );

        assertThrows(
                ValidationException.class,
                () -> Vehicle.normalizeRegistrationNumber(
                        "ABCDEFGHIJKL123"
                )
        );
    }

    @Test
    void shouldTrimMakeAndModel() {

        Vehicle vehicle = new Vehicle(
                "V001",
                "CAR123",
                "  Toyota  ",
                "  Aqua  ",
                TransmissionType.MANUAL,
                VehicleCategory.CAR,
                VehicleOperationalStatus.IN_SERVICE
        );

        assertEquals("Toyota", vehicle.getMake());
        assertEquals("Aqua", vehicle.getModel());
    }

    @Test
    void shouldAcceptPunctuationInMakeAndModel() {

        Vehicle vehicle = new Vehicle(
                "V001",
                "CAR123",
                "Mercedes-Benz",
                "C-Class (2025)",
                TransmissionType.AUTOMATIC,
                VehicleCategory.CAR,
                VehicleOperationalStatus.IN_SERVICE
        );

        assertEquals("Mercedes-Benz", vehicle.getMake());
        assertEquals("C-Class (2025)", vehicle.getModel());
    }

    @Test
    void shouldRejectMakeLongerThan50Characters() {

        String longMake = "A".repeat(51);

        assertThrows(
                ValidationException.class,
                () -> new Vehicle(
                        "V001",
                        "CAR123",
                        longMake,
                        "Aqua",
                        TransmissionType.MANUAL,
                        VehicleCategory.CAR,
                        VehicleOperationalStatus.IN_SERVICE
                )
        );
    }

    @Test
    void shouldRejectNullEnums() {

        assertThrows(
                ValidationException.class,
                () -> new Vehicle(
                        "V001",
                        "CAR123",
                        "Toyota",
                        "Aqua",
                        null,
                        VehicleCategory.CAR,
                        VehicleOperationalStatus.IN_SERVICE
                )
        );

        assertThrows(
                ValidationException.class,
                () -> new Vehicle(
                        "V001",
                        "CAR123",
                        "Toyota",
                        "Aqua",
                        TransmissionType.MANUAL,
                        null,
                        VehicleOperationalStatus.IN_SERVICE
                )
        );

        assertThrows(
                ValidationException.class,
                () -> new Vehicle(
                        "V001",
                        "CAR123",
                        "Toyota",
                        "Aqua",
                        TransmissionType.MANUAL,
                        VehicleCategory.CAR,
                        null
                )
        );
    }

    @Test
    void shouldAllowEachOperationalStatus() {

        Vehicle maintenanceVehicle = new Vehicle(
                "V001",
                "CAR123",
                "Toyota",
                "Aqua",
                TransmissionType.AUTOMATIC,
                VehicleCategory.CAR,
                VehicleOperationalStatus.MAINTENANCE
        );

        Vehicle retiredVehicle = new Vehicle(
                "V002",
                "CAR124",
                "Honda",
                "Fit",
                TransmissionType.MANUAL,
                VehicleCategory.CAR,
                VehicleOperationalStatus.RETIRED
        );

        assertEquals(
                VehicleOperationalStatus.MAINTENANCE,
                maintenanceVehicle.getOperationalStatus()
        );

        assertEquals(
                VehicleOperationalStatus.RETIRED,
                retiredVehicle.getOperationalStatus()
        );
    }

    @Test
    void invalidUpdateShouldNotChangePreviousValue() {

        Vehicle vehicle = createValidVehicle();

        assertThrows(
                ValidationException.class,
                () -> vehicle.setMake("")
        );

        assertEquals("Toyota", vehicle.getMake());
    }

    @Test
    void invalidRegistrationUpdateShouldNotChangePreviousValue() {

        Vehicle vehicle = createValidVehicle();

        assertThrows(
                ValidationException.class,
                () -> vehicle.setRegistrationNumber("123456")
        );

        assertEquals(
                "CAB1234",
                vehicle.getRegistrationNumber()
        );
    }

    @Test
    void shouldPreserveAllFieldsThroughJsonRoundTrip()
            throws Exception {

        Vehicle original = createValidVehicle();

        ObjectMapper objectMapper = new ObjectMapper();

        String json = objectMapper.writeValueAsString(original);

        Vehicle restored = objectMapper.readValue(
                json,
                Vehicle.class
        );

        assertEquals(original.getId(), restored.getId());
        assertEquals(
                original.getRegistrationNumber(),
                restored.getRegistrationNumber()
        );
        assertEquals(original.getMake(), restored.getMake());
        assertEquals(original.getModel(), restored.getModel());
        assertEquals(
                original.getTransmissionType(),
                restored.getTransmissionType()
        );
        assertEquals(
                original.getVehicleCategory(),
                restored.getVehicleCategory()
        );
        assertEquals(
                original.getOperationalStatus(),
                restored.getOperationalStatus()
        );
    }

    @Test
    void shouldRejectInvalidVehicleDuringJsonDeserialization() {

        String invalidJson = """
                {
                  "id": "V000",
                  "registrationNumber": "CAB1234",
                  "make": "Toyota",
                  "model": "Aqua",
                  "transmissionType": "AUTOMATIC",
                  "vehicleCategory": "CAR",
                  "operationalStatus": "IN_SERVICE"
                }
                """;

        ObjectMapper objectMapper = new ObjectMapper();

        assertThrows(
                Exception.class,
                () -> objectMapper.readValue(
                        invalidJson,
                        Vehicle.class
                )
        );
    }

    @Test
    void shouldRejectInvalidEnumDuringJsonDeserialization() {

        String invalidJson = """
                {
                  "id": "V001",
                  "registrationNumber": "CAB1234",
                  "make": "Toyota",
                  "model": "Aqua",
                  "transmissionType": "SEMI_AUTO",
                  "vehicleCategory": "CAR",
                  "operationalStatus": "IN_SERVICE"
                }
                """;

        ObjectMapper objectMapper = new ObjectMapper();

        assertThrows(
                Exception.class,
                () -> objectMapper.readValue(
                        invalidJson,
                        Vehicle.class
                )
        );
    }
}
