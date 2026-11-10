/**
 * ============================================================
 * MEMBER   : IT26101134 - [Name]
 * MODULE   : Bookings, FIFO queue and scheduling
 * FILE     : BookingOrderTest.java
 * WHAT IT DOES (simple words): Checks submission order stays separate from lesson-time order.
 * REAL-LIFE EXAMPLE: Check a spare waiting list before handling real lesson requests.
 * OOP USED : Encapsulation, ordering polymorphism and exception handling
 * CRUD     : Tests the related booking operations using temporary data
 * ============================================================
 */
package com.drivingschool.booking.util;

import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BookingOrderTest {
    // The same ordering contract must preserve fairness in one view and diary order in the other.
    @Test
    void submissionOrderDiffersFromLessonTimeOrderWithoutChangingInput() {
        Booking older = booking("B001", 1, 7);
        Booking newer = booking("B002", 2, 6);
        List<Booking> original = List.of(newer, older);
        BookingOrder fifo = new PendingRequestOrder();
        BookingOrder diary = new LessonTimeOrder();
        assertEquals(List.of(older, newer), fifo.order(original));
        assertEquals(List.of(newer, older), diary.order(original));
        assertEquals(List.of(newer, older), original);
        assertThrows(UnsupportedOperationException.class, () -> diary.order(original).clear());
    }

    // Requests submitted at the same time keep the saved order instead of jumping the line.
    @Test
    void equalSubmissionTimesKeepFifoOrder() {
        Booking first = booking("B001", 1, 7);
        Booking second = booking("B002", 1, 6);
        BookingRequestQueue queue = new BookingRequestQueue(List.of(first, second));
        assertEquals(first, queue.dequeue());
        assertEquals(second, queue.dequeue());
        queue.enqueue(first);
        assertEquals(first, queue.peek());
        assertEquals(1, queue.positionOf(first.getId()));
    }

    // Gives requests different submission and lesson dates so the two orders can be distinguished.
    private Booking booking(String id, int submittedDay, int lessonDay) {
        LocalDateTime start = LocalDateTime.of(2030, 1, lessonDay, 10, 0);
        return new Booking(id, "S001", "E001", start, start.plusHours(1), null, null,
                BookingStatus.PENDING, LocalDateTime.of(2030, 1, submittedDay, 8, 0));
    }
}
