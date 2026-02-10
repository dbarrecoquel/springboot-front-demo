package com.example.frontrest.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.product.model.Product;
import com.example.product.service.ProductService;

@RestController
@RequestMapping("/api/catalog")
@CrossOrigin(origins = "*")
public class CatalogController {
    
    private final ProductService productService;
    
    public CatalogController(ProductService productService) {
        this.productService = productService;
    }
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("FrontRest API is running!");
    }
    // Afficher tous les produits
    @GetMapping
    public ResponseEntity<List<Product>> showCatalog() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
    
    // Voir les détails d'un produit
    @GetMapping("/{id}")
    public ResponseEntity<Optional<Product>> viewProduct(@PathVariable Long id) {
    	 return ResponseEntity.ok(productService.getProductById(id));
         
    }
}
