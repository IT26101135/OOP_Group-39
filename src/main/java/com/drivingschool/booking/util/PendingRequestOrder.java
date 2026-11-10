/**
============================================================
MEMBER   : IT26101134 - [Name]
MODULE   : Bookings, FIFO queue and scheduling
FILE     : PendingRequestOrder.java
WHAT IT DOES (simple words): Provides pending request order for scheduling.
REAL-LIFE EXAMPLE: A reception waiting line handles the oldest request first.
OOP USED : Encapsulation (private fields and public methods)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/
package com.drivingschool.booking.util;

import com.drivingschool.booking.model.Booking;
import java.util.List;

// OOP: POLYMORPHISM - This order shows only the unassigned FIFO waiting line.
// SOLID: L - Like lesson-time order, this returns a safe copy without changing the input.
public class PendingRequestOrder implements BookingOrder {
    @Override
    // Keeps the order step in one place for this module.
    public List<Booking> order(List<Booking> bookings) {
        return new BookingRequestQueue(bookings).toList();
    }
}
