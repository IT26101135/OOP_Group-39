/**
============================================================
MEMBER   : IT26101134 - [Name]
MODULE   : Bookings, FIFO queue and scheduling
FILE     : AdminBookingOperations.java
WHAT IT DOES (simple words): Applies the admin booking operations rules.
REAL-LIFE EXAMPLE: A reception waiting line handles the oldest request first.
OOP USED : Abstraction (a small interface)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/
package com.drivingschool.booking.service;

import com.drivingschool.booking.model.Booking;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

// OOP: ABSTRACTION - Callers use this promise instead of the implementation details.
// SOLID: I - This interface describes one small job for its callers.
public interface AdminBookingOperations {
    // Selects the bookings matching the administrator's schedule filters.
    List<Booking> filterBookings(HttpServletRequest request, String from, String to, String instructorId, String vehicleId, String status);
    // Returns the current unassigned FIFO waiting line for the administrator.
    List<Booking> getPendingQueue(HttpServletRequest request);
    // Tells the administrator where an unassigned request sits in the FIFO line.
    int getQueuePosition(HttpServletRequest request, String bookingId);
    // Checks conflicts before changing an existing confirmed lesson's resources.
    Booking reassign(HttpServletRequest request, String bookingId, String instructorId, String vehicleId);
    // Stores the administrator's reason when a lesson must be cancelled.
    Booking forceCancel(HttpServletRequest request, String bookingId, String reason);
    // Allows assignment only for the FIFO head and records any conflict reason.
    Booking assign(HttpServletRequest request, String bookingId, String instructorId, String vehicleId);
    // Takes only the FIFO head for assignment and checks it again before saving.
    Booking processNext(HttpServletRequest request, String instructorId, String vehicleId);
    // Marks a confirmed lesson complete through the booking rules.
    Booking complete(HttpServletRequest request, String bookingId);
}
