package com.example.frontoffice.controller;

import com.example.product.model.Product;
import com.example.product.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/catalog")
public class CatalogController {
    
    private final ProductService productService;
    
    public CatalogController(ProductService productService) {
        this.productService = productService;
    }
    
    // Afficher tous les produits
    @GetMapping
    public String showCatalog(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "catalog";
    }
    
    // Voir les détails d'un produit
    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produit invalide : " + id));
        model.addAttribute("product", product);
        return "product-detail";
    }
}