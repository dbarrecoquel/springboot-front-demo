package com.example.shippingmethod.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO léger pour représenter un item du panier
 * Pas de dépendance vers shopping
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasketItemDto {
    
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double unitPrice;
}