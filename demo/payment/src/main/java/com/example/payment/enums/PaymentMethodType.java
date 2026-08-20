package com.example.payment.enums;


public enum PaymentMethodType {
    
    COD("Paiement à la livraison"),
    CREDIT_CARD("Carte bancaire"),
    PAYPAL("PayPal"),
    STRIPE("Stripe"),
    BANK_TRANSFER("Virement bancaire");
    
    private String label;
    
    PaymentMethodType(String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }
}
