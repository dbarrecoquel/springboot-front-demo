package com.example.catalog.service;


import com.example.catalog.model.ProductCategoryAssignment;
import com.example.catalog.repository.ProductCategoryAssignmentRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductCategoryAssignmentService {
    
    private final ProductCategoryAssignmentRepository assignmentRepository;
    
    public ProductCategoryAssignmentService(ProductCategoryAssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }
    
    public List<ProductCategoryAssignment> getAssignmentsByProductId(Long productId) {
        return assignmentRepository.findByProductId(productId);
    }
    
    public Page<ProductCategoryAssignment> getAssignmentsByCategoryId(Long categoryId, Pageable pageable) {
        return assignmentRepository.findByCategoryId(categoryId, pageable);
    }
    
    public ProductCategoryAssignment assignProductToCategory(Long productId, Long categoryId) {
        if (!assignmentRepository.existsByProductIdAndCategoryId(productId, categoryId)) {
            ProductCategoryAssignment assignment = new ProductCategoryAssignment(productId, categoryId);
            return assignmentRepository.save(assignment);
        }
        return null; // Déjà assigné
    }
    
    public void removeProductFromCategory(Long productId, Long categoryId) {
        assignmentRepository.deleteByProductIdAndCategoryId(productId, categoryId);
    }
    
    public List<ProductCategoryAssignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }
}