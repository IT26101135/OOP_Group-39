package com.drivingschool.auth.config;

import com.drivingschool.auth.service.AdminSeedService;
import com.drivingschool.common.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminSeedRunnerTest {

    @Test
    void disabledRunnerDoesNotCallSeedService() {
        AdminSeedService service = mock(AdminSeedService.class);

        AdminSeedRunner runner = new AdminSeedRunner(
                service, false, "System Admin", "", ""
        );

        runner.run(new DefaultApplicationArguments(new String[0]));

        verifyNoInteractions(service);
    }

    @Test
    void enabledRunnerPassesConfiguredDetailsToService() {
        AdminSeedService service = mock(AdminSeedService.class);

        AdminSeedRunner runner = new AdminSeedRunner(
                service, true,
                "System Admin", "admin@example.com", "TestPassword123!"
        );

        runner.run(new DefaultApplicationArguments(new String[0]));

        verify(service).seedIfNeeded(
                "System Admin", "admin@example.com", "TestPassword123!"
        );
    }

    @Test
    void seedFailurePropagatesInsteadOfBeingSilentlyIgnored() {
        AdminSeedService service = mock(AdminSeedService.class);

        ValidationException failure =
                new ValidationException("Administrator seed email is required.");

        when(service.seedIfNeeded("System Admin", "", ""))
                .thenThrow(failure);

        AdminSeedRunner runner = new AdminSeedRunner(
                service, true, "System Admin", "", ""
        );

        assertSame(failure, assertThrows(
                ValidationException.class,
                () -> runner.run(
                        new DefaultApplicationArguments(new String[0])
                )
        ));
    }
}