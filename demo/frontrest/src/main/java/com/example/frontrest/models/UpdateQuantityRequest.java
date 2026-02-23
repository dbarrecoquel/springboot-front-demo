package com.example.frontrest.models;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
public class UpdateQuantityRequest {
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be at least 0")
    private Integer quantity;
    
    public UpdateQuantityRequest() {
    }
    
    public UpdateQuantityRequest(Integer quantity) {
        this.quantity = quantity;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}