/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : AdminSeedRunner.java
WHAT IT DOES (simple words): Supports admin seed runner for the account or course workflow.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
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

    // Receives the helpers this class needs so each job stays separate.
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
    // Handles run for this class without changing another class's job.
    public void run(ApplicationArguments args) {
        if (enabled) {
            seedService.seedIfNeeded(name, email, password);
        }
    }
}