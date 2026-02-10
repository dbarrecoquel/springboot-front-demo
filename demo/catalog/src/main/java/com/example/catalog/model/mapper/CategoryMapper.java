package com.example.catalog.model.mapper;

import com.example.catalog.model.Category;
import com.example.catalog.dto.CategoryDto;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }

        return new CategoryDto(
                category.getId(),
                category.getName(),
                category.getParentCategoryId()
        );
    }
}
