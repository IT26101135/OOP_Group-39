/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Shared foundation and student accounts
FILE     : ServletInitializer.java
WHAT IT DOES (simple words): Connects a packaged WAR to its application entry point.
REAL-LIFE EXAMPLE: A reception desk starts the school day and helps people find the right page.
OOP USED : Shared setup, inheritance or exception presentation as explained below
CRUD     : Supports account operations; does not add a new storage operation
============================================================
*/
package com.drivingschool;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

// OOP: INHERITANCE - Reuse Spring's WAR startup class when an external servlet server starts the app.
public class ServletInitializer extends SpringBootServletInitializer {

    // Selects the same application entry point when the WAR is deployed to a server.
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(DrivesparkAcademyApplication.class);
    }

}
