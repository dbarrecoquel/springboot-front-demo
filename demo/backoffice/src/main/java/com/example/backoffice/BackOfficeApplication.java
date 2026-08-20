package com.example.backoffice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "com.example.backoffice", 
    "com.example.product",
    "com.example.catalog",
    "com.example.user",
    "com.example.address",
    "com.example.shippingmethod",
    "com.example.payment"
})
@EntityScan({
    "com.example.product.model",
    "com.example.catalog.model",
    "com.example.user.model",
    "com.example.address.model",
    "com.example.shippingmethod.model",
    "com.example.payment.model"
})
@EnableJpaRepositories({
    "com.example.product.repository",
    "com.example.catalog.repository",
    "com.example.user.repository",
    "com.example.address.repository",
    "com.example.shippingmethod.repository",
    "com.example.payment.repository"
})
public class BackOfficeApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackOfficeApplication.class, args);
    }
}