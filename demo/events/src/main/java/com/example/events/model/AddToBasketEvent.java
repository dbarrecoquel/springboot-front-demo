package com.example.events.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddToBasketEvent {
	@JsonProperty("event_id")
    private String eventId;
    
    @JsonProperty("product_id")
    private Long productId;
    
    @JsonProperty("basket_id")
    private Long basketId;
    
    @JsonProperty("quantity")
    private Integer quantity;
    
    public AddToBasketEvent() {
    	this.eventId = UUID.randomUUID().toString();
    }
    public AddToBasketEvent(Long productId, Integer quantity, Long basketId) {
    	this();
    	this.basketId = basketId;
    	this.productId = productId;
    	this.quantity = quantity;
    }
	public String getEventId() {
		return eventId;
	}
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}
	public Long getProductId() {
		return productId;
	}
	public void setProductId(Long productId) {
		this.productId = productId;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public void setBasketId(Long basketId) {
		this.basketId = basketId;
	}
	public Long getBasketId()
	{
		return this.basketId;
	}
	
}
