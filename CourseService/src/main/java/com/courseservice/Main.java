package com.courseservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"com.courseservice.entity", "com.persistence.Entity"})
@EnableJpaRepositories(basePackages = {"com.courseservice.repository", "com.persistence.Repository"})

public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}