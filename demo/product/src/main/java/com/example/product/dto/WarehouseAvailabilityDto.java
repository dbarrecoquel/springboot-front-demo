package com.example.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseAvailabilityDto {
    
    private Long warehouseId;
    private String warehouseName;
    private String warehouseCode;
    private String country;
    private String region;
    private Double distance; // Distance estimée
    
    /**
     * Tous les produits du panier sont-ils en stock ?
     */
    private Boolean canFulfillOrder;
    
    /**
     * Détails de disponibilité par produit
     */
    private List<ProductStockDto> productAvailability;
    
    /**
     * Nombre de produits disponibles / total requis
     */
    private Integer availableProducts;
    private Integer totalProducts;
}