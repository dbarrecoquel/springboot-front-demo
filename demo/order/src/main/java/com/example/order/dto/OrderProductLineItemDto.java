package com.example.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderProductLineItemDto {
    
    private Long id;
    private Long orderId;
    
    private Long productId;
    private String productName;
    private String productSku;
    
    private Integer quantity;
    private Double unitPrice;
    private Double subtotal;
}