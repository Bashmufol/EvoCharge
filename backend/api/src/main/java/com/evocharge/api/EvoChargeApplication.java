package com.evocharge.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Spring Boot entry point. Enables scheduled network pulse updates. */
@SpringBootApplication
@EnableScheduling
public class EvoChargeApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvoChargeApplication.class, args);
    }
}
