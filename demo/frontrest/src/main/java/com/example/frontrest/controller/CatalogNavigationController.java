package com.example.frontrest.controller;

import com.example.catalog.model.Category;
import com.example.catalog.model.ProductCategoryAssignment;
import com.example.catalog.service.CategoryService;
import com.example.catalog.service.ProductCategoryAssignmentService;
import com.example.frontrest.models.CategoryResponse;
import com.example.product.model.Product;
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

    public CatalogNavigationController(CategoryService categoryService,
                                           ProductCategoryAssignmentService assignmentService,
                                           ProductService productService) {
        this.categoryService = categoryService;
        this.assignmentService = assignmentService;
        this.productService = productService;
    }

    /* ===================== HOME ===================== */
    // Catégories racines
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getRootCategories() {
        return ResponseEntity.ok(categoryService.getRootCategories());
    }

    /* ===================== CATEGORY ===================== */
    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> viewCategory(@PathVariable Long id) {

        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Catégorie invalide : " + id));

        List<Category> subCategories = categoryService.getSubCategories(id);

        List<Product> products = assignmentService.getAssignmentsByCategoryId(id)
                .stream()
                .map(ProductCategoryAssignment::getProductId)
                .map(productService::getProductById)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());

        List<Category> breadcrumb = buildBreadcrumb(category);

        CategoryResponse response = new CategoryResponse(
                category,
                subCategories,
                products,
                breadcrumb
        );

        return ResponseEntity.ok(response);
    }

    /* ===================== BREADCRUMB ===================== */
    private List<Category> buildBreadcrumb(Category category) {
        List<Category> breadcrumb = new ArrayList<>();
        Category current = category;

        while (current != null) {
            breadcrumb.add(0, current);
            current = current.getParentCategoryId() != null
                    ? categoryService.getCategoryById(current.getParentCategoryId()).orElse(null)
                    : null;
        }
        return breadcrumb;
    }
}
