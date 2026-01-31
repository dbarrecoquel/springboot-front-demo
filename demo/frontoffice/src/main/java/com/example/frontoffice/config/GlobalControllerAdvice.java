package com.example.frontoffice.config;

import com.example.catalog.model.Category;
import com.example.catalog.service.CategoryService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {
    
    private final CategoryService categoryService;
    
    public GlobalControllerAdvice(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    
    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        List<Category> rootCategories = categoryService.getRootCategories();
        model.addAttribute("rootCategories", rootCategories);
    }
}