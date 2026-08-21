package com.example.frontoffice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "com.example.frontoffice", 
    "com.example.product",
    "com.example.catalog",
    "com.example.user",
    "com.example.address",
    "com.example.shippingmethod",
    "com.example.shopping",
    "com.example.events",
    "com.example.payment",
    "com.example.order"
    
})
@EntityScan({
    "com.example.product.model",
    "com.example.catalog.model",
    "com.example.user.model",
    "com.example.address.model",
    "com.example.shippingmethod.model",
    "com.example.shopping.model",
    "com.example.events.model",
    "com.example.payment.model",
    "com.example.order.model"
})
@EnableJpaRepositories({
    "com.example.product.repository",
    "com.example.catalog.repository",
    "com.example.user.repository",
    "com.example.address.repository",
    "com.example.shopping.repository",
    "com.example.shippingmethod.repository",
    "com.example.events.repository",
    "com.example.payment.repository",
    "com.example.order.repository"
})
public class FrontOfficeApplication {
    public static void main(String[] args) {
        SpringApplication.run(FrontOfficeApplication.class, args);
    }
}