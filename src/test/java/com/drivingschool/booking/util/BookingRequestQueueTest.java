// Module owner: IT26101134
package com.drivingschool.booking.util;

import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BookingRequestQueueTest {
    private Booking booking(String id, BookingStatus status, LocalDateTime created) {
        return new Booking(id, "S001", "E001", created.plusDays(2), created.plusDays(2).plusHours(1),
                null, null, status, created);
    }
    @Test void supportsEmptyPeekDequeueAndSize() {
        BookingRequestQueue q = new BookingRequestQueue(List.of());
        assertTrue(q.isEmpty()); assertEquals(0, q.size()); assertNull(q.peek()); assertNull(q.dequeue());
    }
    @Test void enqueueDequeuePreservesFifoAndTailAfterEmptying() {
        BookingRequestQueue q = new BookingRequestQueue(List.of());
        Booking a = booking("B001", BookingStatus.PENDING, LocalDateTime.parse("2030-01-01T08:00"));
        Booking b = booking("B002", BookingStatus.PENDING, LocalDateTime.parse("2030-01-01T09:00"));
        q.enqueue(a); q.enqueue(b);
        assertEquals(a, q.peek()); assertEquals(2, q.size());
        assertEquals(a, q.dequeue()); assertEquals(b, q.dequeue()); assertTrue(q.isEmpty());
        q.enqueue(a); assertEquals(a, q.dequeue());
    }
    @Test void ordersPendingByCreationAndSkipsCancelledAndAssignedRequests() {
        Booking later = booking("B001", BookingStatus.PENDING, LocalDateTime.parse("2030-01-01T10:00"));
        Booking oldest = booking("B002", BookingStatus.PENDING, LocalDateTime.parse("2030-01-01T08:00"));
        Booking cancelled = booking("B003", BookingStatus.CANCELLED, LocalDateTime.parse("2030-01-01T07:00"));
        BookingRequestQueue q = new BookingRequestQueue(List.of(later, cancelled, oldest));
        assertEquals(List.of(oldest, later), q.toList()); assertEquals(0, q.positionOf("B003"));
        assertEquals(1, q.positionOf("B002")); assertEquals(2, q.positionOf("B001"));
    }
}
