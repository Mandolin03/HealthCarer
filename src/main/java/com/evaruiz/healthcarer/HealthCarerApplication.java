package com.evaruiz.healthcarer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HealthCarerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthCarerApplication.class, args);
    }

}
