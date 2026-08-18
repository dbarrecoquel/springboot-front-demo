package com.example.product.dto;

/**
 * Projection pour les requêtes JPQL
 * Retourne les données sans avoir besoin d'un constructor
 */
public interface ProductStockWithWarehouseProjection {
    
    Long getProductId();
    Long getWarehouseId();
    String getWarehouseName();
    String getWarehouseCode();
    Integer getQuantity();
    Integer getMinimumStock();
    String getStatus();
    Boolean getInStock();
}