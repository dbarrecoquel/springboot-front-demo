package com.example.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    
    private Long id;
    private String orderNumber;
    private Long userId;
    private Long basketId;
    
    // Adresses
    private Long billingAddressId;
    private Long shippingAddressId;
    
    // Livraison
    private Long shippingMethodId;
    
    // Montants
    private Double subtotal;
    private Double shippingCost;
    private Double tax;
    private Double total;
    
    // Statut
    private String status;
    
    // Items
    private List<OrderProductLineItemDto> items;
    
    // Métadonnées
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}