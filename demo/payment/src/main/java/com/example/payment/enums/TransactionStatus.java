package com.example.payment.enums;

public enum TransactionStatus {
    
    PENDING("En attente"),
    COMPLETED("Complétée"),
    FAILED("Échouée"),
    CANCELLED("Annulée"),
    REFUNDED("Remboursée");
    
    private String label;
    
    TransactionStatus(String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }
}