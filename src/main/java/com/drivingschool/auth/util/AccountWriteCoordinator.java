package com.drivingschool.auth.util;

import java.util.function.Supplier;

public final class AccountWriteCoordinator {

    private AccountWriteCoordinator() {
    }

    /**
     * Coordinates account-changing operations within this application JVM.
     * Every account writer must use this method for its complete operation.
     */
    public static synchronized <T> T execute(Supplier<T> operation) {
        return operation.get();
    }
}