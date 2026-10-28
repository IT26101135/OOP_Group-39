package com.drivingschool.auth.util;

import com.drivingschool.auth.model.Admin;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.student.model.Student;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    private final PasswordHasher hasher = new PasswordHasher();

    @Test
    void generatedHashVerifiesTheCorrectPassword() {
        String password = "DrivingSchool123!";
        String hash = hasher.hash(password);

        assertNotEquals(password, hash);
        assertTrue(hasher.matches(password, hash));
        assertFalse(hasher.matches("IncorrectPassword123!", hash));
    }

    @Test
    void hashingSamePasswordTwiceProducesDifferentHashes() {
        String password = "DrivingSchool123!";

        String firstHash = hasher.hash(password);
        String secondHash = hasher.hash(password);

        assertNotEquals(firstHash, secondHash);
        assertTrue(hasher.matches(password, firstHash));
        assertTrue(hasher.matches(password, secondHash));
    }

    @Test
    void preservesPasswordCaseAndSpaces() {
        String password = " DriveSpark123! ";
        String hash = hasher.hash(password);

        assertTrue(hasher.matches(password, hash));
        assertFalse(hasher.matches("DriveSpark123!", hash));
        assertFalse(hasher.matches(" drivespark123! ", hash));
    }

    @Test
    void rejectsNullBlankAndShortNewPasswords() {
        assertThrows(
                ValidationException.class,
                () -> hasher.hash(null)
        );

        assertThrows(
                ValidationException.class,
                () -> hasher.hash("")
        );

        assertThrows(
                ValidationException.class,
                () -> hasher.hash("        ")
        );

        assertThrows(
                ValidationException.class,
                () -> hasher.hash("1234567")
        );
    }

    @Test
    void acceptsEightCharacterPassword() {
        String password = "Abcd123!";
        String hash = hasher.hash(password);

        assertTrue(hasher.matches(password, hash));
    }

    @Test
    void acceptsExactlySeventyTwoAsciiBytes() {
        String password = "a".repeat(72);
        String hash = hasher.hash(password);

        assertTrue(hasher.matches(password, hash));

        // An extra byte must not be silently ignored during verification.
        assertFalse(hasher.matches(password + "b", hash));
    }

    @Test
    void rejectsNewPasswordLongerThanSeventyTwoBytes() {
        assertThrows(
                ValidationException.class,
                () -> hasher.hash("a".repeat(73))
        );
    }

    @Test
    void measuresUnicodePasswordLimitInUtf8Bytes() {
        // é occupies two bytes in UTF-8: 36 characters = 72 bytes.
        String valid = "\u00E9".repeat(36);
        String hash = hasher.hash(valid);

        assertTrue(hasher.matches(valid, hash));

        // 37 characters = 74 bytes.
        String tooLong = "\u00E9".repeat(37);

        assertThrows(
                ValidationException.class,
                () -> hasher.hash(tooLong)
        );

        assertFalse(hasher.matches(tooLong, hash));
    }

    @Test
    void invalidLoginInputsReturnFalse() {
        String hash = hasher.hash("DrivingSchool123!");

        assertFalse(hasher.matches(null, hash));
        assertFalse(hasher.matches("", hash));
        assertFalse(hasher.matches("        ", hash));

        assertFalse(hasher.matches("DrivingSchool123!", null));
        assertFalse(hasher.matches("DrivingSchool123!", ""));
        assertFalse(hasher.matches("DrivingSchool123!", "not-a-hash"));
        assertFalse(hasher.matches("DrivingSchool123!", "$2a$12$short"));
    }

    @Test
    void generatedHashIsAcceptedByStudentAndAdminModels() {
        String hash = hasher.hash("DrivingSchool123!");

        Student student = new Student(
                "S001",
                "Nimal Perera",
                "nimal@example.com",
                hash,
                "0771234567"
        );

        Admin admin = new Admin(
                "A001",
                "System Admin",
                "admin@example.com",
                hash
        );

        assertEquals(hash, student.getPasswordHash());
        assertEquals(hash, admin.getPasswordHash());

        assertTrue(hasher.matches(
                "DrivingSchool123!", student.getPasswordHash()
        ));

        assertTrue(hasher.matches(
                "DrivingSchool123!", admin.getPasswordHash()
        ));
    }
}