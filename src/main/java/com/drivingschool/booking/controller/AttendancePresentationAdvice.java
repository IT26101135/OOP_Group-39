/**
 * MEMBER   : IT26101134 - [Name]
 * MODULE   : Booking attendance presentation
 * FILE     : AttendancePresentationAdvice.java
 * WHAT IT DOES: Shows when the existing attendance buttons can be used.
 * OOP USED : Encapsulation; the school clock and setting are received through the constructor.
 * CRUD     : READ only. BookingService remains responsible for every saved change.
 */
package com.drivingschool.booking.controller;

import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class AttendancePresentationAdvice {
    private final Clock clock;
    private final boolean requireLessonEnded;

    public AttendancePresentationAdvice(Clock clock,
            @Value("${app.attendance.require-lesson-ended:true}") boolean requireLessonEnded) {
        this.clock = clock;
        this.requireLessonEnded = requireLessonEnded;
    }

    @ModelAttribute("attendanceDisplay")
    public AttendancePresentationAdvice display() { return this; }

    public boolean isAvailable(Booking booking) {
        return booking != null && booking.getStatus() == BookingStatus.CONFIRMED
                && (!requireLessonEnded || !LocalDateTime.now(clock).isBefore(booking.getEndTime()));
    }

    public String availableAt(Booking booking) {
        return booking.getEndTime().format(DateTimeFormatter.ofPattern("d MMM uuuu 'at' h:mm a", Locale.UK))
                .replace("AM", "am").replace("PM", "pm");
    }
}
