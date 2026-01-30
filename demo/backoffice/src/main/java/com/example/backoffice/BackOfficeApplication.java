package com.example.backoffice;



import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication(scanBasePackages = {
    "com.example.backoffice", 
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
public class BackOfficeApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackOfficeApplication.class, args);
    }
}