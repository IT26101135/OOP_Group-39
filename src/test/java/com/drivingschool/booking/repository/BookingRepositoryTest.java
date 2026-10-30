
package com.drivingschool.booking.repository;

import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingRepositoryTest {

    @TempDir
    Path directory;

    private BookingRepository createRepository() {
        return new BookingRepository(
                directory.toString(),
                new ObjectMapper().findAndRegisterModules()
        );
    }

    private Booking createPendingBooking(String id) {
        LocalDateTime start = LocalDateTime.of(
                2026, 11, 1, 9, 0
        );

        return new Booking(
                id,
                "S001",
                "EN001",
                start,
                start.plusHours(1),
                null,
                null,
                BookingStatus.PENDING,
                LocalDateTime.of(2026, 10, 30, 10, 0)
        );
    }

    @Test
    void savesAndReopensBookingWithAllFields() {
        BookingRepository repository = createRepository();

        String id = repository.nextId("B");
        Booking booking = createPendingBooking(id);

        repository.save(booking);

        BookingRepository reopened = createRepository();

        Booking saved = reopened.findById(id).orElseThrow();

        assertEquals(id, saved.getId());
        assertEquals("S001", saved.getStudentId());
        assertEquals("EN001", saved.getEnrollmentId());
        assertEquals(booking.getStartTime(), saved.getStartTime());
        assertEquals(booking.getEndTime(), saved.getEndTime());
        assertEquals(BookingStatus.PENDING, saved.getStatus());
        assertNull(saved.getInstructorId());
        assertNull(saved.getVehicleId());
        assertEquals(booking.getCreatedAt(), saved.getCreatedAt());
    }

    @Test
    void savesConfirmedBookingWithAssignments() {
        BookingRepository repository = createRepository();

        LocalDateTime start = LocalDateTime.of(
                2026, 11, 2, 10, 0
        );

        Booking booking = new Booking(
                repository.nextId("B"),
                "S001",
                "EN001",
                start,
                start.plusHours(1),
                "I001",
                "V001",
                BookingStatus.CONFIRMED,
                LocalDateTime.of(2026, 10, 30, 10, 0)
        );

        repository.save(booking);

        Booking saved = createRepository()
                .findById(booking.getId())
                .orElseThrow();

        assertEquals("I001", saved.getInstructorId());
        assertEquals("V001", saved.getVehicleId());
        assertEquals(BookingStatus.CONFIRMED, saved.getStatus());
    }

    @Test
    void updatesAndReopensBooking() {
        BookingRepository repository = createRepository();

        String id = repository.nextId("B");
        Booking booking = createPendingBooking(id);

        repository.save(booking);

        booking.confirm("I001", "V001");
        repository.update(booking);

        Booking saved = createRepository()
                .findById(id)
                .orElseThrow();

        assertEquals(BookingStatus.CONFIRMED, saved.getStatus());
        assertEquals("I001", saved.getInstructorId());
        assertEquals("V001", saved.getVehicleId());
    }

    @Test
    void deletesBookingPermanently() {
        BookingRepository repository = createRepository();

        String id = repository.nextId("B");
        repository.save(createPendingBooking(id));

        repository.delete(id);

        assertTrue(createRepository().findById(id).isEmpty());
    }

    @Test
    void rejectsDuplicateBookingIds() {
        BookingRepository repository = createRepository();

        repository.save(createPendingBooking("B001"));

        assertThrows(
                DuplicateRecordException.class,
                () -> repository.save(
                        createPendingBooking("B001")
                )
        );
    }

    @Test
    void generatesUniqueIdsAcrossRepositoryInstances() {
        BookingRepository first = createRepository();

        assertEquals("B001", first.nextId("B"));

        BookingRepository second = createRepository();

        assertEquals("B002", second.nextId("B"));
        assertEquals("B003", first.nextId("B"));
    }

    @Test
    void storesOneJsonObjectPerLine() throws Exception {
        BookingRepository repository = createRepository();

        repository.save(createPendingBooking(repository.nextId("B")));
        repository.save(createPendingBooking(repository.nextId("B")));

        Path file = directory.resolve("bookings.txt");

        assertEquals(2, Files.readAllLines(file).size());
    }
}
