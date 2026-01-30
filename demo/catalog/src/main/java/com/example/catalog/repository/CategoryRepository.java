package com.example.catalog.repository;


import com.example.catalog.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentCategoryIdIsNull();
    List<Category> findByParentCategoryId(Long parentCategoryId);
}