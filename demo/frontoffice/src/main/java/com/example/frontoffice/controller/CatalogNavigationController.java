package com.example.frontoffice.controller;

import com.example.catalog.model.Category;
import com.example.catalog.model.ProductCategoryAssignment;
import com.example.catalog.service.CategoryService;
import com.example.catalog.service.ProductCategoryAssignmentService;
import com.example.product.model.Product;
import com.example.product.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.stream.Collectors;

@Controller
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
    
    // Page d'accueil avec les catégories racines
    @GetMapping("/")
    public String home(Model model) {
        List<Category> rootCategories = categoryService.getRootCategories();
        model.addAttribute("rootCategories", rootCategories);
        return "home";
    }
    
    // Navigation dans une catégorie
    @GetMapping("/category/{id}")
    public String viewCategory(@PathVariable Long id, Model model) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Catégorie invalide : " + id));
        
        // Récupérer les sous-catégories
        List<Category> subCategories = categoryService.getSubCategories(id);
        
        // Récupérer les produits de cette catégorie
        List<ProductCategoryAssignment> assignments = assignmentService.getAssignmentsByCategoryId(id);
        List<Product> products = assignments.stream()
                .map(assignment -> productService.getProductById(assignment.getProductId()).orElse(null))
                .filter(product -> product != null)
                .collect(Collectors.toList());
        
        // Récupérer le chemin de navigation (breadcrumb)
        List<Category> breadcrumb = buildBreadcrumb(category);
        
        model.addAttribute("category", category);
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("products", products);
        model.addAttribute("breadcrumb", breadcrumb);
        
        return "category-view";
    }
    
    // Construire le fil d'Ariane (breadcrumb)
    private List<Category> buildBreadcrumb(Category category) {
        List<Category> breadcrumb = new java.util.ArrayList<>();
        Category current = category;
        
        while (current != null) {
            breadcrumb.add(0, current);
            if (current.getParentCategoryId() != null) {
                current = categoryService.getCategoryById(current.getParentCategoryId()).orElse(null);
            } else {
                current = null;
            }
        }
        
        return breadcrumb;
    }
}