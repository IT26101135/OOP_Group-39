package com.drivingschool.auth.service;

import com.drivingschool.auth.model.Admin;
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
    static void generateTestHash() {
        testHash = new PasswordHasher().hash(PASSWORD);
    }

    @BeforeEach
    void setUp() {
        repository = reopen();
        hasher = new PasswordHasher();
        service = new AuthService(repository, hasher);
    }

    private UserRepository reopen() {
        return new UserRepository(
                tempDir.toString(),
                new ObjectMapper()
        );
    }

    private Student register(String email) {
        return service.registerStudent(
                "Nimal Perera",
                email,
                "0771234567",
                PASSWORD
        );
    }

    @Test
    void registrationGeneratesIdAndPersistsHashedStudent() {
        Student created = service.registerStudent(
                "  Nimal Perera  ",
                "  NIMAL@EXAMPLE.COM  ",
                "  0771234567  ",
                PASSWORD
        );

        assertEquals("S001", created.getId());
        assertEquals(Role.STUDENT, created.getRole());

        Student stored = assertInstanceOf(
                Student.class,
                reopen().findByIdStrict("S001").orElseThrow()
        );

        assertEquals("Nimal Perera", stored.getName());
        assertEquals("nimal@example.com", stored.getEmail());
        assertEquals("0771234567", stored.getPhone());
        assertNotEquals(PASSWORD, stored.getPasswordHash());
        assertTrue(hasher.matches(PASSWORD, stored.getPasswordHash()));
    }

    @Test
    void successiveRegistrationsReceiveDifferentIds() {
        Student first = register("first@example.com");
        Student second = register("second@example.com");

        assertEquals("S001", first.getId());
        assertEquals("S002", second.getId());
        assertEquals(2, reopen().readAllStrict().size());
    }

    @Test
    void rejectsDuplicateStudentEmailIgnoringCaseAndOuterSpaces() {
        register("nimal@example.com");

        assertThrows(
                DuplicateRecordException.class,
                () -> register("  NIMAL@EXAMPLE.COM  ")
        );

        assertEquals(1, reopen().readAllStrict().size());
    }

    @Test
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