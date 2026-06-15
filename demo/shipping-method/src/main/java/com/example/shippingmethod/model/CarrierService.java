package com.example.shippingmethod.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "carrier_services")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarrierService {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "carrier_id", nullable = false)
    private Carrier carrier;
    
    @Column(nullable = false)
    private String name; // Ex: Colissimo Domicile, DPD Next Day
    
    @Column(nullable = false)
    private String code;
    
    /**
     * Délai de livraison en jours ouvrables
     * Exemple: 2 pour livraison en 2 jours ouvrables
     */
    @Column(nullable = false)
    private Integer deliveryDays;
    
    /**
     * Heure limite de commande (format HH:mm)
     * Les commandes après cette heure sont traitées le jour suivant
     */
    @Column(nullable = false)
    private LocalTime cutoffTime = LocalTime.of(14, 0); // 14:00
    
    /**
     * Délai de traitement en jours (avant enlèvement par le transporteur)
     */
    @Column(nullable = false)
    private Integer processingDays = 1;
    
    /**
     * Coût de livraison
     */
    @Column(nullable = false)
    private Double cost = 0.0;
    
    /**
     * Coût minimal pour livraison gratuite
     */
    @Column(nullable = false)
    private Double freeShippingMinAmount = 50.0;
    
    /**
     * Description de la livraison
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}