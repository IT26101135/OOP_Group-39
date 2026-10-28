package com.drivingschool.learningpackage.repository;

import com.drivingschool.learningpackage.model.LearningPackage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LearningPackageRepositoryTest {

    @TempDir
    Path tempDirectory;

    private LearningPackageRepository createRepository() {
        return new LearningPackageRepository(
                tempDirectory,
                new ObjectMapper()
        );
    }

    private LearningPackage createPackage(String id) {
        return new LearningPackage(
                id,
                "Beginner Package",
                "A complete package for beginner drivers.",
                new BigDecimal("15000.00"),
                10,
                90,
                true
        );
    }

    @Test
    void saveAndReopenShouldPreserveAllFields() {
        LearningPackageRepository repository = createRepository();
        LearningPackage learningPackage = createPackage("P001");

        repository.save(learningPackage);

        LearningPackageRepository reopenedRepository = createRepository();
        Optional<LearningPackage> result =
                reopenedRepository.findById("P001");

        assertTrue(result.isPresent());

        LearningPackage saved = result.orElseThrow();
        assertEquals("P001", saved.getId());
        assertEquals("Beginner Package", saved.getName());
        assertEquals(
                "A complete package for beginner drivers.",
                saved.getDescription()
        );
        assertEquals(
                new BigDecimal("15000.00"),
                saved.getPrice()
        );
        assertEquals(10, saved.getLessonCount());
        assertEquals(90, saved.getValidityDays());
        assertTrue(saved.isActive());
    }

    @Test
    void saveAndReopenShouldPreservePriceScale() {
        LearningPackageRepository repository = createRepository();

        repository.save(createPackage("P001"));

        LearningPackage saved = createRepository()
                .findById("P001")
                .orElseThrow();

        assertEquals(new BigDecimal("15000.00"), saved.getPrice());
        assertEquals(2, saved.getPrice().scale());
    }

    @Test
    void saveAndReopenShouldPreserveInactiveStatus() {
        LearningPackageRepository repository = createRepository();
        LearningPackage learningPackage = createPackage("P001");
        learningPackage.setActive(false);

        repository.save(learningPackage);

        LearningPackage saved = createRepository()
                .findById("P001")
                .orElseThrow();

        assertFalse(saved.isActive());
    }

    @Test
    void findByIdShouldReturnEmptyForMissingId() {
        LearningPackageRepository repository = createRepository();

        assertTrue(repository.findById("P999").isEmpty());
    }

    @Test
    void updateAndReopenShouldPreserveChanges() {
        LearningPackageRepository repository = createRepository();
        repository.save(createPackage("P001"));

        LearningPackage learningPackage =
                repository.findById("P001").orElseThrow();

        learningPackage.setName("Advanced Package");
        learningPackage.setPrice(new BigDecimal("25000.00"));
        learningPackage.setActive(false);

        repository.update(learningPackage);

        LearningPackage saved = createRepository()
                .findById("P001")
                .orElseThrow();

        assertEquals("Advanced Package", saved.getName());
        assertEquals(new BigDecimal("25000.00"), saved.getPrice());
        assertFalse(saved.isActive());
    }

    @Test
    void deleteAndReopenShouldRemovePackage() {
        LearningPackageRepository repository = createRepository();
        repository.save(createPackage("P001"));

        repository.delete("P001");

        assertTrue(createRepository().findById("P001").isEmpty());
    }

    @Test
    void saveShouldRejectDuplicateId() {
        LearningPackageRepository repository = createRepository();
        repository.save(createPackage("P001"));

        assertThrows(
                Exception.class,
                () -> repository.save(createPackage("P001"))
        );
    }

    @Test
    void nextIdShouldAllocatePackageIdsAcrossInstances() {
        LearningPackageRepository repository = createRepository();

        String firstId = repository.nextId("P");
        repository.save(createPackage(firstId));

        LearningPackageRepository reopenedRepository = createRepository();
        String secondId = reopenedRepository.nextId("P");

        assertEquals("P001", firstId);
        assertEquals("P002", secondId);
    }

    @Test
    void fileShouldContainOneJsonObjectPerLine() throws Exception {
        LearningPackageRepository repository = createRepository();

        repository.save(createPackage("P001"));
        repository.save(createPackage("P002"));

        Path file = tempDirectory.resolve("packages.txt");
        List<String> lines = Files.readAllLines(file);

        assertEquals(2, lines.size());

        ObjectMapper mapper = new ObjectMapper();
        for (String line : lines) {
            assertFalse(line.isBlank());
            assertTrue(mapper.readTree(line).isObject());
        }
    }
}
