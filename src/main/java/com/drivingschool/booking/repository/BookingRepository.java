/**
============================================================
MEMBER   : IT26101134 - [Name]
MODULE   : Bookings, FIFO queue and scheduling
FILE     : BookingRepository.java
WHAT IT DOES (simple words): Saves and reads booking records.
REAL-LIFE EXAMPLE: A reception waiting line handles the oldest request first.
OOP USED : Inheritance (reuses FileRepository)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/

package com.drivingschool.booking.repository;

import com.drivingschool.booking.model.Booking;
import com.drivingschool.common.repository.FileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.List;

@Repository
// SOLID: S - This class is responsible for file storage.
public class BookingRepository extends FileRepository<Booking> {

    // Receives the helpers or validates the details needed to create this object.
    public BookingRepository(
            @Value("${app.data-directory:./data}") String dataDirectory,
            ObjectMapper objectMapper) {

        super(
                Path.of(dataDirectory),
                "bookings.txt",
                Booking.class,
                objectMapper
        );
    }
    // OOP: INHERITANCE - Bookings reuse the shared FileRepository safety checks.
    // SOLID: L - Each override keeps the parent's behaviour for validation and file errors.
    @Override
    // Adds a new record using the shared safe file writer.
    // CRUD: CREATE - adds a new booking and saves it
    public void save(Booking booking) { super.save(booking); }

    @Override
    // Reads the complete filing drawer, refusing damaged data before safety decisions.
    // CRUD: READ   - finds or lists booking
    public List<Booking> readAllStrict() { return super.readAllStrict(); }

    @Override
    // Replaces the selected record while keeping its permanent ID.
    // CRUD: UPDATE - changes an existing booking
    public void update(Booking booking) { super.update(booking); }

    @Override
    // Removes a record only after the service has checked its rules.
    // CRUD: DELETE - removes a booking
    public void delete(String bookingId) { super.delete(bookingId); }
}
