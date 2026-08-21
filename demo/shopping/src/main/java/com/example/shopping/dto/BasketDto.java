package com.example.shopping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasketDto {
    
    private Long id;
    private Long userId;
    private String sessionId;
    
    // Adresses
    private Long billingAddressId;
    private Long shippingAddressId;
    
    // Entrepôt
    private Long warehouseId;
    
    // Livraison
    private Long carrierServiceId;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime latestDeliveryDate;
    
    // Paiement
    private Long paymentMethodId;
    
    // Métadonnées
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    
    // Articles
    private List<ProductLineItemDto> items;
}