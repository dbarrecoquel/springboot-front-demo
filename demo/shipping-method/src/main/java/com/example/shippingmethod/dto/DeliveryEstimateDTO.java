package com.example.shippingmethod.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryEstimateDTO {
    
    private Long carrierServiceId;
    private String carrierName;
    private String serviceName;
    private String serviceDescription;
    
    // Dates
    private LocalDateTime orderDate;
    private LocalDate earliestDeliveryDate;
    private LocalDate latestDeliveryDate;
    private Integer estimatedDays;
    private Integer processingDays;
    
    // Coût
    private Double cost;
    private Boolean freeShipping;
    
    // Warehouse
    private String warehouseCode;
    private String warehouseName;
    
    // Additional info
    private String cutoffTime;
    private String processingInfo;
}