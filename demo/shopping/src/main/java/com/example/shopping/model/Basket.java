package com.example.shopping.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "baskets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Basket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Utilisateur propriétaire du panier
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * Session ID pour les paniers non authentifiés
     */
    @Column(name = "session_id")
    private String sessionId;
    
    @Column(name = "guest_id")
    private String guestId;
    // ======== CHECKOUT : ADRESSES ========
    
    /**
     * Adresse de facturation
     */
    @Column(name = "billing_address_id")
    private Long billingAddressId;
    
    /**
     * Adresse de livraison
     */
    @Column(name = "shipping_address_id")
    private Long shippingAddressId;
    
    // ======== CHECKOUT : ENTREPÔT ========
    
    /**
     * Entrepôt sélectionné pour fulfiller la commande
     */
    @Column(name = "warehouse_id")
    private Long warehouseId;
    
    @Column(name = "shipping_method_id")
    private Long shippingMethodId;
    // ======== CHECKOUT : LIVRAISON ========
    
    /**
     * Service de transporteur sélectionné
     */
    @Column(name = "carrier_service_id")
    private Long carrierServiceId;
    
    /**
     * Date estimée de livraison (début)
     */
    @Column(name = "estimated_delivery_date")
    private LocalDate estimatedDeliveryDate;
    
    /**
     * Date la plus tard de livraison (fin)
     */
    @Column(name = "latest_delivery_date")
    private LocalDate latestDeliveryDate;
    
    // ======== CHECKOUT : PAIEMENT ========
    
    /**
     * Méthode de paiement sélectionnée
     */
    @Column(name = "payment_method_id")
    private Long paymentMethodId;
    
    /**
     * Statut du panier
     */
    @Column(nullable = false)
    private String status ="ACTIVE";
    
    /**
     * Métadonnées
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    public Basket(Long userId, String sessionId) {
    	this.userId = userId;
    	this.sessionId = sessionId;
    }
}