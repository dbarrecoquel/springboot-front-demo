package com.example.product.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.product.dto.ProductStockDto;
import com.example.product.dto.ProductStockWithWarehouseProjection;
import com.example.product.enums.StockStatus;
import com.example.product.mapper.ProductStockMapper;
import com.example.product.model.ProductStock;
import com.example.product.repository.ProductStockRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class ProductStockService {

	private final ProductStockRepository productStockRepository;
	private final ProductStockMapper mapper;

	@Transactional(readOnly = true)
	Optional<ProductStock> getProductStock(Long productId, Long warehouseId){
		return productStockRepository.findByProductIdAndWarehouseId( productId,  warehouseId);
	}
	
	@Transactional(readOnly = true)
    public boolean isProductInStock(Long productId, Long warehouseId, Integer quantity) {
        return productStockRepository.isProductInStock(productId, warehouseId, quantity);
    }
	@Transactional(readOnly = true)
    public List<ProductStockDto> getProductStocks(Long productId) {
        return productStockRepository.findByProductId(productId).stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }
	   
	    /**
	     * Récupérer tous les stocks d'un entrepôt
	     */
    @Transactional(readOnly = true)
    public List<ProductStockDto> getWarehouseStocks(Long warehouseId) {
        return productStockRepository.findByWarehouseId(warehouseId).stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public Optional<ProductStockDto> getProductStockDto(Long productId, Long warehouseId) {
        return productStockRepository.findByProductIdAndWarehouseId(productId, warehouseId)
            .map(mapper::toDto);
    }
	 public void deleteStock(Long productId, Long warehouseId) {
	        Optional<ProductStock> stock = productStockRepository.findByProductIdAndWarehouseId(productId, warehouseId);
	        stock.ifPresent(s -> {
	        	productStockRepository.deleteById(s.getId());
	            log.info("🗑️ Stock supprimé | Produit: {} | Entrepôt: {}", productId, warehouseId);
	        });
	}
	/**
     * Créer ou mettre à jour le stock d'un produit dans un entrepôt
     */
	public ProductStock createOrUpdateStock(Long productId, Long warehouseId, Integer quantity) {
		 Optional<ProductStock> existing = productStockRepository.findByProductIdAndWarehouseId(productId, warehouseId);
	        
	        ProductStock stock;
	        if (existing.isPresent()) {
	            stock = existing.get();
	            stock.setQuantity(quantity);
	            log.info("Stock mis à jour: Produit {} | Entrepôt {} | Qté: {}", 
	                productId, warehouseId, quantity);
	        }
	        else {
	        	stock = new ProductStock();
	        	stock.setProductId(productId);
	        	stock.setWarehouseId(warehouseId);
	        	stock.setQuantity(quantity);
	        	stock.setCreatedAt(LocalDateTime.now());
	        	log.info("Nouveau stock créé: Produit {} | Entrepôt {} | Qté: {}", 
	                    productId, warehouseId, quantity);
	        }
	        stock.setUpdatedAt(LocalDateTime.now());
	        updateStatus(stock);
	        
	        return productStockRepository.save(stock);
	}
	public ProductStockDto createOrUpdateStock(
            Long productId,
            Long warehouseId,
            Integer quantity,
            Integer minimumStock) {
        
        Optional<ProductStock> existing = productStockRepository.findByProductIdAndWarehouseId(productId, warehouseId);
        
        ProductStock stock;
        if (existing.isPresent()) {
            stock = existing.get();
            stock.setQuantity(quantity);
            stock.setMinimumStock(minimumStock);
            log.info("📝 Stock mis à jour | Produit: {} | Entrepôt: {} | Nouvelle qté: {}", 
                productId, warehouseId, quantity);
        } else {
            stock = new ProductStock();
            stock.setProductId(productId);
            stock.setWarehouseId(warehouseId);
            stock.setQuantity(quantity);
            stock.setMinimumStock(minimumStock);
            stock.setCreatedAt(LocalDateTime.now());
            log.info("📦 Nouveau stock créé | Produit: {} | Entrepôt: {} | Qté: {}", 
                productId, warehouseId, quantity);
        }
        
        stock.setUpdatedAt(LocalDateTime.now());
        updateStatus(stock);
        
        ProductStock saved = productStockRepository.save(stock);
        return mapper.toDto(saved);
	}
	public ProductStock decreaseStock(Long productId, Long warehouseId, Integer quantity) {
        ProductStock stock = productStockRepository.findByProductIdAndWarehouseId(productId, warehouseId)
            .orElseThrow(() -> new RuntimeException("Stock non trouvé"));
        
        if (stock.getQuantity() < quantity) {
            throw new RuntimeException("Stock insuffisant pour le produit " + productId);
        }
        
        stock.setQuantity(stock.getQuantity() - quantity);
        stock.setUpdatedAt(LocalDateTime.now());
        updateStatus(stock);
        
        log.info("Stock réduit: Produit {} | Entrepôt {} | Qté: {} | Nouveau stock: {}", 
            productId, warehouseId, quantity, stock.getQuantity());
        
        return productStockRepository.save(stock);
    }
	 public ProductStock increaseStock(Long productId, Long warehouseId, Integer quantity) {
	        ProductStock stock = productStockRepository.findByProductIdAndWarehouseId(productId, warehouseId)
	            .orElseThrow(() -> new RuntimeException("Stock non trouvé"));
	        
	        stock.setQuantity(stock.getQuantity() + quantity);
	        stock.setUpdatedAt(LocalDateTime.now());
	        updateStatus(stock);
	        
	        log.info("Stock augmenté: Produit {} | Entrepôt {} | Qté: {} | Nouveau stock: {}", 
	            productId, warehouseId, quantity, stock.getQuantity());
	        
	        return productStockRepository.save(stock);
	  }
	 @Transactional(readOnly = true)
	 public List<Long> findWarehouseIdsWithProduct(Long productId) {
	        return productStockRepository.findWarehouseIdsWithProduct(productId);
	 }
	 private void updateStatus(ProductStock stock) {
	        if (stock.getQuantity() <= 0) {
	            stock.setStatus(StockStatus.OUT_OF_STOCK);
	        } else if (stock.getQuantity() <= stock.getMinimumStock()) {
	            stock.setStatus(StockStatus.LOW_STOCK);
	        } else {
	            stock.setStatus(StockStatus.AVAILABLE);
	        }
	  }
	 @Transactional(readOnly = true)
	 public Page<ProductStockDto> getFilteredStocks(Long warehouseId, StockStatus status, Pageable pageable) {
        
        log.info("🔍 Filtrage des stocks | Entrepôt: {} | Statut: {} | Page: {}", 
            warehouseId, status, pageable.getPageNumber());
        
        // Récupérer les projections de la requête
        Page<ProductStockWithWarehouseProjection> projectionsPage = 
        		productStockRepository.filterStocksWithWarehouse(warehouseId, status, pageable);
        
        // Convertir les projections en ProductStockDto
        List<ProductStockDto> dtos = projectionsPage.getContent().stream()
            .map(this::projectionToDto)
            .collect(Collectors.toList());
        
        log.info("✅ {} stock(s) trouvé(s)", dtos.size());
        
        // Retourner une Page enrichie
        return new PageImpl<>(dtos, pageable, projectionsPage.getTotalElements());
	 }
	 /**
	  * Vérifier si le stock est disponible pour un produit et une quantité dans un entrepôt
	  */
	 @Transactional(readOnly = true)
	 public boolean hasStockInWarehouse(Long productId, Long warehouseId, Integer requiredQuantity) {
	     try {
	         Optional<ProductStock> stock = productStockRepository.findByProductIdAndWarehouseId(productId, warehouseId);
	         
	         if (stock.isEmpty()) {
	             log.warn("Pas de stock trouvé pour produit {} dans entrepôt {}", productId, warehouseId);
	             return false;
	         }
	         
	         ProductStock productStock = stock.get();
	         boolean hasStock = productStock.getQuantity() >= requiredQuantity;
	         
	         log.info("Vérification stock | Produit: {} | Entrepôt: {} | Quantité requise: {} | Disponible: {} | Résultat: {}",
	             productId, warehouseId, requiredQuantity, productStock.getQuantity(), hasStock);
	         
	         return hasStock;
	     } catch (Exception e) {
	         log.error("Erreur lors de la vérification du stock: {}", e.getMessage());
	         return false;
	     }
	 }

	 /**
	  * Réserver le stock pour une commande
	  */
	 @Transactional
	 public void reserveStock(Long productId, Long warehouseId, Integer quantity) {
	     Optional<ProductStock> stock = productStockRepository.findByProductIdAndWarehouseId(productId, warehouseId);
	     
	     if (stock.isPresent()) {
	         ProductStock productStock = stock.get();
	         productStock.setQuantity(productStock.getQuantity() - quantity);
	         
	         // Mettre à jour le statut
	         if (productStock.getQuantity() == 0) {
	             productStock.setStatus(StockStatus.OUT_OF_STOCK);
	         } else if (productStock.getQuantity() <= productStock.getMinimumStock()) {
	             productStock.setStatus(StockStatus.LOW_STOCK);
	         } else {
	             productStock.setStatus(StockStatus.AVAILABLE);
	         }
	         
	         productStockRepository.save(productStock);
	         
	         log.info("Stock réservé | Produit: {} | Entrepôt: {} | Quantité: {}", 
	             productId, warehouseId, quantity);
	     }
	 }
	 /**
	  * Réserver le stock pour une commande
	  */
	 private ProductStockDto projectionToDto(ProductStockWithWarehouseProjection projection) {
	        return ProductStockDto.builder()
	            .productId(projection.getProductId())
	            .warehouseId(projection.getWarehouseId())
	            .warehouseName(projection.getWarehouseName())
	            .warehouseCode(projection.getWarehouseCode())
	            .quantity(projection.getQuantity())
	            .minimumStock(projection.getMinimumStock())
	            .status(projection.getStatus())
	            .inStock(projection.getInStock())
	            .build();
	    }
}
