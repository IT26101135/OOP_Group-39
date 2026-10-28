package com.drivingschool.auth.model;

import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.student.model.Student;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserModelTest {

    // Format-only fixture. This is not a usable account credential.
    private static final String HASH =
            "$2a$10$" + "A".repeat(53);

    private final ObjectMapper mapper = new ObjectMapper();

    private Student student() {
        return new Student(
                "S001",
                "Nimal Perera",
                "nimal@example.com",
                HASH,
                "+94771234567"
        );
    }

    private Admin admin() {
        return new Admin(
                "A001",
                "System Admin",
                "admin@example.com",
                HASH
        );
    }

    @Test
    void studentNormalizesContactDetails() {
        Student student = new Student(
                "S001",
                "  Nimal Perera  ",
                "  NIMAL@EXAMPLE.COM  ",
                HASH,
                "  +94771234567  "
        );

        assertEquals("S001", student.getId());
        assertEquals("Nimal Perera", student.getName());
        assertEquals("nimal@example.com", student.getEmail());
        assertEquals("+94771234567", student.getPhone());
        assertEquals(HASH, student.getPasswordHash());
    }

    @Test
    void subclassesProvideTheirOwnBehaviourThroughUserReferences() {
        User student = student();
        User admin = admin();

        assertEquals(Role.STUDENT, student.getRole());
        assertEquals("/student/profile", student.getDashboardPath());

        assertEquals(Role.ADMIN, admin.getRole());
        assertEquals("/admin/dashboard", admin.getDashboardPath());
    }

    @Test
    void rejectsInvalidStudentIds() {
        for (String id : List.of(
                "S000", "S1000", "s001", "A001", "S01", " S001")) {

            assertThrows(ValidationException.class, () ->
                    new Student(
                            id, "Nimal Perera", "nimal@example.com",
                            HASH, "0771234567"
                    )
            );
        }

        assertThrows(ValidationException.class, () ->
                new Student(
                        null, "Nimal Perera", "nimal@example.com",
                        HASH, "0771234567"
                )
        );
    }

    @Test
    void validatesAdminIdPrefixAndUpperBoundary() {
        assertThrows(ValidationException.class, () ->
                new Admin(
                        "S001", "System Admin", "admin@example.com", HASH
                )
        );

        assertThrows(ValidationException.class, () ->
                new Admin(
                        "A000", "System Admin", "admin@example.com", HASH
                )
        );

        assertEquals("A999", new Admin(
                "A999", "System Admin", "admin@example.com", HASH
        ).getId());

        assertEquals("S999", new Student(
                "S999", "Nimal Perera", "nimal@example.com",
                HASH, "0771234567"
        ).getId());
    }

    @Test
    void validatesNameBoundariesAndPreservesPreviousValueOnFailure() {
        Student student = student();

        student.setName("Ab");
        student.setName("N".repeat(100));

        for (String invalid : List.of("", " ", "A", "N".repeat(101))) {
            assertThrows(
                    ValidationException.class,
                    () -> student.setName(invalid)
            );

            assertEquals("N".repeat(100), student.getName());
        }

        assertThrows(
                ValidationException.class,
                () -> student.setName(null)
        );

        assertEquals("N".repeat(100), student.getName());
    }

    @Test
    void rejectsInvalidEmailsWithoutChangingPreviousEmail() {
        Student student = student();

        for (String email : List.of(
                "",
                " ",
                "missing-at.example.com",
                "nimal@",
                "nimal@example",
                "nimal@@example.com",
                "ni mal@example.com",
                ".nimal@example.com",
                "nimal.@example.com",
                "ni..mal@example.com",
                "x".repeat(255) + "@example.com")) {

            assertThrows(
                    ValidationException.class,
                    () -> student.setEmail(email)
            );

            assertEquals("nimal@example.com", student.getEmail());
        }

        assertThrows(
                ValidationException.class,
                () -> student.setEmail(null)
        );
    }

    @Test
    void validatesPhoneBoundariesAndPreservesPreviousValueOnFailure() {
        Student student = student();

        student.setPhone("1234567");
        assertEquals("1234567", student.getPhone());

        student.setPhone("+123456789012345");

        for (String phone : List.of(
                "", "123456", "1234567890123456",
                "077-1234567", "phone123", "++94771234567")) {

            assertThrows(
                    ValidationException.class,
                    () -> student.setPhone(phone)
            );

            assertEquals("+123456789012345", student.getPhone());
        }

        assertThrows(
                ValidationException.class,
                () -> student.setPhone(null)
        );
    }

    @Test
    void rejectsPlainPasswordsAndMalformedHashes() {
        Student student = student();

        for (String invalid : List.of(
                "", "Password123!", "hash-value", "$2a$10$short")) {

            assertThrows(
                    ValidationException.class,
                    () -> student.setPasswordHash(invalid)
            );

            assertEquals(HASH, student.getPasswordHash());
        }

        assertThrows(
                ValidationException.class,
                () -> student.setPasswordHash(null)
        );
    }

    @Test
    void studentRoundTripThroughUserPreservesAllStoredFields()
            throws Exception {

        String json = mapper.writerFor(User.class)
                .writeValueAsString(student());

        JsonNode tree = mapper.readTree(json);

        assertEquals("STUDENT", tree.get("type").asText());
        assertFalse(tree.has("role"));
        assertFalse(tree.has("dashboardPath"));
        assertFalse(tree.has("password"));

        User restored = mapper.readerFor(User.class).readValue(json);
        Student actual = assertInstanceOf(Student.class, restored);

        assertEquals("S001", actual.getId());
        assertEquals("Nimal Perera", actual.getName());
        assertEquals("nimal@example.com", actual.getEmail());
        assertEquals("+94771234567", actual.getPhone());
        assertEquals(HASH, actual.getPasswordHash());
        assertEquals(Role.STUDENT, actual.getRole());
    }

    @Test
    void adminRoundTripThroughUserPreservesAllStoredFields()
            throws Exception {

        String json = mapper.writerFor(User.class)
                .writeValueAsString(admin());

        JsonNode tree = mapper.readTree(json);

        assertEquals("ADMIN", tree.get("type").asText());

        User restored = mapper.readerFor(User.class).readValue(json);
        Admin actual = assertInstanceOf(Admin.class, restored);

        assertEquals("A001", actual.getId());
        assertEquals("System Admin", actual.getName());
        assertEquals("admin@example.com", actual.getEmail());
        assertEquals(HASH, actual.getPasswordHash());
        assertEquals(Role.ADMIN, actual.getRole());
    }

    @Test
    void rejectsMissingAndNullRequiredProperties() throws Exception {
        for (User original : List.of(student(), admin())) {
            String json = mapper.writerFor(User.class)
                    .writeValueAsString(original);

            List<String> required = original instanceof Student
                    ? List.of("id", "name", "email", "passwordHash", "phone")
                    : List.of("id", "name", "email", "passwordHash");

            for (String property : required) {
                ObjectNode missing = (ObjectNode) mapper.readTree(json);
                missing.remove(property);

                assertThrows(
                        MismatchedInputException.class,
                        () -> mapper.readValue(
                                missing.toString(), User.class
                        )
                );

                ObjectNode explicitNull =
                        (ObjectNode) mapper.readTree(json);
                explicitNull.putNull(property);

                ValueInstantiationException exception = assertThrows(
                        ValueInstantiationException.class,
                        () -> mapper.readValue(
                                explicitNull.toString(), User.class
                        )
                );

                assertInstanceOf(
                        ValidationException.class,
                        exception.getCause()
                );
            }
        }
    }

    @Test
    void rejectsUnknownOrMissingUserType() throws Exception {
        ObjectNode tree = (ObjectNode) mapper.readTree(
                mapper.writerFor(User.class)
                        .writeValueAsString(student())
        );

        tree.put("type", "INSTRUCTOR");

        assertThrows(
                InvalidTypeIdException.class,
                () -> mapper.readValue(tree.toString(), User.class)
        );

        tree.remove("type");

        assertThrows(
                InvalidTypeIdException.class,
                () -> mapper.readValue(tree.toString(), User.class)
        );
    }

    @Test
    void studentIdCannotBeReconstructedAsAnAdmin() throws Exception {
        ObjectNode tree = (ObjectNode) mapper.readTree(
                mapper.writerFor(User.class)
                        .writeValueAsString(student())
        );

        tree.put("type", "ADMIN");
        tree.remove("phone");

        ValueInstantiationException exception = assertThrows(
                ValueInstantiationException.class,
                () -> mapper.readValue(tree.toString(), User.class)
        );

        assertInstanceOf(
                ValidationException.class,
                exception.getCause()
        );
    }
}