/**
============================================================
MEMBER   : IT26101134 - [Name]
MODULE   : Bookings, FIFO queue and scheduling
FILE     : StudentBookingOperations.java
WHAT IT DOES (simple words): Applies the student booking operations rules.
REAL-LIFE EXAMPLE: A reception waiting line handles the oldest request first.
OOP USED : Abstraction (a small interface)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/
package com.drivingschool.booking.service;

import com.drivingschool.booking.model.Booking;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

// OOP: ABSTRACTION - Callers use this promise instead of the implementation details.
// SOLID: I - This interface describes one small job for its callers.
public interface StudentBookingOperations {
    // Lists only this student's requests in lesson-time order.
    List<Booking> getOwnBookings(HttpServletRequest request);
    // Shows a queue position only after checking the request belongs to this student.
    int getOwnQueuePosition(HttpServletRequest request, String bookingId);
    // Checks the student, time and enrollment before saving a pending lesson request.
    Booking requestLesson(HttpServletRequest request, String packageId, String instructorId, LocalDateTime startTime);
    // Cancels an owned request only when the configured notice period permits it.
    void cancelOwnBooking(HttpServletRequest request, String bookingId);
    // Validates a replacement request before cancelling the old one; it joins the queue tail.
    Booking rescheduleOwnBooking(HttpServletRequest request, String bookingId, LocalDateTime startTime);
    // Removes only this student's already-cancelled request.
    void deleteCancelledBooking(HttpServletRequest request, String bookingId);
}
