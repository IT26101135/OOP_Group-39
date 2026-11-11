/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Shared foundation and student accounts
FILE     : DrivesparkAcademyApplication.java
WHAT IT DOES (simple words): Starts the web application and provides the school clock.
REAL-LIFE EXAMPLE: A reception desk starts the school day and helps people find the right page.
OOP USED : Shared setup, inheritance or exception presentation as explained below
CRUD     : Supports account operations; does not add a new storage operation
============================================================
*/
package com.drivingschool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.ZoneId;

@SpringBootApplication
public class DrivesparkAcademyApplication {

    // Gives all time-dependent rules the same Colombo clock.
    @Bean
    public Clock applicationClock() {
        return Clock.system(ZoneId.of("Asia/Colombo"));
    }

    // Starts Spring and the local web server so the JSP pages can open.
    public static void main(String[] args) {
        SpringApplication.run(DrivesparkAcademyApplication.class, args);
    }

}
