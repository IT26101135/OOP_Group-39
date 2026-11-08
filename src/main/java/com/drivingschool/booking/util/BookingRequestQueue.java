/**
============================================================
MEMBER   : IT26101134 - [Name]
MODULE   : Bookings, FIFO queue and scheduling
FILE     : BookingRequestQueue.java
WHAT IT DOES (simple words): Provides booking request queue for scheduling.
REAL-LIFE EXAMPLE: A reception waiting line handles the oldest request first.
OOP USED : Encapsulation (private head, tail and linked nodes)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/
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
        // Each node holds one request and a link to the next person in line.
        private Node(Booking booking) { this.booking = booking; }
    }

    private Node head;
    private Node tail;
    private int size;

    // Receives the helpers or validates the details needed to create this object.
    public BookingRequestQueue(List<Booking> bookings) {
        List<Booking> pending = new ArrayList<>();
        // Step 1: Only pending, unassigned requests belong in the waiting line.
        for (Booking booking : bookings) {
            if (booking.getStatus() == BookingStatus.PENDING && booking.getInstructorId() == null) {
                pending.add(booking);
            }
        }
        // Step 2: Stable insertion sort restores submission order; equal times stay in file order.
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
        // Step 3: Link the ordered requests from oldest to newest.
        for (Booking booking : pending) enqueue(booking);
    }

    // Adds a request at the back of the linked-node waiting line.
    public void enqueue(Booking booking) {
        if (booking == null) throw new IllegalArgumentException("Booking cannot be null.");
        // The new request joins the tail; it never jumps ahead of the head.
        Node node = new Node(booking);
        if (tail == null) head = node;
        else tail.next = node;
        tail = node;
        size++;
    }

    /** Removes and returns the oldest request, or null when the queue is empty. */
    // Takes the oldest request from the front and fixes the head/tail links.
    public Booking dequeue() {
        if (head == null) return null;
        Booking first = head.booking;
        head = head.next;
        // An empty line has neither a first nor a last person.
        if (head == null) tail = null;
        size--;
        return first;
    }

    // Reads the oldest request without taking it out of the waiting line.
    public Booking peek() {
        return head == null ? null : head.booking;
    }

    // Checks whether anyone is still waiting in the queue.
    public boolean isEmpty() {
        return size == 0;
    }

    // Returns how many requests are currently waiting.
    public int size() { return size; }

    // Walks the waiting line to tell a student how many requests are ahead.
    public int positionOf(String bookingId) {
        int position = 1;
        for (Node node = head; node != null; node = node.next, position++) {
            if (node.booking.getId().equals(bookingId)) return position;
        }
        return 0;
    }

    // Copies the waiting line for display without giving callers the internal nodes.
    public List<Booking> toList() {
        List<Booking> copy = new ArrayList<>(size);
        for (Node node = head; node != null; node = node.next) copy.add(node.booking);
        return List.copyOf(copy);
    }
}
