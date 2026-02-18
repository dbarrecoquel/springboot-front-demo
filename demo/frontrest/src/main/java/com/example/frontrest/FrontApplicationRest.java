package com.example.frontrest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
	    "com.example.frontrest", 
	    "com.example.product",
	    "com.example.catalog",
	    "com.example.user",
	    "com.example.address",
	    "com.example.shopping",
	    "com.example.events" 
	})
	@EntityScan({
	    "com.example.product.model",
	    "com.example.catalog.model",
	    "com.example.user.model",
	    "com.example.address.model",
	    "com.example.shopping.model",
	    "com.example.events.model"
	})
	@EnableJpaRepositories({
	    "com.example.product.repository",
	    "com.example.catalog.repository",
	    "com.example.user.repository",
	    "com.example.address.repository",
	    "com.example.shopping.repository",
	    "com.example.events.repository"
	})
	public class FrontApplicationRest {
	    public static void main(String[] args) {
	        SpringApplication.run(FrontApplicationRest.class, args);
	    }
	}