package com.example.product.service;


import com.example.product.model.Product;
import com.example.product.repository.ProductRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {
    
    private final ProductRepository productRepository;
    
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
    
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
    
    public Optional<Product> getProductBySku(String sku) {
    	
    	return productRepository.findBySku(sku);
    }
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public Page<Product> findWithFilters(String keyword,
            Double minPrice, Double maxPrice,
            Pageable pageable) {

        return productRepository.findWithFilters(keyword, minPrice, maxPrice, pageable);
    }

	public List<Product> findAllByIds(List<Long> productIds) {
		return productRepository.findAllById(productIds);
	}
}