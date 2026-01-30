package com.example.backoffice.controller;


import com.example.catalog.service.CategoryService;
import com.example.catalog.service.ProductCategoryAssignmentService;
import com.example.product.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/assignments")
public class AssignmentController {
    
    private final ProductCategoryAssignmentService assignmentService;
    private final ProductService productService;
    private final CategoryService categoryService;
    
    public AssignmentController(ProductCategoryAssignmentService assignmentService,
                              ProductService productService,
                              CategoryService categoryService) {
        this.assignmentService = assignmentService;
        this.productService = productService;
        this.categoryService = categoryService;
    }
    
    @GetMapping
    public String listAssignments(Model model) {
        model.addAttribute("assignments", assignmentService.getAllAssignments());
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "assignments";
    }
    
    @PostMapping("/assign")
    public String assignProductToCategory(@RequestParam Long productId,
                                         @RequestParam Long categoryId,
                                         RedirectAttributes redirectAttributes) {
        assignmentService.assignProductToCategory(productId, categoryId);
        redirectAttributes.addFlashAttribute("message", "Produit assigné à la catégorie avec succès !");
        return "redirect:/assignments";
    }
    
    @GetMapping("/remove/{productId}/{categoryId}")
    public String removeAssignment(@PathVariable Long productId,
                                  @PathVariable Long categoryId,
                                  RedirectAttributes redirectAttributes) {
        assignmentService.removeProductFromCategory(productId, categoryId);
        redirectAttributes.addFlashAttribute("message", "Assignation supprimée avec succès !");
        return "redirect:/assignments";
    }
}