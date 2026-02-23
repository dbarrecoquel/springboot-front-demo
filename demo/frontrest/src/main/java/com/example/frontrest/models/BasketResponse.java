package com.example.frontrest.models;

import java.util.List;

import com.example.shopping.model.Basket;
import com.example.shopping.model.ProductLineItem;

public class BasketResponse {
    private Long basketId;
    private List<ProductLineItem> items;
    private Double total;
    private Integer itemCount;
    
    public BasketResponse() {
    }
    
    public BasketResponse(Basket basket, List<ProductLineItem> items, Double total) {
        this.basketId = basket.getId();
        this.items = items;
        this.total = total;
        this.itemCount = items.size();
    }
    
    public Long getBasketId() {
        return basketId;
    }
    
    public void setBasketId(Long basketId) {
        this.basketId = basketId;
    }
    
    public List<ProductLineItem> getItems() {
        return items;
    }
    
    public void setItems(List<ProductLineItem> items) {
        this.items = items;
    }
    
    public Double getTotal() {
        return total;
    }
    
    public void setTotal(Double total) {
        this.total = total;
    }
    
    public Integer getItemCount() {
        return itemCount;
    }
    
    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }
}