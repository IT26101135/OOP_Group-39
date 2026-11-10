// Module owner: IT26101136
package com.drivingschool.learningpackage.config;

import com.drivingschool.learningpackage.model.LearningPackage;
import com.drivingschool.learningpackage.repository.LearningPackageRepository;
import com.drivingschool.learningpackage.service.LearningPackageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LearningPackageConfigTest {

    @TempDir
    Path tempDirectory;

    @Test
    void shouldCreateRepositoryAndServiceBeans() {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext();

        context.getEnvironment().getPropertySources().addFirst(
                new MapPropertySource(
                        "testProperties",
                        Map.of("app.data-directory", tempDirectory.toString())
                )
        );

        context.getBeanFactory().registerSingleton(
                "objectMapper",
                new ObjectMapper()
        );

        context.register(LearningPackageConfig.class);
        context.refresh();

        try {
            LearningPackageRepository repository =
                    context.getBean(LearningPackageRepository.class);

            LearningPackageService service =
                    context.getBean(LearningPackageService.class);

            assertNotNull(repository);
            assertNotNull(service);

            assertSame(service,
                    context.getBean(LearningPackageService.class));

            LearningPackage created = service.createPackage(
                    "Beginner Package",
                    "Package for beginner driving students",
                    new BigDecimal("25000.00"),
                    10,
                    30,
                    true
            );

            assertEquals("P001", created.getId());
            assertEquals(1, repository.readAllStrict().size());

        } finally {
            context.close();
        }
    }
}
