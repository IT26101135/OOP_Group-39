package com.drivingschool.auth.config;

import com.drivingschool.auth.service.AdminSeedService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminSeedRunner implements ApplicationRunner {

    private final AdminSeedService seedService;
    private final boolean enabled;
    private final String name;
    private final String email;
    private final String password;

    public AdminSeedRunner(
            AdminSeedService seedService,
            @Value("${app.admin-seed.enabled:false}") boolean enabled,
            @Value("${app.admin-seed.name:System Admin}") String name,
            @Value("${app.admin-seed.email:}") String email,
            @Value("${app.admin-seed.password:}") String password) {

        this.seedService = seedService;
        this.enabled = enabled;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (enabled) {
            seedService.seedIfNeeded(name, email, password);
        }
    }
}