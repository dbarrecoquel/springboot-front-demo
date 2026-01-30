package com.example.product.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.product.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
	
	// Optional<Product> findBySku(String sku);

}