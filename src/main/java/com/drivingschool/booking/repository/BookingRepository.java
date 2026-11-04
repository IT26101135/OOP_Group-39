
package com.drivingschool.booking.repository;

import com.drivingschool.booking.model.Booking;
import com.drivingschool.common.repository.FileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;

@Repository
public class BookingRepository extends FileRepository<Booking> {

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
}
