// Module owner: IT26101136

package com.drivingschool.learningpackage.service;

import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.common.exception.StorageException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.learningpackage.model.LearningPackage;
import com.drivingschool.learningpackage.repository.LearningPackageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class LearningPackageServiceTest {

    @TempDir
    Path tempDirectory;

    private LearningPackageRepository createRepository() {
        return new LearningPackageRepository(
                tempDirectory,
                new ObjectMapper()
        );
    }

    private LearningPackageService createService() {
        return new LearningPackageService(createRepository());
    }

    private LearningPackage createPackage(
            String name,
            String description,
            boolean active) {

        return createService().createPackage(
                name,
                description,
                new BigDecimal("15000.00"),
                10,
                90,
                active
        );
    }
    private static class FailingLearningPackageRepository
            extends LearningPackageRepository {

        private final StorageException failure;
        private boolean failOnSave;
        private boolean failOnUpdate;

        FailingLearningPackageRepository(
                Path directory,
                StorageException failure) {

            super(directory, new ObjectMapper());
            this.failure = failure;
        }

        @Override
        public void save(LearningPackage record) {
            if (failOnSave) {
                throw failure;
            }

            super.save(record);
        }

        @Override
        public void update(LearningPackage record) {
            if (failOnUpdate) {
                throw failure;
            }

            super.update(record);
        }
    }

    @Test
    void createPackageShouldGenerateIdAndPersistRecord() {
        LearningPackageService service = createService();

        LearningPackage created = service.createPackage(
                "Beginner Package",
                "A complete package for beginner drivers.",
                new BigDecimal("15000"),
                10,
                90,
                true
        );

        assertEquals("P001", created.getId());
        assertEquals("Beginner Package", created.getName());
        assertEquals(new BigDecimal("15000.00"), created.getPrice());

        LearningPackage reopened = createRepository()
                .findByIdStrict("P001")
                .orElseThrow();

        assertEquals(created.getName(), reopened.getName());
        assertEquals(created.getDescription(), reopened.getDescription());
    }

    @Test
    void createPackageShouldTrimNameAndDescription() {
        LearningPackage created = createService().createPackage(
                "  Beginner Package  ",
                "  A complete package for beginner drivers.  ",
                new BigDecimal("15000.00"),
                10,
                90,
                true
        );

        assertEquals("Beginner Package", created.getName());
        assertEquals(
                "A complete package for beginner drivers.",
                created.getDescription()
        );
    }

    @Test
    void listPackagesShouldIncludeActiveAndInactivePackages() {
        LearningPackageService service = createService();

        service.createPackage(
                "Beginner Package",
                "A complete package for beginner drivers.",
                new BigDecimal("15000"),
                10,
                90,
                true
        );

        service.createPackage(
                "Advanced Package",
                "A complete package for experienced drivers.",
                new BigDecimal("25000"),
                20,
                120,
                false
        );

        List<LearningPackage> packages = service.listPackages();

        assertEquals(2, packages.size());
        assertTrue(packages.get(0).isActive());
        assertFalse(packages.get(1).isActive());
    }

    @Test
    void findPackageByIdShouldReturnExistingAndMissingPackages() {
        LearningPackageService service = createService();

        service.createPackage(
                "Beginner Package",
                "A complete package for beginner drivers.",
                new BigDecimal("15000"),
                10,
                90,
                true
        );

        assertTrue(service.findPackageById("P001").isPresent());
        assertEquals(Optional.empty(), service.findPackageById("P999"));
    }

    @Test
    void findPackageByIdShouldRejectInvalidIds() {
        LearningPackageService service = createService();

        assertThrows(
                ValidationException.class,
                () -> service.findPackageById("p001")
        );

        assertThrows(
                ValidationException.class,
                () -> service.findPackageById("P000")
        );

        assertThrows(
                ValidationException.class,
                () -> service.findPackageById(" P001 ")
        );
    }

    @Test
    void duplicateNamesShouldBeRejectedIgnoringCaseAndOuterSpaces() {
        LearningPackageService service = createService();

        createPackage(
                "Beginner Package",
                "A complete package for beginner drivers.",
                true
        );

        assertThrows(
                DuplicateRecordException.class,
                () -> service.createPackage(
                        "  BEGINNER PACKAGE  ",
                        "Another complete package for beginner drivers.",
                        new BigDecimal("20000"),
                        15,
                        90,
                        false
                )
        );

        assertEquals(1, service.listPackages().size());
    }

    @Test
    void inactivePackageNameShouldAlsoBeUnique() {
        LearningPackageService service = createService();

        createPackage(
                "Beginner Package",
                "A complete package for beginner drivers.",
                false
        );

        assertThrows(
                DuplicateRecordException.class,
                () -> createPackage(
                        "BEGINNER PACKAGE",
                        "Another complete package for beginner drivers.",
                        true
                )
        );
    }

    @Test
    void invalidOrDuplicateCreationShouldNotAllocateAnId() {
        LearningPackageService service = createService();

        createPackage(
                "Beginner Package",
                "A complete package for beginner drivers.",
                true
        );

        assertThrows(
                ValidationException.class,
                () -> service.createPackage(
                        "X",
                        "A complete package for beginner drivers.",
                        new BigDecimal("15000"),
                        10,
                        90,
                        true
                )
        );

        assertThrows(
                DuplicateRecordException.class,
                () -> createPackage(
                        "BEGINNER PACKAGE",
                        "Another complete package for beginner drivers.",
                        true
                )
        );

        LearningPackage next = createPackage(
                "Advanced Package",
                "A complete package for experienced drivers.",
                true
        );

        assertEquals("P002", next.getId());
    }

    @Test
    void updateShouldChangeOnlyNameAndDescription() {
        LearningPackageService service = createService();

        LearningPackage original = createPackage(
                "Beginner Package",
                "A complete package for beginner drivers.",
                false
        );

        LearningPackage updated = service.updatePackageMetadata(
                original.getId(),
                "Updated Package",
                "An updated package for beginner drivers."
        );

        assertEquals(original.getId(), updated.getId());
        assertEquals("Updated Package", updated.getName());
        assertEquals(
                "An updated package for beginner drivers.",
                updated.getDescription()
        );

        assertEquals(original.getPrice(), updated.getPrice());
        assertEquals(original.getLessonCount(), updated.getLessonCount());
        assertEquals(original.getValidityDays(), updated.getValidityDays());
        assertEquals(original.isActive(), updated.isActive());

        LearningPackage persisted = createRepository()
                .findByIdStrict(original.getId())
                .orElseThrow();

        assertEquals("Updated Package", persisted.getName());
        assertEquals(
                "An updated package for beginner drivers.",
                persisted.getDescription()
        );

        assertEquals(original.getId(), persisted.getId());
        assertEquals(original.getPrice(), persisted.getPrice());
        assertEquals(original.getLessonCount(), persisted.getLessonCount());
        assertEquals(original.getValidityDays(), persisted.getValidityDays());
        assertEquals(original.isActive(), persisted.isActive());
    }

    @Test
    void updateShouldAllowKeepingItsOwnName() {
        LearningPackageService service = createService();

        LearningPackage original = createPackage(
                "Beginner Package",
                "A complete package for beginner drivers.",
                true
        );

        LearningPackage updated = service.updatePackageMetadata(
                original.getId(),
                "  BEGINNER PACKAGE  ",
                "An updated package for beginner drivers."
        );

        assertEquals("BEGINNER PACKAGE", updated.getName());
    }

    @Test
    void updateShouldRejectAnotherPackagesName() {
        LearningPackageService service = createService();

        LearningPackage first = createPackage(
                "Beginner Package",
                "A complete package for beginner drivers.",
                true
        );

        createPackage(
                "Advanced Package",
                "A complete package for experienced drivers.",
                true
        );

        assertThrows(
                DuplicateRecordException.class,
                () -> service.updatePackageMetadata(
                        first.getId(),
                        "ADVANCED PACKAGE",
                        "An updated package for beginner drivers."
                )
        );

        assertEquals(
                "Beginner Package",
                service.findPackageById(first.getId())
                        .orElseThrow()
                        .getName()
        );

        LearningPackage persisted = createRepository()
                .findByIdStrict(first.getId())
                .orElseThrow();

        assertEquals(first.getId(), persisted.getId());
        assertEquals(first.getName(), persisted.getName());
        assertEquals(first.getDescription(), persisted.getDescription());
        assertEquals(first.getPrice(), persisted.getPrice());
        assertEquals(first.getLessonCount(), persisted.getLessonCount());
        assertEquals(first.getValidityDays(), persisted.getValidityDays());
        assertEquals(first.isActive(), persisted.isActive());
    }

    @Test
    void updateShouldRejectMissingAndInvalidIds() {
        LearningPackageService service = createService();

        assertThrows(
                RecordNotFoundException.class,
                () -> service.updatePackageMetadata(
                        "P001",
                        "Updated Package",
                        "An updated package for beginner drivers."
                )
        );

        assertThrows(
                ValidationException.class,
                () -> service.updatePackageMetadata(
                        "invalid",
                        "Updated Package",
                        "An updated package for beginner drivers."
                )
        );
    }

    @Test
    void invalidUpdateShouldNotChangeStoredPackage() {
        LearningPackageService service = createService();

        LearningPackage original = createPackage(
                "Beginner Package",
                "A complete package for beginner drivers.",
                true
        );

        assertThrows(
                ValidationException.class,
                () -> service.updatePackageMetadata(
                        original.getId(),
                        "X",
                        "An updated package for beginner drivers."
                )
        );

        assertThrows(
                ValidationException.class,
                () -> service.updatePackageMetadata(
                        original.getId(),
                        "Valid Replacement Name",
                        "Short"
                )
        );

        LearningPackage persisted = createRepository()
                .findByIdStrict(original.getId())
                .orElseThrow();

        assertEquals(original.getId(), persisted.getId());
        assertEquals(original.getName(), persisted.getName());
        assertEquals(original.getDescription(), persisted.getDescription());
        assertEquals(original.getPrice(), persisted.getPrice());
        assertEquals(original.getLessonCount(), persisted.getLessonCount());
        assertEquals(original.getValidityDays(), persisted.getValidityDays());
        assertEquals(original.isActive(), persisted.isActive());

        LearningPackage stored = service.findPackageById(
                original.getId()
        ).orElseThrow();

        assertEquals(original.getName(), stored.getName());
        assertEquals(original.getDescription(), stored.getDescription());
    }

    @Test
    void corruptedRecordsShouldPreventAllOperationsWithoutChangingFiles()
            throws IOException {

        LearningPackageService service = createService();

        LearningPackage original = createPackage(
                "Beginner Package",
                "A complete package for beginner drivers.",
                true
        );

        Path recordsFile = tempDirectory.resolve("packages.txt");
        Path counterFile = tempDirectory.resolve("packages.txt.ids.txt");

        Files.writeString(
                recordsFile,
                Files.readString(recordsFile) + "{invalid json}\n"
        );

        String recordsBefore = Files.readString(recordsFile);
        String counterBefore = Files.readString(counterFile);

        assertThrows(StorageException.class, service::listPackages);

        assertThrows(
                StorageException.class,
                () -> service.findPackageById(original.getId())
        );

        assertThrows(
                StorageException.class,
                () -> service.createPackage(
                        "Advanced Package",
                        "A complete package for experienced drivers.",
                        new BigDecimal("25000"),
                        20,
                        120,
                        true
                )
        );

        assertThrows(
                StorageException.class,
                () -> service.updatePackageMetadata(
                        original.getId(),
                        "Updated Package",
                        "An updated package for beginner drivers."
                )
        );

        assertEquals(recordsBefore, Files.readString(recordsFile));
        assertEquals(counterBefore, Files.readString(counterFile));
    }

    @Test
    void corruptedCounterShouldPreventIdAllocation()
            throws IOException {

        LearningPackageService service = createService();

        Files.writeString(
                tempDirectory.resolve("packages.txt"),
                ""
        );

        Files.writeString(
                tempDirectory.resolve("packages.txt.ids.txt"),
                "invalid"
        );

        assertThrows(
                StorageException.class,
                () -> service.createPackage(
                        "Beginner Package",
                        "A complete package for beginner drivers.",
                        new BigDecimal("15000"),
                        10,
                        90,
                        true
                )
        );
    }

    @Test
    void concurrentEquivalentCreationsShouldAllowOnlyOneSuccess()
            throws Exception {

        Path sharedDirectory = tempDirectory.resolve("concurrent");

        LearningPackageService firstService =
                new LearningPackageService(
                        new LearningPackageRepository(
                                sharedDirectory,
                                new ObjectMapper()
                        )
                );

        LearningPackageService secondService =
                new LearningPackageService(
                        new LearningPackageRepository(
                                sharedDirectory,
                                new ObjectMapper()
                        )
                );

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Boolean> firstTask = () -> {
            ready.countDown();
            start.await();

            try {
                firstService.createPackage(
                        "Beginner Package",
                        "A complete package for beginner drivers.",
                        new BigDecimal("15000"),
                        10,
                        90,
                        true
                );
                return true;

            } catch (DuplicateRecordException exception) {
                return false;
            }
        };

        Callable<Boolean> secondTask = () -> {
            ready.countDown();
            start.await();

            try {
                secondService.createPackage(
                        "  BEGINNER PACKAGE  ",
                        "Another complete package for beginner drivers.",
                        new BigDecimal("15000"),
                        10,
                        90,
                        true
                );
                return true;

            } catch (DuplicateRecordException exception) {
                return false;
            }
        };

        try {
            Future<Boolean> first = executor.submit(firstTask);
            Future<Boolean> second = executor.submit(secondTask);

            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();

            int successes = (first.get(10, TimeUnit.SECONDS) ? 1 : 0)
                    + (second.get(10, TimeUnit.SECONDS) ? 1 : 0);

            assertEquals(1, successes);

            LearningPackageRepository repository =
                    new LearningPackageRepository(
                            sharedDirectory,
                            new ObjectMapper()
                    );

            assertEquals(1, repository.readAllStrict().size());

        } finally {
            start.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void createShouldPropagateSaveFailure() {

        StorageException expected = new StorageException(
                "Simulated save failure",
                new IOException("Disk write failed")
        );

        FailingLearningPackageRepository repository =
                new FailingLearningPackageRepository(
                        tempDirectory,
                        expected
                );

        LearningPackageService service =
                new LearningPackageService(repository);

        repository.failOnSave = true;

        StorageException actual = assertThrows(
                StorageException.class,
                () -> service.createPackage(
                        "Beginner Package",
                        "A complete package for beginner drivers.",
                        new BigDecimal("15000"),
                        10,
                        90,
                        true
                )
        );

        assertSame(expected, actual);
    }

    @Test
    void updateShouldPropagateRepositoryFailureWithoutMutatingOriginal() {

        LearningPackage original = createPackage(
                "Beginner Package",
                "A complete package for beginner drivers.",
                true
        );

        StorageException expected = new StorageException(
                "Simulated update failure",
                new IOException("Disk write failed")
        );

        FailingLearningPackageRepository repository =
                new FailingLearningPackageRepository(
                        tempDirectory,
                        expected
                );

        LearningPackageService service =
                new LearningPackageService(repository);

        repository.failOnUpdate = true;

        StorageException actual = assertThrows(
                StorageException.class,
                () -> service.updatePackageMetadata(
                        original.getId(),
                        "Updated Package",
                        "An updated package for beginner drivers."
                )
        );

        assertSame(expected, actual);

        assertEquals("Beginner Package", original.getName());
        assertEquals(
                "A complete package for beginner drivers.",
                original.getDescription()
        );

        assertEquals(new BigDecimal("15000.00"), original.getPrice());
        assertEquals(10, original.getLessonCount());
        assertEquals(90, original.getValidityDays());
        assertTrue(original.isActive());
        LearningPackage persisted = createRepository()
                .findByIdStrict(original.getId())
                .orElseThrow();

        assertEquals(original.getName(), persisted.getName());
        assertEquals(original.getDescription(), persisted.getDescription());
        assertEquals(original.getPrice(), persisted.getPrice());
        assertEquals(original.getLessonCount(), persisted.getLessonCount());
        assertEquals(original.getValidityDays(), persisted.getValidityDays());
        assertEquals(original.isActive(), persisted.isActive());
    }
}
