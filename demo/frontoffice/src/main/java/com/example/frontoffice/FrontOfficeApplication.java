package com.example.frontoffice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "com.example.frontoffice", 
    "com.example.product",
    "com.example.catalog"
})
@EntityScan({
    "com.example.product.model",
    "com.example.catalog.model"
})
@EnableJpaRepositories({
    "com.example.product.repository",
    "com.example.catalog.repository"
})
public class FrontOfficeApplication {
    public static void main(String[] args) {
        SpringApplication.run(FrontOfficeApplication.class, args);
    }
}