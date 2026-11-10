/**
 * ============================================================
 * MEMBER   : IT26101135 - [Name]
 * MODULE   : Authentication and Students
 * FILE     : StudentEnrollmentBalanceTest.java
 * WHAT IT DOES (simple words): Checks that unpaid receipts cannot unlock lessons.
 * REAL-LIFE EXAMPLE: Reception checks the receipt before handing out lesson tickets.
 * OOP USED : Encapsulation and inheritance
 * CRUD     : READ student account balances
 * ============================================================
 */
package com.drivingschool.auth.repository;

import com.drivingschool.learningpackage.model.Enrollment;
import com.drivingschool.learningpackage.repository.EnrollmentRepository;
import com.drivingschool.student.model.Student;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class StudentEnrollmentBalanceTest {
    @TempDir Path directory;

    // Every read API hides an old balance linked to an unpaid request, without editing files.
    @Test
    void awaitingPaymentDoesNotProvideActiveLessons() throws Exception {
        UserRepository users = savedBalance("AWAITING_PAYMENT");
        String before = Files.readString(directory.resolve("users.txt"));
        assertNoLessons((Student) users.findByIdStrict("S001").orElseThrow());
        assertNoLessons((Student) users.readAllStrict().get(0));
        assertNoLessons((Student) users.findById("S001").orElseThrow());
        assertNoLessons((Student) users.readAll().get(0));
        assertEquals(before, Files.readString(directory.resolve("users.txt")));
    }

    // Cancelled requests stay in the history but cannot act as a learning package.
    @Test
    void cancelledRequestDoesNotProvideActiveLessons() {
        UserRepository users = savedBalance("CANCELLED");
        assertNoLessons((Student) users.findByIdStrict("S001").orElseThrow());
    }

    // An existing paid receipt retains the student's remaining lesson tickets.
    @Test
    void paidReceiptKeepsActiveLessons() {
        UserRepository users = savedBalance("PAID");
        Student student = (Student) users.findByIdStrict("S001").orElseThrow();
        assertEquals("E001", student.getActiveEnrollmentId());
        assertEquals("P001", student.getActivePackageId());
        assertEquals(5, student.getLessonsRemaining());
    }

    // A missing receipt cannot act as proof of payment, even if the account has old pointers.
    @Test
    void missingReceiptDoesNotProvideActiveLessons() throws Exception {
        UserRepository users = savedBalance("PAID");
        Files.writeString(directory.resolve("enrollments.txt"), "");
        assertNoLessons((Student) users.findByIdStrict("S001").orElseThrow());
    }

    // Builds deliberately stale account pointers in a temporary filing cabinet.
    private UserRepository savedBalance(String status) {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        UserRepository users = new UserRepository(directory.toString(), mapper);
        EnrollmentRepository receipts = new EnrollmentRepository(directory.toString(), mapper);
        Enrollment enrollment = new Enrollment("E001", "S001", "P001",
                LocalDateTime.now(), new BigDecimal("1000"), "PAID");
        enrollment.setStatus(status);
        receipts.save(enrollment);
        Student student = new Student("S001", "Test Student", "student@example.test",
                "$2a$10$" + "A".repeat(53), "0771234567");
        student.setActivePackageId("P001");
        student.setLessonsRemaining(5);
        student.setActiveEnrollmentId("E001");
        users.save(student);
        return users;
    }

    // Checks that all three active-learning details disappear together.
    private void assertNoLessons(Student student) {
        assertNull(student.getActiveEnrollmentId());
        assertNull(student.getActivePackageId());
        assertEquals(0, student.getLessonsRemaining());
    }
}
