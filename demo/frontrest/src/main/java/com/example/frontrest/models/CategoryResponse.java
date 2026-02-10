package com.example.frontrest.models;


import com.example.catalog.model.Category;
import com.example.catalog.dto.CategoryDto;
import com.example.product.model.dto.ProductDto;

import java.util.List;

public class CategoryResponse {

    private Category category;
    private List<CategoryDto> subCategories;
    private List<ProductDto> products;
    private List<CategoryDto> breadcrumb;

    public CategoryResponse(Category category,
                            List<CategoryDto> subCategories,
                            List<ProductDto> products,
                            List<CategoryDto> breadcrumb) {
        this.category = category;
        this.subCategories = subCategories;
        this.products = products;
        this.breadcrumb = breadcrumb;
    }

    public Category getCategory() {
        return category;
    }

    public List<CategoryDto> getSubCategories() {
        return subCategories;
    }

    public List<ProductDto> getProducts() {
        return products;
    }

    public List<CategoryDto> getBreadcrumb() {
        return breadcrumb;
    }
}
