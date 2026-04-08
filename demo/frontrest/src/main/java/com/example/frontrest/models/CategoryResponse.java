package com.example.frontrest.models;


import com.example.catalog.model.Category;
import com.example.catalog.dto.CategoryDto;
import com.example.product.model.dto.ProductDto;

import java.util.List;

public class CategoryResponse {

    private CategoryDto category;
    private List<CategoryDto> subCategories;
    private List<CategoryDto> breadcrumb;

    public CategoryResponse(CategoryDto category,
                            List<CategoryDto> subCategories,
                            List<CategoryDto> breadcrumb) {
        this.category = category;
        this.subCategories = subCategories;
        this.breadcrumb = breadcrumb;
    }

    public CategoryDto getCategory() {
        return category;
    }

    public List<CategoryDto> getSubCategories() {
        return subCategories;
    }

    public List<CategoryDto> getBreadcrumb() {
        return breadcrumb;
    }
}
