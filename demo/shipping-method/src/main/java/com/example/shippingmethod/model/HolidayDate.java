package com.example.shippingmethod.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "holiday_dates", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"warehouse_id", "holiday_date"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HolidayDate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;
    
    @Column(nullable = false)
    private LocalDate holidayDate;
    
    @Column(nullable = false)
    private String name; // Ex: "Noël", "Jour de l'an", "14 juillet"
    
    @Column(nullable = false)
    private String country; // Code pays (FR, DE, IT, etc.)
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private Boolean isRecurring = false; // Si c'est un jour férié récurrent chaque année
    
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