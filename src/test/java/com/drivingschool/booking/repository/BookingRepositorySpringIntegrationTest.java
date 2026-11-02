
package com.drivingschool.booking.repository;

import com.drivingschool.DrivesparkAcademyApplication;
import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookingRepositorySpringIntegrationTest {

    @TempDir
    Path directory;

    @Test
    void springInjectedObjectMapperStoresIsoDateTimes() throws Exception {

        try (ConfigurableApplicationContext context =
                     new SpringApplicationBuilder(
                             DrivesparkAcademyApplication.class
                     )
                             .web(WebApplicationType.NONE)
                             .run(
                                     "--app.data-directory=" + directory,
                                     "--app.admin-seed.enabled=false"
                             )) {

            BookingRepository repository =
                    context.getBean(BookingRepository.class);

            ObjectMapper mapper =
                    context.getBean(ObjectMapper.class);

            LocalDateTime start = LocalDateTime.of(
                    2026, 11, 5, 9, 0
            );

            LocalDateTime createdAt = LocalDateTime.of(
                    2026, 11, 1, 10, 30
            );

            Booking booking = new Booking(
                    repository.nextId("B"),
                    "S001",
                    "EN001",
                    start,
                    start.plusHours(1),
                    null,
                    null,
                    BookingStatus.PENDING,
                    createdAt
            );

            repository.save(booking);

            Booking saved = repository
                    .findByIdStrict(booking.getId())
                    .orElseThrow();

            assertEquals(booking.getId(), saved.getId());
            assertEquals(start, saved.getStartTime());
            assertEquals(start.plusHours(1), saved.getEndTime());
            assertEquals(createdAt, saved.getCreatedAt());

            Path file = directory.resolve("bookings.txt");

            assertTrue(
                    Files.exists(file),
                    "Expected bookings.txt in the temporary test directory"
            );

            List<String> lines = Files.readAllLines(
                    file,
                    StandardCharsets.UTF_8
            );

            assertEquals(1, lines.size());

            JsonNode json = mapper.readTree(lines.get(0));

            assertTrue(json.isObject());

            assertTrue(
                    json.get("startTime").isTextual(),
                    "Spring ObjectMapper should store startTime as ISO text"
            );

            assertTrue(
                    json.get("endTime").isTextual(),
                    "Spring ObjectMapper should store endTime as ISO text"
            );

            assertTrue(
                    json.get("createdAt").isTextual(),
                    "Spring ObjectMapper should store createdAt as ISO text"
            );

            assertEquals(
                    start,
                    LocalDateTime.parse(json.get("startTime").asText())
            );

            assertEquals(
                    start.plusHours(1),
                    LocalDateTime.parse(json.get("endTime").asText())
            );

            assertEquals(
                    createdAt,
                    LocalDateTime.parse(json.get("createdAt").asText())
            );
        }
    }
}
