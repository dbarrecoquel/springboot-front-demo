package com.example.product.dto;

import com.example.product.enums.StockStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class ProductStockDto {
    
    private Long productId;
    private Long warehouseId;
    private String warehouseName;
    private String warehouseCode;
    private Integer quantity;
    private Integer minimumStock;
    private String status; // AVAILABLE, LOW_STOCK, OUT_OF_STOCK
    private Boolean inStock;
    public ProductStockDto(
            Long productId,
            Long warehouseId,
            String warehouseName,
            String warehouseCode,
            Integer quantity,
            Integer minimumStock,
            String status,
            Boolean inStock) {
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.warehouseName = warehouseName;
        this.warehouseCode = warehouseCode;
        this.quantity = quantity;
        this.minimumStock = minimumStock;
        this.status = status;
        this.inStock = inStock;
    }
}