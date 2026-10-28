package com.drivingschool.auth.service;

import com.drivingschool.auth.model.Admin;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.util.PasswordHasher;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.StorageException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.student.model.Student;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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

class AdminSeedServiceTest {

    private static final String PASSWORD = "SeedTestPassword123!";

    @TempDir
    Path tempDir;

    private UserRepository repository;
    private PasswordHasher hasher;
    private AdminSeedService service;

    @BeforeEach
    void setUp() {
        repository = reopen();
        hasher = new PasswordHasher();
        service = new AdminSeedService(repository, hasher);
    }

    private UserRepository reopen() {
        return new UserRepository(
                tempDir.toString(),
                new ObjectMapper()
        );
    }

    @Test
    void createsAdministratorWithGeneratedIdAndHashedPassword() {
        assertTrue(service.seedIfNeeded(
                " System Admin ",
                " ADMIN@EXAMPLE.COM ",
                PASSWORD
        ));

        Admin stored = assertInstanceOf(
                Admin.class,
                reopen().findByIdStrict("A001").orElseThrow()
        );

        assertEquals("System Admin", stored.getName());
        assertEquals("admin@example.com", stored.getEmail());
        assertNotEquals(PASSWORD, stored.getPasswordHash());
        assertTrue(hasher.matches(PASSWORD, stored.getPasswordHash()));
    }

    @Test
    void repeatedSeedDoesNotChangeAccountOrCounters() throws Exception {
        service.seedIfNeeded(
                "System Admin", "admin@example.com", PASSWORD
        );

        Path records = tempDir.resolve("users.txt");
        Path counters = tempDir.resolve("users.txt.ids.txt");

        String recordsBefore = Files.readString(records);
        String countersBefore = Files.readString(counters);

        AdminSeedService restarted =
                new AdminSeedService(reopen(), hasher);

        assertFalse(restarted.seedIfNeeded(
                "Changed Name",
                "different@example.com",
                "DifferentPassword123!"
        ));

        assertEquals(recordsBefore, Files.readString(records));
        assertEquals(countersBefore, Files.readString(counters));
        assertEquals(1, reopen().readAllStrict().size());
    }

    @Test
    void existingAdministratorDoesNotRequireSeedCredentials() {
        service.seedIfNeeded(
                "System Admin", "admin@example.com", PASSWORD
        );

        assertFalse(service.seedIfNeeded(null, null, null));
    }

    @Test
    void rejectsEmailBelongingToStudentWithoutPromotingStudent() {
        AuthService authService = new AuthService(repository, hasher);

        authService.registerStudent(
                "Nimal Perera",
                "shared@example.com",
                "0771234567",
                PASSWORD
        );

        assertThrows(
                DuplicateRecordException.class,
                () -> service.seedIfNeeded(
                        "System Admin",
                        " SHARED@EXAMPLE.COM ",
                        PASSWORD
                )
        );

        List<User> users = reopen().readAllStrict();

        assertEquals(1, users.size());
        assertInstanceOf(Student.class, users.get(0));
    }

    @Test
    void invalidSeedDetailsDoNotSaveAdministrator() {
        assertThrows(
                ValidationException.class,
                () -> service.seedIfNeeded(
                        "System Admin", "", PASSWORD
                )
        );

        assertThrows(
                ValidationException.class,
                () -> service.seedIfNeeded(
                        "System Admin", "admin@example.com", ""
                )
        );

        assertThrows(
                ValidationException.class,
                () -> service.seedIfNeeded(
                        " ", "admin@example.com", PASSWORD
                )
        );

        assertThrows(
                ValidationException.class,
                () -> service.seedIfNeeded(
                        "System Admin", "invalid-email", PASSWORD
                )
        );

        assertTrue(reopen().readAllStrict().isEmpty());
    }

    @Test
    void corruptionBlocksSeedingWithoutChangingFiles() throws Exception {
        Path records = tempDir.resolve("users.txt");
        Path counters = tempDir.resolve("users.txt.ids.txt");

        Files.writeString(
                records,
                "invalid-json\n",
                StandardOpenOption.APPEND
        );

        String recordsBefore = Files.readString(records);
        String countersBefore = Files.readString(counters);

        assertThrows(
                StorageException.class,
                () -> service.seedIfNeeded(
                        "System Admin", "admin@example.com", PASSWORD
                )
        );

        assertEquals(recordsBefore, Files.readString(records));
        assertEquals(countersBefore, Files.readString(counters));
    }

    @Test
    void registrationAndSeedingCannotCreateSameEmailConcurrently()
            throws Exception {

        // Separate repository instances still share the account coordinator.
        AuthService authService = new AuthService(reopen(), hasher);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Boolean> register = () -> {
            awaitStart(ready, start);

            try {
                authService.registerStudent(
                        "Nimal Perera", "shared@example.com",
                        "0771234567", PASSWORD
                );
                return true;
            } catch (DuplicateRecordException exception) {
                return false;
            }
        };

        Callable<Boolean> seed = () -> {
            awaitStart(ready, start);

            try {
                return service.seedIfNeeded(
                        "System Admin", "shared@example.com", PASSWORD
                );
            } catch (DuplicateRecordException exception) {
                return false;
            }
        };

        try {
            Future<Boolean> registered = executor.submit(register);
            Future<Boolean> seeded = executor.submit(seed);

            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();

            assertNotEquals(
                    registered.get(30, TimeUnit.SECONDS),
                    seeded.get(30, TimeUnit.SECONDS)
            );

            assertEquals(1, reopen().readAllStrict().size());
        } finally {
            start.countDown();
            executor.shutdownNow();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }
    }

    private static void awaitStart(
            CountDownLatch ready,
            CountDownLatch start) throws InterruptedException {

        ready.countDown();

        if (!start.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Test start timed out.");
        }
    }
}