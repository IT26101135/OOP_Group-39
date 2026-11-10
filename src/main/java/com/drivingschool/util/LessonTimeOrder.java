/**
============================================================
MEMBER   : IT26101134 - [Name]
MODULE   : Bookings, FIFO queue and scheduling
FILE     : LessonTimeOrder.java
WHAT IT DOES (simple words): Provides lesson time order for scheduling.
REAL-LIFE EXAMPLE: A reception waiting line handles the oldest request first.
OOP USED : Encapsulation (private fields and public methods)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/
package com.drivingschool.booking.util;

import com.drivingschool.booking.model.Booking;
import java.util.ArrayList;
import java.util.List;

// OOP: POLYMORPHISM - This order shows lessons by their start time, not submission time.
// SOLID: L - Like the FIFO order, this returns a safe copy without changing the caller's list.
public class LessonTimeOrder implements BookingOrder {
    @Override
    // Keeps the order step in one place for this module.
    public List<Booking> order(List<Booking> bookings) {
        List<Booking> sorted = new ArrayList<>(bookings);
        // Move each lesson left until it reaches its chronological place.
        for (int index = 1; index < sorted.size(); index++) {
            Booking current = sorted.get(index);
            int previous = index - 1;
            while (previous >= 0 && sorted.get(previous).getStartTime().isAfter(current.getStartTime())) {
                sorted.set(previous + 1, sorted.get(previous));
                previous--;
            }
            sorted.set(previous + 1, current);
        }
        return List.copyOf(sorted);
    }
}
