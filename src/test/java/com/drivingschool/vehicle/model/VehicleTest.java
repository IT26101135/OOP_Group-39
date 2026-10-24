package com.drivingschool.vehicle.model;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
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
    void shouldRejectNullMake() {

        assertThrows(
                ValidationException.class,
                () -> new Vehicle(
                        "V001",
                        "CAR123",
                        null,
                        "Aqua",
                        TransmissionType.MANUAL,
                        VehicleCategory.CAR,
                        VehicleOperationalStatus.IN_SERVICE
                )
        );
    }

    @Test
    void shouldRejectBlankMake() {

        assertThrows(
                ValidationException.class,
                () -> new Vehicle(
                        "V001",
                        "CAR123",
                        "   ",
                        "Aqua",
                        TransmissionType.MANUAL,
                        VehicleCategory.CAR,
                        VehicleOperationalStatus.IN_SERVICE
                )
        );
    }

    @Test
    void shouldRejectNullModel() {

        assertThrows(
                ValidationException.class,
                () -> new Vehicle(
                        "V001",
                        "CAR123",
                        "Toyota",
                        null,
                        TransmissionType.MANUAL,
                        VehicleCategory.CAR,
                        VehicleOperationalStatus.IN_SERVICE
                )
        );
    }

    @Test
    void shouldRejectBlankModel() {

        assertThrows(
                ValidationException.class,
                () -> new Vehicle(
                        "V001",
                        "CAR123",
                        "Toyota",
                        "   ",
                        TransmissionType.MANUAL,
                        VehicleCategory.CAR,
                        VehicleOperationalStatus.IN_SERVICE
                )
        );
    }

    @Test
    void shouldAcceptMakeLengthOne() {

        Vehicle vehicle = new Vehicle(
                "V001",
                "CAR123",
                "A",
                "Aqua",
                TransmissionType.MANUAL,
                VehicleCategory.CAR,
                VehicleOperationalStatus.IN_SERVICE
        );

        assertEquals("A", vehicle.getMake());
    }

    @Test
    void shouldAcceptMakeLengthFifty() {

        String make = "A".repeat(50);

        Vehicle vehicle = new Vehicle(
                "V001",
                "CAR123",
                make,
                "Aqua",
                TransmissionType.MANUAL,
                VehicleCategory.CAR,
                VehicleOperationalStatus.IN_SERVICE
        );

        assertEquals(make, vehicle.getMake());
    }

    @Test
    void shouldRejectMakeLengthFiftyOne() {

        String make = "A".repeat(51);

        assertThrows(
                ValidationException.class,
                () -> new Vehicle(
                        "V001",
                        "CAR123",
                        make,
                        "Aqua",
                        TransmissionType.MANUAL,
                        VehicleCategory.CAR,
                        VehicleOperationalStatus.IN_SERVICE
                )
        );
    }

    @Test
    void shouldAcceptModelLengthOne() {

        Vehicle vehicle = new Vehicle(
                "V001",
                "CAR123",
                "Toyota",
                "A",
                TransmissionType.MANUAL,
                VehicleCategory.CAR,
                VehicleOperationalStatus.IN_SERVICE
        );

        assertEquals("A", vehicle.getModel());
    }

    @Test
    void shouldAcceptModelLengthFifty() {

        String model = "A".repeat(50);

        Vehicle vehicle = new Vehicle(
                "V001",
                "CAR123",
                "Toyota",
                model,
                TransmissionType.MANUAL,
                VehicleCategory.CAR,
                VehicleOperationalStatus.IN_SERVICE
        );

        assertEquals(model, vehicle.getModel());
    }

    @Test
    void shouldRejectModelLengthFiftyOne() {

        String model = "A".repeat(51);

        assertThrows(
                ValidationException.class,
                () -> new Vehicle(
                        "V001",
                        "CAR123",
                        "Toyota",
                        model,
                        TransmissionType.MANUAL,
                        VehicleCategory.CAR,
                        VehicleOperationalStatus.IN_SERVICE
                )
        );
    }

    @Test
    void shouldRejectJsonWithMissingOperationalStatus()
            throws Exception {

        String invalidJson = """
            {
              "id": "V001",
              "registrationNumber": "CAB1234",
              "make": "Toyota",
              "model": "Aqua",
              "transmissionType": "AUTOMATIC",
              "vehicleCategory": "CAR"
            }
            """;

        ObjectMapper objectMapper = new ObjectMapper();

        assertThrows(
                ValueInstantiationException.class,
                () -> objectMapper.readValue(
                        invalidJson,
                        Vehicle.class
                )
        );
    }

    @Test
    void shouldRejectJsonWithNullOperationalStatus()
            throws Exception {

        String invalidJson = """
            {
              "id": "V001",
              "registrationNumber": "CAB1234",
              "make": "Toyota",
              "model": "Aqua",
              "transmissionType": "AUTOMATIC",
              "vehicleCategory": "CAR",
              "operationalStatus": null
            }
            """;

        ObjectMapper objectMapper = new ObjectMapper();

        assertThrows(
                ValueInstantiationException.class,
                () -> objectMapper.readValue(
                        invalidJson,
                        Vehicle.class
                )
        );
    }

    @Test
    void shouldRejectJsonWithMissingId() throws Exception {

        String invalidJson = """
            {
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
                ValueInstantiationException.class,
                () -> objectMapper.readValue(invalidJson, Vehicle.class)
        );
    }

    @Test
    void shouldRejectJsonWithMissingRegistrationNumber() throws Exception {

        String invalidJson = """
            {
              "id": "V001",
              "make": "Toyota",
              "model": "Aqua",
              "transmissionType": "AUTOMATIC",
              "vehicleCategory": "CAR",
              "operationalStatus": "IN_SERVICE"
            }
            """;

        ObjectMapper objectMapper = new ObjectMapper();

        assertThrows(
                ValueInstantiationException.class,
                () -> objectMapper.readValue(invalidJson, Vehicle.class)
        );
    }

    @Test
    void shouldRejectJsonWithNullRegistrationNumber() throws Exception {

        String invalidJson = """
            {
              "id": "V001",
              "registrationNumber": null,
              "make": "Toyota",
              "model": "Aqua",
              "transmissionType": "AUTOMATIC",
              "vehicleCategory": "CAR",
              "operationalStatus": "IN_SERVICE"
            }
            """;

        ObjectMapper objectMapper = new ObjectMapper();

        assertThrows(
                ValueInstantiationException.class,
                () -> objectMapper.readValue(invalidJson, Vehicle.class)
        );
    }

    @Test
    void shouldRejectJsonWithMissingMake() throws Exception {

        String invalidJson = """
            {
              "id": "V001",
              "registrationNumber": "CAB1234",
              "model": "Aqua",
              "transmissionType": "AUTOMATIC",
              "vehicleCategory": "CAR",
              "operationalStatus": "IN_SERVICE"
            }
            """;

        ObjectMapper objectMapper = new ObjectMapper();

        assertThrows(
                ValueInstantiationException.class,
                () -> objectMapper.readValue(invalidJson, Vehicle.class)
        );
    }

    @Test
    void shouldRejectJsonWithNullMake() throws Exception {

        String invalidJson = """
            {
              "id": "V001",
              "registrationNumber": "CAB1234",
              "make": null,
              "model": "Aqua",
              "transmissionType": "AUTOMATIC",
              "vehicleCategory": "CAR",
              "operationalStatus": "IN_SERVICE"
            }
            """;

        ObjectMapper objectMapper = new ObjectMapper();

        assertThrows(
                ValueInstantiationException.class,
                () -> objectMapper.readValue(invalidJson, Vehicle.class)
        );
    }

    @Test
    void shouldRejectJsonWithMissingModel() throws Exception {

        String invalidJson = """
            {
              "id": "V001",
              "registrationNumber": "CAB1234",
              "make": "Toyota",
              "transmissionType": "AUTOMATIC",
              "vehicleCategory": "CAR",
              "operationalStatus": "IN_SERVICE"
            }
            """;

        ObjectMapper objectMapper = new ObjectMapper();

        assertThrows(
                ValueInstantiationException.class,
                () -> objectMapper.readValue(invalidJson, Vehicle.class)
        );
    }

    @Test
    void shouldRejectJsonWithNullModel() throws Exception {

        String invalidJson = """
            {
              "id": "V001",
              "registrationNumber": "CAB1234",
              "make": "Toyota",
              "model": null,
              "transmissionType": "AUTOMATIC",
              "vehicleCategory": "CAR",
              "operationalStatus": "IN_SERVICE"
            }
            """;

        ObjectMapper objectMapper = new ObjectMapper();

        assertThrows(
                ValueInstantiationException.class,
                () -> objectMapper.readValue(invalidJson, Vehicle.class)
        );
    }

    @Test
    void shouldRejectJsonWithMissingTransmissionType() throws Exception {

        String invalidJson = """
            {
              "id": "V001",
              "registrationNumber": "CAB1234",
              "make": "Toyota",
              "model": "Aqua",
              "vehicleCategory": "CAR",
              "operationalStatus": "IN_SERVICE"
            }
            """;

        ObjectMapper objectMapper = new ObjectMapper();

        assertThrows(
                ValueInstantiationException.class,
                () -> objectMapper.readValue(invalidJson, Vehicle.class)
        );
    }

    @Test
    void shouldRejectJsonWithNullTransmissionType() throws Exception {

        String invalidJson = """
            {
              "id": "V001",
              "registrationNumber": "CAB1234",
              "make": "Toyota",
              "model": "Aqua",
              "transmissionType": null,
              "vehicleCategory": "CAR",
              "operationalStatus": "IN_SERVICE"
            }
            """;

        ObjectMapper objectMapper = new ObjectMapper();

        assertThrows(
                ValueInstantiationException.class,
                () -> objectMapper.readValue(invalidJson, Vehicle.class)
        );
    }

    @Test
    void shouldRejectJsonWithMissingVehicleCategory() throws Exception {

        String invalidJson = """
            {
              "id": "V001",
              "registrationNumber": "CAB1234",
              "make": "Toyota",
              "model": "Aqua",
              "transmissionType": "AUTOMATIC",
              "operationalStatus": "IN_SERVICE"
            }
            """;

        ObjectMapper objectMapper = new ObjectMapper();

        assertThrows(
                ValueInstantiationException.class,
                () -> objectMapper.readValue(invalidJson, Vehicle.class)
        );
    }

    @Test
    void shouldRejectJsonWithNullVehicleCategory() throws Exception {

        String invalidJson = """
            {
              "id": "V001",
              "registrationNumber": "CAB1234",
              "make": "Toyota",
              "model": "Aqua",
              "transmissionType": "AUTOMATIC",
              "vehicleCategory": null,
              "operationalStatus": "IN_SERVICE"
            }
            """;

        ObjectMapper objectMapper = new ObjectMapper();

        assertThrows(
                ValueInstantiationException.class,
                () -> objectMapper.readValue(invalidJson, Vehicle.class)
        );
    }



    @Test
    void shouldRejectJsonWithNullId() throws Exception {

        String invalidJson = """
            {
              "id": null,
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
                ValueInstantiationException.class,
                () -> objectMapper.readValue(invalidJson, Vehicle.class)
        );
    }

    @Test
    void invalidModelUpdateShouldPreservePreviousValue() {

        Vehicle vehicle = createValidVehicle();

        assertThrows(
                ValidationException.class,
                () -> vehicle.setModel("")
        );

        assertEquals(
                "Aqua",
                vehicle.getModel()
        );
    }

    @Test
    void invalidTransmissionUpdateShouldPreservePreviousValue() {

        Vehicle vehicle = createValidVehicle();

        TransmissionType original =
                vehicle.getTransmissionType();

        assertThrows(
                ValidationException.class,
                () -> vehicle.setTransmissionType(null)
        );

        assertEquals(
                original,
                vehicle.getTransmissionType()
        );
    }

    @Test
    void invalidCategoryUpdateShouldPreservePreviousValue() {

        Vehicle vehicle = createValidVehicle();

        VehicleCategory original =
                vehicle.getVehicleCategory();

        assertThrows(
                ValidationException.class,
                () -> vehicle.setVehicleCategory(null)
        );

        assertEquals(
                original,
                vehicle.getVehicleCategory()
        );
    }

    @Test
    void invalidOperationalStatusUpdateShouldPreservePreviousValue() {

        Vehicle vehicle = createValidVehicle();

        VehicleOperationalStatus original =
                vehicle.getOperationalStatus();

        assertThrows(
                ValidationException.class,
                () -> vehicle.setOperationalStatus(null)
        );

        assertEquals(
                original,
                vehicle.getOperationalStatus()
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
    void shouldRejectInvalidVehicleDuringJsonDeserialization()
            throws Exception {

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

        ValueInstantiationException exception = assertThrows(
                ValueInstantiationException.class,
                () -> objectMapper.readValue(
                        invalidJson,
                        Vehicle.class
                )
        );

        assertInstanceOf(
                com.drivingschool.common.exception.ValidationException.class,
                exception.getCause()
        );
    }

    @Test
    void shouldRejectInvalidEnumDuringJsonDeserialization()
            throws Exception {

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
                InvalidFormatException.class,
                () -> objectMapper.readValue(
                        invalidJson,
                        Vehicle.class
                )
        );
    }

    @Test
    void shouldAcceptRegistrationLengthFour() {

        assertEquals(
                "A123",
                Vehicle.normalizeRegistrationNumber("A123")
        );
    }

    @Test
    void shouldAcceptRegistrationLengthTwelve() {

        assertEquals(
                "ABCDEF123456",
                Vehicle.normalizeRegistrationNumber("ABCDEF123456")
        );
    }

    @Test
    void shouldRejectRegistrationLengthThree() {

        assertThrows(
                ValidationException.class,
                () -> Vehicle.normalizeRegistrationNumber("A12")
        );
    }

    @Test
    void shouldRejectRegistrationLengthThirteen() {

        assertThrows(
                ValidationException.class,
                () -> Vehicle.normalizeRegistrationNumber("ABCDEFG1234567")
        );
    }


}
