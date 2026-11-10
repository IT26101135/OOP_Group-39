package com.drivingschool.booking.service;

import java.util.function.Supplier;

public final class BookingWriteCoordinator {

    private static final Object LOCK = new Object();

    private BookingWriteCoordinator() {
        // Prevent object creation.
    }

    public static <T> T execute(Supplier<T> operation) {
        synchronized (LOCK) {
            return operation.get();
        }
    }
}