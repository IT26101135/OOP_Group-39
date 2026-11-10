/**
============================================================
MEMBER   : IT26101134 - [Name]
MODULE   : Bookings, FIFO queue and scheduling
FILE     : BookingOrder.java
WHAT IT DOES (simple words): Provides booking order for scheduling.
REAL-LIFE EXAMPLE: A reception waiting line handles the oldest request first.
OOP USED : Abstraction (a small interface)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/
package com.drivingschool.booking.util;

import com.drivingschool.booking.model.Booking;
import java.util.List;

// OOP: ABSTRACTION - Callers ask for an ordered view without knowing how it is built.
// SOLID: I - This ordering contract needs just one operation.
// SOLID: O - Another order can be added without changing existing ordering classes.
// OOP: ABSTRACTION - Callers use this promise instead of the implementation details.
// SOLID: I - This interface describes one small job for its callers.
public interface BookingOrder {
    // Provides the order operation needed by this caller.
    List<Booking> order(List<Booking> bookings);
}
