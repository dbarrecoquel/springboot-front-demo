package com.example.shippingmethod.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarrierServiceDto {
    
    private Long id;
    
    // Transporteur
    private Long carrierId;
    private String carrierName;
    private String carrierCode;
    
    // Service
    private String name;
    private String code;
    private String description;
    
    // Délais
    private Integer deliveryDays;
    private Integer processingDays;
    private LocalTime cutoffTime;
    
    // Tarification
    private Double cost = 0.0;
    private Double freeShippingMinAmount = 50.0;
    private Boolean freeShipping = false;
    
    // Statut
    @Builder.Default
    private Boolean enabled = true; // Par défaut true
    
    // Métadonnées
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed
    private Integer totalDays;
    private String deliveryEstimate;
}