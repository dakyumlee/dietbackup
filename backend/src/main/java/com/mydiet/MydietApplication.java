package com.mydiet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MydietApplication {
    public static void main(String[] args) {
        SpringApplication.run(MydietApplication.class, args);
    }
}