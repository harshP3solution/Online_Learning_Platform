package com.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"com.authservice.entity", "com.persistence.Entity"})
@EnableJpaRepositories(basePackages = {"com.authservice.repository", "com.persistence.Repository"})
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);

    }
}