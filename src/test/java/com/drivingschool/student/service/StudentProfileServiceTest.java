package com.drivingschool.student.service;

import com.drivingschool.auth.model.User;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.service.AuthService;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.auth.util.PasswordHasher;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.StorageException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.student.dto.StudentProfile;
import com.drivingschool.student.model.Student;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StudentProfileServiceTest {
    // Synthetic format-valid hash, never a real credential.
    private static final String HASH = "$2a$10$" + "A".repeat(53);
    @TempDir Path directory;
    private ObjectMapper mapper;
    private UserRepository repository;
    private AuthService authentication;
    private AuthSessionService sessions;
    private StudentProfileService service;
    private Student student;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper().findAndRegisterModules();
        repository = spy(new UserRepository(directory.toString(), mapper));
        authentication = mock(AuthService.class);
        sessions = new AuthSessionService(authentication, repository);
        service = new StudentProfileService(repository, sessions);
        student = addStudent("learner@example.com");
        request = login(student);
    }

    private Student addStudent(String email) {
        Student value = new Student(repository.nextId("S"), "Test Learner",
                email, HASH, "+94771234567");
        repository.save(value);
        return value;
    }

    private MockHttpServletRequest login(User user) {
        when(authentication.authenticate(user.getEmail(), "test-login"))
                .thenReturn(Optional.of(user));
        MockHttpServletRequest result = new MockHttpServletRequest();
        assertTrue(sessions.login(result, user.getEmail(), "test-login"));
        return result;
    }

    private User addAdmin() throws Exception {
        // Use the existing polymorphic model without assuming an Admin constructor.
        String json = mapper.createObjectNode()
                .put("type", "ADMIN").put("id", repository.nextId("A"))
                .put("name", "Test Admin").put("email", "admin@example.com")
                .put("passwordHash", HASH).toString();
        User admin = mapper.readValue(json, User.class);
        repository.save(admin);
        return admin;
    }

    @Test
    void returnsOnlySafeProfileFields() {
        StudentProfile profile = service.getOwnProfile(request);
        assertEquals(student.getId(), profile.getId());
        assertEquals(student.getName(), profile.getName());
        assertEquals(student.getEmail(), profile.getEmail());
        assertEquals(student.getPhone(), profile.getPhone());
        var json = mapper.valueToTree(profile);
        assertEquals(4, json.size());
        assertFalse(json.has("passwordHash"));
        assertFalse(json.has("role"));
    }

    @Test
    void rejectsAnonymousReadAndUpdateWithoutCreatingSession() {
        MockHttpServletRequest anonymous = new MockHttpServletRequest();
        assertEquals(401, assertThrows(ResponseStatusException.class,
                () -> service.getOwnProfile(anonymous)).getStatusCode().value());
        assertEquals(401, assertThrows(ResponseStatusException.class,
                () -> service.updateOwnProfile(anonymous, "New Name",
                        "new@example.com", "0771234567")).getStatusCode().value());
        assertNull(anonymous.getSession(false));
    }

    @Test
    void rejectsAdministratorReadAndUpdate() throws Exception {
        MockHttpServletRequest adminRequest = login(addAdmin());
        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> service.getOwnProfile(adminRequest)).getStatusCode().value());
        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> service.updateOwnProfile(adminRequest, "New Name",
                        "new@example.com", "0771234567")).getStatusCode().value());
    }

    @Test
    void normalizesAndPersistsOnlyOwnProfilePreservingIdentityAndHash() {
        Student other = addStudent("other@example.com");
        String sessionId = request.getSession(false).getId();
        StudentProfile result = service.updateOwnProfile(request,
                "  Updated Learner  ", "  NEW@EXAMPLE.COM  ", "  +94770000000  ");
        UserRepository reopened = new UserRepository(directory.toString(), mapper);
        Student stored = (Student) reopened.findByIdStrict(student.getId()).orElseThrow();
        assertEquals("Updated Learner", stored.getName());
        assertEquals("new@example.com", stored.getEmail());
        assertEquals("+94770000000", stored.getPhone());
        assertEquals(student.getId(), stored.getId());
        assertEquals(HASH, stored.getPasswordHash());
        assertEquals(stored.getEmail(), result.getEmail());
        assertEquals(other.getEmail(), reopened.findByIdStrict(other.getId()).orElseThrow().getEmail());
        assertEquals(sessionId, request.getSession(false).getId());
        assertEquals("new@example.com", sessions.requireLogin(request).getEmail());
        assertEquals("Updated Learner", service.getOwnProfile(request).getName());
    }

    @Test
    void normalizedNoOpAvoidsWriteAndIdAllocation() throws Exception {
        byte[] counters = Files.readAllBytes(directory.resolve("users.txt.ids.txt"));
        clearInvocations(repository);
        service.updateOwnProfile(request, "  Test Learner ",
                " LEARNER@EXAMPLE.COM ", " +94771234567 ");
        verify(repository, never()).update(any());
        verify(repository, never()).nextId(anyString());
        assertArrayEquals(counters, Files.readAllBytes(directory.resolve("users.txt.ids.txt")));
    }

    @Test
    void invalidReplacementLeavesFileAndOriginalObjectUnchanged() throws Exception {
        byte[] before = Files.readAllBytes(directory.resolve("users.txt"));
        String[][] invalid = {
                {null, "new@example.com", "0771234567"},
                {"X", "new@example.com", "0771234567"},
                {"New Name", "bad email", "0771234567"},
                {"New Name", "new@example.com", "123"}
        };
        for (String[] values : invalid) {
            assertThrows(ValidationException.class, () -> service.updateOwnProfile(
                    request, values[0], values[1], values[2]));
            assertArrayEquals(before, Files.readAllBytes(directory.resolve("users.txt")));
        }
        assertEquals("Test Learner", student.getName());
    }

    @Test
    void rejectsEmailOwnedByAnotherStudent() {
        addStudent("taken@example.com");
        assertThrows(DuplicateRecordException.class, () -> service.updateOwnProfile(
                request, "New Name", " TAKEN@EXAMPLE.COM ", "0771234567"));
        assertEquals(student.getEmail(), service.getOwnProfile(request).getEmail());
    }

    @Test
    void rejectsEmailOwnedByAdministrator() throws Exception {
        addAdmin();
        assertThrows(DuplicateRecordException.class, () -> service.updateOwnProfile(
                request, "New Name", " ADMIN@EXAMPLE.COM ", "0771234567"));
    }

    @Test
    void refusesUpdateWhenStoredEmailsAlreadyConflict() {
        // Deliberate integrity fixture: repository alone does not enforce email uniqueness.
        addStudent(student.getEmail());
        assertThrows(StorageException.class, () -> service.updateOwnProfile(
                request, "New Name", "new@example.com", "0771234567"));
    }

    @Test
    void missingAccountInvalidatesSessionAndCannotBeRecreatedByUpdate() {
        repository.delete(student.getId());
        assertEquals(401, assertThrows(ResponseStatusException.class,
                () -> service.updateOwnProfile(request, "New Name", "new@example.com",
                        "0771234567")).getStatusCode().value());
        assertNull(request.getSession(false));
        assertTrue(repository.readAllStrict().isEmpty());
    }

    @Test
    void corruptStoragePropagatesForReadAndWrite() throws Exception {
        Files.writeString(directory.resolve("users.txt"), "not-json\n", StandardCharsets.UTF_8);
        assertThrows(StorageException.class, () -> service.getOwnProfile(request));
        assertThrows(StorageException.class, () -> service.updateOwnProfile(
                request, "New Name", "new@example.com", "0771234567"));
        assertNotNull(request.getSession(false));
    }

    @Test
    void writeFailurePropagatesWithoutMutatingCurrentAccount() {
        StorageException failure = new StorageException("Test write failure", new IOException());
        doThrow(failure).when(repository).update(any());
        assertSame(failure, assertThrows(StorageException.class,
                () -> service.updateOwnProfile(request, "New Name", "new@example.com", "0771234567")));
        assertEquals(student.getEmail(), repository.findByIdStrict(student.getId()).orElseThrow().getEmail());
    }

    @Test
    void twoServiceInstancesCannotClaimSameEmail() throws Exception {
        Student second = addStudent("second@example.com");
        MockHttpServletRequest secondRequest = login(second);
        UserRepository secondRepository = new UserRepository(directory.toString(), mapper);
        StudentProfileService secondService = new StudentProfileService(secondRepository,
                new AuthSessionService(authentication, secondRepository));
        assertOneWinner(
                () -> service.updateOwnProfile(request, "First Learner", "shared@example.com", "0771234567"),
                () -> secondService.updateOwnProfile(secondRequest, "Second Learner", "shared@example.com", "0771234567"));
        assertEquals(1L, repository.readAllStrict().stream()
                .filter(u -> u.getEmail().equals("shared@example.com")).count());
    }

    @Test
    void registrationAndProfileUpdateShareAccountCoordinator() throws Exception {
        PasswordHasher hasher = mock(PasswordHasher.class);
        when(hasher.hash(anyString())).thenReturn(HASH);
        AuthService realRegistration = new AuthService(
                new UserRepository(directory.toString(), mapper), hasher);
        assertOneWinner(
                () -> service.updateOwnProfile(request, "First Learner", "shared@example.com", "0771234567"),
                () -> realRegistration.registerStudent("New Learner", "shared@example.com", "0771234567", "test-only"));
        assertEquals(1L, repository.readAllStrict().stream()
                .filter(u -> u.getEmail().equals("shared@example.com")).count());
    }

    private void assertOneWinner(Runnable first, Runnable second) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Callable<Boolean> a = contender(first, ready, start);
            Callable<Boolean> b = contender(second, ready, start);
            List<Future<Boolean>> futures = List.of(executor.submit(a), executor.submit(b));
            assertTrue(ready.await(5, TimeUnit.SECONDS), "Workers did not become ready");
            start.countDown();
            int successes = 0;
            for (Future<Boolean> future : futures) {
                if (future.get(10, TimeUnit.SECONDS)) successes++;
            }
            assertEquals(1, successes);
        } finally {
            start.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), "Executor did not terminate");
        }
    }

    private Callable<Boolean> contender(Runnable operation, CountDownLatch ready,
                                         CountDownLatch start) {
        return () -> {
            ready.countDown();
            if (!start.await(5, TimeUnit.SECONDS)) throw new TimeoutException("Start signal missing");
            try {
                operation.run();
                return true;
            } catch (DuplicateRecordException expected) {
                return false;
            }
            // Every unexpected exception escapes and fails Future.get().
        };
    }
}
