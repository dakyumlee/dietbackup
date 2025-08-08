package com.mydiet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.mydiet")
public class MydietApplication {

    public static void main(String[] args) {
        SpringApplication.run(MydietApplication.class, args);
    }
}