package com.example.frontrest.models;


import com.example.catalog.model.Category;
import com.example.product.model.Product;

import java.util.List;

public class CategoryResponse {

    private Category category;
    private List<Category> subCategories;
    private List<Product> products;
    private List<Category> breadcrumb;

    public CategoryResponse(Category category,
                            List<Category> subCategories,
                            List<Product> products,
                            List<Category> breadcrumb) {
        this.category = category;
        this.subCategories = subCategories;
        this.products = products;
        this.breadcrumb = breadcrumb;
    }

    public Category getCategory() {
        return category;
    }

    public List<Category> getSubCategories() {
        return subCategories;
    }

    public List<Product> getProducts() {
        return products;
    }

    public List<Category> getBreadcrumb() {
        return breadcrumb;
    }
}
