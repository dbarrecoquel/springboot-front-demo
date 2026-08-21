package com.example.shopping.enums;

public enum BasketStatus {
    
    ACTIVE("Actif"),
    COMPLETED("Completé"),
    ABANDONED("Abandonné"),
    CHECKOUT("En cours de paiement");
    
    private String label;
    
    BasketStatus(String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }
}