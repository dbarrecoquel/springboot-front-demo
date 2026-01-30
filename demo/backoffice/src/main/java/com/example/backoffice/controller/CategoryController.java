package com.example.backoffice.controller;

import com.example.catalog.model.Category;
import com.example.catalog.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController {
    
    private final CategoryService categoryService;
    
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    
    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("rootCategories", categoryService.getRootCategories());
        return "categories";
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("allCategories", categoryService.getAllCategories());
        return "category-form";
    }
    
    @PostMapping
    public String saveCategory(@Valid @ModelAttribute("category") Category category,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("allCategories", categoryService.getAllCategories());
            return "category-form";
        }
        
        categoryService.saveCategory(category);
        redirectAttributes.addFlashAttribute("message", "Catégorie enregistrée avec succès !");
        return "redirect:/categories";
    }
    
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID de catégorie invalide:" + id));
        model.addAttribute("category", category);
        model.addAttribute("allCategories", categoryService.getAllCategories());
        return "category-form";
    }
    
    @PostMapping("/update/{id}")
    public String updateCategory(@PathVariable Long id,
                               @Valid @ModelAttribute("category") Category category,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (result.hasErrors()) {
            category.setId(id);
            model.addAttribute("allCategories", categoryService.getAllCategories());
            return "category-form";
        }
        
        category.setId(id);
        categoryService.saveCategory(category);
        redirectAttributes.addFlashAttribute("message", "Catégorie mise à jour avec succès !");
        return "redirect:/categories";
    }
    
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        categoryService.deleteCategory(id);
        redirectAttributes.addFlashAttribute("message", "Catégorie supprimée avec succès !");
        return "redirect:/categories";
    }
}