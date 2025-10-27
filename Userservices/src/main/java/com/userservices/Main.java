package com.userservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableFeignClients
@EntityScan(basePackages = {"com.userservices.entity", "com.persistence.Entity"})
@EnableJpaRepositories(basePackages = {"com.userservices.repository", "com.persistence.Repository"})
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

}