package com.drivingschool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.ZoneId;

@SpringBootApplication
public class DrivesparkAcademyApplication {

    @Bean
    public Clock applicationClock() {
        return Clock.system(ZoneId.of("Asia/Colombo"));
    }

    public static void main(String[] args) {
        SpringApplication.run(DrivesparkAcademyApplication.class, args);
    }

}
