/**
 * ============================================================
 * MEMBER   : IT26101135 - [Name]
 * MODULE   : Authentication and Students
 * FILE     : AuthServiceTest.java
 * WHAT IT DOES (simple words): Checks registration and password rules on spare files.
 * REAL-LIFE EXAMPLE: Practises checking account forms before real students arrive.
 * OOP USED : Encapsulation and exception handling
 * CRUD     : CREATE and READ student accounts
 * ============================================================
 */
package com.drivingschool.auth.service;

import com.drivingschool.auth.model.Admin;
import com.drivingschool.auth.model.AccountStatus;
import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.util.PasswordHasher;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.StorageException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.student.model.Student;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private static final String PASSWORD = "DrivingSchool123!";
    private static String testHash;

    @TempDir
    Path tempDir;

    private UserRepository repository;
    private PasswordHasher hasher;
    private AuthService service;

    @BeforeAll
    // Prepares the account details needed by these practice checks: generate test hash.
    static void generateTestHash() {
        testHash = new PasswordHasher().hash(PASSWORD);
    }

    @BeforeEach
    // Checks the account rule using temporary data: set up.
    void setUp() {
        repository = reopen();
        hasher = new PasswordHasher();
        service = new AuthService(repository, hasher);
    }

    // Prepares the account details needed by these practice checks: reopen.
    private UserRepository reopen() {
        return new UserRepository(
                tempDir.toString(),
                new ObjectMapper()
        );
    }

    // Prepares the account details needed by these practice checks: register.
    private Student register(String email) {
        return service.registerStudent(
                "Nimal Perera",
                email,
                "0771234567",
                PASSWORD
        );
    }

    @Test
    // Checks the account rule using temporary data: registration generates id and persists hashed student.
    void registrationGeneratesIdAndPersistsHashedStudent() {
        Student created = service.registerStudent(
                "  Nimal Perera  ",
                "  NIMAL@EXAMPLE.COM  ",
                "  0771234567  ",
                PASSWORD
        );

        assertEquals("S001", created.getId());
        assertEquals(Role.STUDENT, created.getRole());
        assertEquals(AccountStatus.ACTIVE, created.getStatus());

        Student stored = assertInstanceOf(
                Student.class,
                reopen().findByIdStrict("S001").orElseThrow()
        );

        assertEquals("Nimal Perera", stored.getName());
        assertEquals("nimal@example.com", stored.getEmail());
        assertEquals("0771234567", stored.getPhone());
        assertEquals(AccountStatus.ACTIVE, stored.getStatus());
        assertNotEquals(PASSWORD, stored.getPasswordHash());
        assertTrue(hasher.matches(PASSWORD, stored.getPasswordHash()));
    }

    @Test
    // Checks the account rule using temporary data: successive registrations receive different ids.
    void successiveRegistrationsReceiveDifferentIds() {
        Student first = register("first@example.com");
        Student second = register("second@example.com");

        assertEquals("S001", first.getId());
        assertEquals("S002", second.getId());
        assertEquals(2, reopen().readAllStrict().size());
    }

    @Test
    // Checks the account rule using temporary data: rejects duplicate student email ignoring case and outer spaces.
    void rejectsDuplicateStudentEmailIgnoringCaseAndOuterSpaces() {
        register("nimal@example.com");

        assertThrows(
                DuplicateRecordException.class,
                () -> register("  NIMAL@EXAMPLE.COM  ")
        );

        assertEquals(1, reopen().readAllStrict().size());
    }

    @Test
    // Checks the account rule using temporary data: rejects registration using an administrator email.
    void rejectsRegistrationUsingAnAdministratorEmail() {
        repository.save(new Admin(
                "A001",
                "System Admin",
                "admin@example.com",
                testHash
        ));

        assertThrows(
                DuplicateRecordException.class,
                () -> register(" ADMIN@EXAMPLE.COM ")
        );

        List<User> users = reopen().readAllStrict();

        assertEquals(1, users.size());
        assertInstanceOf(Admin.class, users.get(0));
    }

    @Test
    // Checks the account rule using temporary data: invalid registration does not save an account.
    void invalidRegistrationDoesNotSaveAnAccount() {
        assertThrows(
                ValidationException.class,
                () -> service.registerStudent(
                        " ", "valid@example.com", "0771234567", PASSWORD
                )
        );

        assertThrows(
                ValidationException.class,
                () -> register("invalid-email")
        );

        assertThrows(
                ValidationException.class,
                () -> service.registerStudent(
                        "Nimal Perera", "valid@example.com", "123", PASSWORD
                )
        );

        assertThrows(
                ValidationException.class,
                () -> service.registerStudent(
                        "Nimal Perera", "valid@example.com",
                        "0771234567", "short"
                )
        );

        assertThrows(
                ValidationException.class,
                () -> register(null)
        );

        assertTrue(reopen().readAllStrict().isEmpty());
    }

    @Test
    // Checks the account rule using temporary data: authenticates saved student using normalized email.
    void authenticatesSavedStudentUsingNormalizedEmail() {
        Student student = register("nimal@example.com");

        AuthService reloadedService =
                new AuthService(reopen(), hasher);

        User authenticated = reloadedService.authenticate(
                "  NIMAL@EXAMPLE.COM  ",
                PASSWORD
        ).orElseThrow();

        assertInstanceOf(Student.class, authenticated);
        assertEquals(student.getId(), authenticated.getId());
        assertEquals(Role.STUDENT, authenticated.getRole());
    }

    @Test
    // Checks the account rule using temporary data: authenticates administrator without public admin registration.
    void authenticatesAdministratorWithoutPublicAdminRegistration() {
        repository.save(new Admin(
                "A001",
                "System Admin",
                "admin@example.com",
                testHash
        ));

        User authenticated = service.authenticate(
                "admin@example.com",
                PASSWORD
        ).orElseThrow();

        assertInstanceOf(Admin.class, authenticated);
        assertEquals(Role.ADMIN, authenticated.getRole());
    }

    @Test
    // Checks the account rule using temporary data: invalid credentials return empty.
    void invalidCredentialsReturnEmpty() {
        register("nimal@example.com");

        assertTrue(service.authenticate(
                "nimal@example.com", "WrongPassword123!"
        ).isEmpty());

        assertTrue(service.authenticate(
                "unknown@example.com", PASSWORD
        ).isEmpty());

        assertTrue(service.authenticate(
                "nimal@example.com", null
        ).isEmpty());

        assertTrue(service.authenticate(
                "nimal@example.com", "a".repeat(73)
        ).isEmpty());

        assertTrue(service.authenticate(null, PASSWORD).isEmpty());
        assertTrue(service.authenticate(" ", PASSWORD).isEmpty());
    }

    @Test
    // Checks the account rule using temporary data: duplicate stored emails cause storage failure.
    void duplicateStoredEmailsCauseStorageFailure() {
        repository.save(new Student(
                "S001",
                "Nimal Perera",
                "shared@example.com",
                testHash,
                "0771234567"
        ));

        repository.save(new Admin(
                "A001",
                "System Admin",
                "shared@example.com",
                testHash
        ));

        assertThrows(
                StorageException.class,
                () -> service.authenticate(
                        "shared@example.com", PASSWORD
                )
        );

        assertThrows(
                StorageException.class,
                () -> register("shared@example.com")
        );
    }

    @Test
    // Checks the account rule using temporary data: corrupt storage blocks registration and authentication.
    void corruptStorageBlocksRegistrationAndAuthentication()
            throws IOException {

        register("nimal@example.com");

        Path records = tempDir.resolve("users.txt");
        Path counters = tempDir.resolve("users.txt.ids.txt");

        Files.writeString(
                records,
                "invalid-json\n",
                StandardCharsets.UTF_8,
                StandardOpenOption.APPEND
        );

        String recordsBefore = Files.readString(records);
        String countersBefore = Files.readString(counters);

        assertThrows(
                StorageException.class,
                () -> register("another@example.com")
        );

        assertThrows(
                StorageException.class,
                () -> service.authenticate(
                        "nimal@example.com", PASSWORD
                )
        );

        assertEquals(recordsBefore, Files.readString(records));
        assertEquals(countersBefore, Files.readString(counters));
    }

    @Test
    // Checks the account rule using temporary data: concurrent duplicate registrations create only one account.
    void concurrentDuplicateRegistrationsCreateOnlyOneAccount()
            throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Boolean> registration = () -> {
            ready.countDown();

            if (!start.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                        "Registration start signal timed out."
                );
            }

            try {
                register("same@example.com");
                return true;
            } catch (DuplicateRecordException exception) {
                return false;
            }
        };

        try {
            Future<Boolean> first = executor.submit(registration);
            Future<Boolean> second = executor.submit(registration);

            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();

            boolean firstCreated = first.get(30, TimeUnit.SECONDS);
            boolean secondCreated = second.get(30, TimeUnit.SECONDS);

            assertNotEquals(firstCreated, secondCreated);
            assertEquals(1, reopen().readAllStrict().size());
        } finally {
            start.countDown();
            executor.shutdownNow();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }
    }
}
