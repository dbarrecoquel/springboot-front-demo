package com.example.product.mapper;

import org.springframework.stereotype.Component;

import com.example.product.dto.ProductDto;
import com.example.product.model.Product;
@Component
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
