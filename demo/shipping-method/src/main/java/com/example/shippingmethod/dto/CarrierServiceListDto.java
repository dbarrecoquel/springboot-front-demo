package com.example.shippingmethod.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarrierServiceListDto {
    
    private Long id;
    private String serviceName;
    private String serviceCode;
    private String carrierName;
    private String carrierCode;
    
    // Délais (résumé)
    private Integer deliveryDays;
    private Integer processingDays;
    private Integer totalDays;
    private LocalTime cutoffTime;
    
    // Tarification
    private Double cost;
    private Double freeShippingMinAmount;
    
    // Statut
    private Boolean enabled;
    
    // Icône pour le transport
    private String deliveryIcon; // "fast" ou "standard"
}