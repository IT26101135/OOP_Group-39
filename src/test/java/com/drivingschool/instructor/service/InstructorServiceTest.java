
package com.drivingschool.instructor.service;

import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.common.exception.StorageException;
import com.drivingschool.instructor.model.Instructor;
import com.drivingschool.instructor.repository.InstructorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class InstructorServiceTest {

    @TempDir
    Path tempDirectory;

    private InstructorRepository newRepository() {
        return new InstructorRepository(
                tempDirectory, new ObjectMapper());
    }

    private InstructorService newService() {
        return new InstructorService(newRepository());
    }

    private Instructor create(
            InstructorService service, String name,
            String email, int experience) {

        return service.createInstructor(
                name, email, "0771234567", experience);
    }

    @Test
    void createsGeneratedIdAndPersistsAcrossInstances() {
        InstructorService service = newService();

        Instructor created = create(
                service, "Kumar Silva", "kumar@example.com", 5);

        assertEquals("I001", created.getId());
        assertTrue(created.isActive());

        Instructor reloaded = newRepository()
                .findByIdStrict("I001").orElseThrow();

        assertEquals("Kumar Silva", reloaded.getName());
        assertEquals("kumar@example.com", reloaded.getEmail());
        assertEquals(5, reloaded.getYearsOfExperience());

        Instructor second = create(
                service, "Nimal Silva", "nimal@example.com", 3);

        assertEquals("I002", second.getId());
    }

    @Test
    void listsAndFindsInstructors() {
        InstructorService service = newService();

        assertTrue(service.getAllInstructors().isEmpty());

        Instructor first = create(
                service, "Kumar Silva", "kumar@example.com", 5);

        create(service, "Nimal Silva", "nimal@example.com", 3);

        assertEquals(2, service.getAllInstructors().size());
        assertEquals(first.getId(),
                service.getInstructorById(first.getId()).getId());
    }

    @Test
    void throwsWhenInstructorDoesNotExist() {
        InstructorService service = newService();

        assertThrows(RecordNotFoundException.class,
                () -> service.getInstructorById("I999"));

        assertThrows(RecordNotFoundException.class,
                () -> service.updateInstructor(
                        "I999", "Kumar Silva",
                        "kumar@example.com", "0771234567", 5));
    }

    @Test
    void rejectsDuplicateEmailIgnoringCaseAndSpaces() {
        InstructorService service = newService();

        create(service, "Kumar Silva",
                "Kumar@Example.com", 5);

        assertThrows(DuplicateRecordException.class,
                () -> create(service, "Nimal Silva",
                        "  kumar@example.COM  ", 3));

        assertEquals(1, newRepository().readAllStrict().size());
    }

    @Test
    void inactiveInstructorEmailStillBlocksDuplicates() {
        InstructorRepository repository = newRepository();

        repository.save(new Instructor(
                "I001", "Kumar Silva",
                "kumar@example.com", "0771234567",
                5, false));

        InstructorService service =
                new InstructorService(repository);

        assertThrows(DuplicateRecordException.class,
                () -> create(service, "Nimal Silva",
                        "KUMAR@example.com", 3));

        assertEquals(1, repository.readAllStrict().size());
    }

    @Test
    void updatesOwnEmailWhilePreservingIdAndActiveStatus() {
        InstructorRepository repository = newRepository();

        repository.save(new Instructor(
                "I001", "Kumar Silva",
                "kumar@example.com", "0771234567",
                5, false));

        InstructorService service =
                new InstructorService(repository);

        Instructor updated = service.updateInstructor(
                "I001", "Kumar Fernando",
                "  KUMAR@example.com  ",
                "+94771234567", 8);

        assertEquals("I001", updated.getId());
        assertFalse(updated.isActive());
        assertEquals("Kumar Fernando", updated.getName());
        assertEquals("KUMAR@example.com", updated.getEmail());
        assertEquals(8, updated.getYearsOfExperience());

        Instructor reloaded = newRepository()
                .findByIdStrict("I001").orElseThrow();

        assertFalse(reloaded.isActive());
        assertEquals("I001", reloaded.getId());
        assertEquals("+94771234567", reloaded.getPhone());
    }

    @Test
    void rejectsOtherInstructorEmailDuringUpdate() {
        InstructorService service = newService();

        create(service, "Kumar Silva",
                "kumar@example.com", 5);
        create(service, "Nimal Silva",
                "nimal@example.com", 3);

        assertThrows(DuplicateRecordException.class,
                () -> service.updateInstructor(
                        "I002", "Nimal Updated",
                        " KUMAR@example.com ",
                        "0771234567", 7));

        Instructor unchanged =
                service.getInstructorById("I002");

        assertEquals("Nimal Silva", unchanged.getName());
        assertEquals("nimal@example.com", unchanged.getEmail());
        assertEquals(3, unchanged.getYearsOfExperience());
    }

    @Test
    void invalidUpdatePreservesStoredRecord() {
        InstructorService service = newService();

        create(service, "Kumar Silva",
                "kumar@example.com", 5);

        assertThrows(
                com.drivingschool.common.exception.ValidationException.class,
                () -> service.updateInstructor(
                        "I001", "Kumar Updated",
                        "invalid-email", "0771234567", 10));

        Instructor unchanged = newRepository()
                .findByIdStrict("I001").orElseThrow();

        assertEquals("Kumar Silva", unchanged.getName());
        assertEquals("kumar@example.com", unchanged.getEmail());
        assertEquals(5, unchanged.getYearsOfExperience());
    }

    @Test
    void invalidCreateDoesNotSaveRecord() {
        InstructorService service = newService();

        assertThrows(
                com.drivingschool.common.exception.ValidationException.class,
                () -> create(service, "Kumar Silva",
                        "invalid-email", 5));

        assertTrue(newRepository().readAllStrict().isEmpty());

        Instructor valid = create(
                service, "Nimal Silva",
                "nimal@example.com", 4);

        assertEquals("I001", valid.getId());
    }

    @Test
    void sortsByExperienceDescendingAndKeepsEqualOrder() {
        InstructorService service = newService();

        create(service, "Kumar Silva", "kumar@example.com", 5);
        create(service, "Nimal Silva", "nimal@example.com", 10);
        create(service, "Arun Silva", "arun@example.com", 10);
        create(service, "Ravi Silva", "ravi@example.com", 2);

        List<Instructor> sorted =
                service.getInstructorsSortedByExperience();

        assertEquals(
                List.of("I002", "I003", "I001", "I004"),
                sorted.stream().map(Instructor::getId).toList());

        // Sorting must not change stored order.
        assertEquals(
                List.of("I001", "I002", "I003", "I004"),
                service.getAllInstructors().stream()
                        .map(Instructor::getId).toList());
    }

    @Test
    void corruptStorageBlocksCreateWithoutRewritingFile()
            throws Exception {

        InstructorService service = newService();

        create(service, "Kumar Silva",
                "kumar@example.com", 5);

        Path file = tempDirectory.resolve("instructors.txt");

        Files.writeString(file, "broken-json\n",
                StandardOpenOption.APPEND);

        String before = Files.readString(file);

        assertThrows(StorageException.class,
                () -> create(service, "Nimal Silva",
                        "nimal@example.com", 3));

        assertEquals(before, Files.readString(file));
    }

    @Test
    void corruptStorageBlocksUpdateWithoutRewritingFile()
            throws Exception {

        InstructorService service = newService();

        create(service, "Kumar Silva",
                "kumar@example.com", 5);

        Path file = tempDirectory.resolve("instructors.txt");

        Files.writeString(file, "broken-json\n",
                StandardOpenOption.APPEND);

        String before = Files.readString(file);

        assertThrows(StorageException.class,
                () -> service.updateInstructor(
                        "I001", "Kumar Updated",
                        "updated@example.com", "0771234567", 9));

        assertEquals(before, Files.readString(file));
    }

    @Test
    void concurrentEquivalentEmailsAllowOnlyOneCreate()
            throws Exception {

        InstructorService service = newService();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            java.util.concurrent.Callable<Boolean> firstTask = () -> {
                ready.countDown();
                start.await();
                try {
                    create(service, "Kumar Silva",
                            "kumar@example.com", 5);
                    return true;
                } catch (DuplicateRecordException e) {
                    return false;
                }
            };

            java.util.concurrent.Callable<Boolean> secondTask = () -> {
                ready.countDown();
                start.await();
                try {
                    create(service, "Nimal Silva",
                            " KUMAR@example.com ", 3);
                    return true;
                } catch (DuplicateRecordException e) {
                    return false;
                }
            };

            Future<Boolean> first = executor.submit(firstTask);
            Future<Boolean> second = executor.submit(secondTask);

            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();

            int successes =
                    (first.get(10, TimeUnit.SECONDS) ? 1 : 0)
                            + (second.get(10, TimeUnit.SECONDS) ? 1 : 0);

            assertEquals(1, successes);
            assertEquals(1, newRepository().readAllStrict().size());

        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }
}
