package com.example.catalog.repository;

import com.example.catalog.model.ProductCategoryAssignment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryAssignmentRepository extends JpaRepository<ProductCategoryAssignment, Long> {
    List<ProductCategoryAssignment> findByProductId(Long productId);
    Page<ProductCategoryAssignment> findByCategoryId(Long categoryId,Pageable pageable);
    void deleteByProductIdAndCategoryId(Long productId, Long categoryId);
    boolean existsByProductIdAndCategoryId(Long productId, Long categoryId);
}