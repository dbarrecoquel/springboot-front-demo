package com.example.demo.controller;


import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
public class ProductController {
    
    private final ProductService productService;
    
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    // Display list of products
    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "products";
    }
    
    // Show form for creating new product
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        return "product-form";
    }
    
    // Save new product
    @PostMapping
    public String saveProduct(@Valid @ModelAttribute("product") Product product,
                            BindingResult result,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "product-form";
        }
        
        productService.saveProduct(product);
        redirectAttributes.addFlashAttribute("message", "Product saved successfully!");
        return "redirect:/products";
    }
    
    // Show form for editing existing product
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        return "product-form";
    }
    
    // Update existing product
    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable Long id,
                              @Valid @ModelAttribute("product") Product product,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            product.setId(id);
            return "product-form";
        }
        
        product.setId(id);
        productService.saveProduct(product);
        redirectAttributes.addFlashAttribute("message", "Product updated successfully!");
        return "redirect:/products";
    }
    
    // View product details
    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        return "product-detail";
    }
    
    // Delete product
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.deleteProduct(id);
        redirectAttributes.addFlashAttribute("message", "Product deleted successfully!");
        return "redirect:/products";
    }
}