package com.example.product.enums;

public enum StockStatus {

	AVAILABLE("En stock"),
	LOW_STOCK("Stock faible"),
	OUT_OF_STOCK("Pas de stock");
	
	private String label;
	
    StockStatus(String name) {
		this.label = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
    
    public boolean isInStock() {
    	return this == AVAILABLE;
    }
    
    public boolean isLowStock() {
    	return this == LOW_STOCK;
    }
	
    public boolean isOutOfStock() {
    	return this == OUT_OF_STOCK;
    }
	
}
