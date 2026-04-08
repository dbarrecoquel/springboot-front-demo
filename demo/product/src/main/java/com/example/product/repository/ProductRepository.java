package com.example.product.repository;


import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.product.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
	
	Optional<Product> findBySku(String sku);
	@Query("""
			SELECT p FROM Product p
			WHERE (:keyword IS NULL OR 
			      LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
			      LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
			AND (:minPrice IS NULL OR p.price >= :minPrice)
			AND (:maxPrice IS NULL OR p.price <= :maxPrice)
			""")
	    Page<Product> findWithFilters(
	        @Param("keyword") String search,
	        @Param("minPrice") Double minPrice,
	        @Param("maxPrice") Double maxPrice,
	        Pageable pageable);
}