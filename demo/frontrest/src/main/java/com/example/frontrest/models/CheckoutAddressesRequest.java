package com.example.frontrest.models;

public class CheckoutAddressesRequest {
	private Long billingAddressId;
	private Long shippingAddressId;
	public Long getBillingAddressId() {
		return billingAddressId;
	}
	public void setBillingAddressId(Long billingAddressId) {
		this.billingAddressId = billingAddressId;
	}
	public Long getShippingAddressId() {
		return shippingAddressId;
	}
	public void setShippingAddressId(Long shippingAddressId) {
		this.shippingAddressId = shippingAddressId;
	}
	
	
}
