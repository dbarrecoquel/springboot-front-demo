package com.example.product.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.example.product.enums.StockStatus;

@Entity
@Table(name = "product_stocks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"product_id", "warehouse_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductStock {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Référence au produit
     */
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    /**
     * Référence à l'entrepôt
     */
    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;
    
    /**
     * Quantité en stock
     */
    @Column(nullable = false)
    private Integer quantity = 0;
    
    /**
     * Seuil minimal de stock
     */
    @Column(nullable = false)
    private Integer minimumStock = 10;
    
    /**
     * Statut de disponibilité
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockStatus status = StockStatus.AVAILABLE; // AVAILABLE, LOW_STOCK, OUT_OF_STOCK
    
    /**
     * Métadonnées
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}