package com.example.demo.repository;


import com.example.demo.model.Product;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
	
	// Optional<Product> findBySku(String sku);

}