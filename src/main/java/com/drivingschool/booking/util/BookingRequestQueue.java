// Module owner: IT26101134
package com.drivingschool.booking.util;

import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;

import java.util.ArrayList;
import java.util.List;

/** A FIFO view of pending lesson requests, ordered by when they were created. */
public final class BookingRequestQueue {

    private static final class Node {
        private final Booking booking;
        private Node next;
        private Node(Booking booking) { this.booking = booking; }
    }

    private Node head;
    private Node tail;
    private int size;

    public BookingRequestQueue(List<Booking> bookings) {
        List<Booking> pending = new ArrayList<>();
        bookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.PENDING)
                .filter(booking -> booking.getInstructorId() == null)
                .forEach(pending::add);
        // Stable insertion sort by submission time; no library sort is used.
        for (int i = 1; i < pending.size(); i++) {
            Booking current = pending.get(i);
            int j = i - 1;
            while (j >= 0 && pending.get(j).getCreatedAt()
                    .isAfter(current.getCreatedAt())) {
                pending.set(j + 1, pending.get(j));
                j--;
            }
            pending.set(j + 1, current);
        }
        pending.forEach(this::enqueue);
    }

    public void enqueue(Booking booking) {
        if (booking == null) throw new IllegalArgumentException("Booking cannot be null.");
        Node node = new Node(booking);
        if (tail == null) head = node;
        else tail.next = node;
        tail = node;
        size++;
    }

    /** Removes and returns the oldest request, or null when the queue is empty. */
    public Booking dequeue() {
        if (head == null) return null;
        Booking first = head.booking;
        head = head.next;
        if (head == null) tail = null;
        size--;
        return first;
    }

    public Booking peek() {
        return head == null ? null : head.booking;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() { return size; }

    public int positionOf(String bookingId) {
        int position = 1;
        for (Node node = head; node != null; node = node.next, position++) {
            if (node.booking.getId().equals(bookingId)) return position;
        }
        return 0;
    }

    public List<Booking> toList() {
        List<Booking> copy = new ArrayList<>(size);
        for (Node node = head; node != null; node = node.next) copy.add(node.booking);
        return List.copyOf(copy);
    }
}
