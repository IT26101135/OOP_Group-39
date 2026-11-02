package com.drivingschool.instructor.config;

import com.drivingschool.instructor.model.Instructor;
import com.drivingschool.instructor.repository.InstructorRepository;
import com.drivingschool.instructor.service.InstructorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InstructorConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void configRegistersSingletonBeansAndPersistsInstructor() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext()) {

            context.getEnvironment().getPropertySources().addFirst(
                    new MapPropertySource(
                            "testProperties",
                            Map.of("app.data-directory", tempDir.toString())
                    )
            );

            context.registerBean(ObjectMapper.class, () -> new ObjectMapper());
            context.register(InstructorConfig.class);
            context.refresh();

            InstructorRepository repository =
                    context.getBean(InstructorRepository.class);

            InstructorService service =
                    context.getBean(InstructorService.class);

            assertNotNull(repository);
            assertNotNull(service);

            assertSame(repository, context.getBean(InstructorRepository.class));
            assertSame(service, context.getBean(InstructorService.class));

            Instructor created = service.createInstructor(
                    "Test Instructor",
                    "instructor@example.com",
                    "+94771234567",
                    5
            );

            InstructorRepository reopenedRepository =
                    new InstructorRepository(tempDir, new ObjectMapper());

            Instructor restored = reopenedRepository
                    .findByIdStrict(created.getId())
                    .orElseThrow();

            assertEquals(created.getId(), restored.getId());
            assertEquals(created.getName(), restored.getName());
            assertEquals(created.getEmail(), restored.getEmail());
            assertEquals(
                    created.getYearsOfExperience(),
                    restored.getYearsOfExperience()
            );
        }
    }
}
