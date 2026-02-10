package com.example.product.mapper;

import com.example.product.model.Product;
import com.example.product.model.dto.ProductDto;

public class ProductMapper {
	 public ProductDto toDto(Product product) {
	        if (product == null) {
	            return null;
	        }

	        ProductDto p = new ProductDto();
	        p.setId(product.getId());
	        p.setName(product.getName());
	        p.setDescription(product.getDescription());
	        p.setSku(product.getSku());
	        p.setPrice(product.getPrice());
	        return p;
	    }
}
