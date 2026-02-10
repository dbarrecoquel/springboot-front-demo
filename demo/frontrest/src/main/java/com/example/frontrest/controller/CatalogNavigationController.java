package com.example.frontrest.controller;

import com.example.catalog.model.Category;
import com.example.catalog.dto.CategoryDto;
import com.example.catalog.model.ProductCategoryAssignment;
import com.example.catalog.model.mapper.CategoryMapper;
import com.example.catalog.service.CategoryService;
import com.example.catalog.service.ProductCategoryAssignmentService;
import com.example.frontrest.models.CategoryResponse;
import com.example.product.mapper.ProductMapper;
import com.example.product.model.Product;
import com.example.product.model.dto.ProductDto;
import com.example.product.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class CatalogNavigationController {

    private final CategoryService categoryService;
    private final ProductCategoryAssignmentService assignmentService;
    private final ProductService productService;

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    public CatalogNavigationController(CategoryService categoryService,
                                           ProductCategoryAssignmentService assignmentService,
                                           ProductService productService,
                                           CategoryMapper categoryMapper,
                                           ProductMapper productMapper) {
        this.categoryService = categoryService;
        this.assignmentService = assignmentService;
        this.productService = productService;
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
    }

    /* ===================== HOME ===================== */
    // Catégories racines
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getRootCategories() {
        return ResponseEntity.ok(
                categoryService.getRootCategories()
                        .stream()
                        .map(categoryMapper::toDto)
                        .toList()
        );
    }

    /* ===================== CATEGORY ===================== */
    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> viewCategory(@PathVariable Long id) {

        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Catégorie invalide : " + id));

        List<CategoryDto> subCategories = categoryService.getSubCategories(id).stream()
                .map(categoryMapper::toDto)
                .toList();
        
        List<ProductDto> products = assignmentService.getAssignmentsByCategoryId(id)
                .stream()
                .map(ProductCategoryAssignment::getProductId)
                .map(productService::getProductById)
                .flatMap(Optional::stream)
                .map(productMapper::toDto)
                .collect(Collectors.toList());

        List<CategoryDto> breadcrumb = buildBreadcrumb(category);

        CategoryResponse response = new CategoryResponse(
                category,
                subCategories,
                products,
                breadcrumb
        );

        return ResponseEntity.ok(response);
    }

    /* ===================== BREADCRUMB ===================== */
    private List<CategoryDto> buildBreadcrumb(Category category) {
        List<CategoryDto> breadcrumb = new ArrayList<>();
        Category current = category;

        while (current != null) {
            breadcrumb.add(0, categoryMapper.toDto(current));
            current = current.getParentCategoryId() != null
                    ? categoryService.getCategoryById(current.getParentCategoryId()).orElse(null)
                    : null;
        }
        return breadcrumb;
    }
}
