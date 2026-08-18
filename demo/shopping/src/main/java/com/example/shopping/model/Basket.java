package com.example.shopping.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "baskets")
public class Basket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "session_id")
    private String sessionId; // Pour les utilisateurs non connectés
    
    @Column(name = "billing_address_id")
    private Long billingAddressId;
    
    @Column(name = "shipping_address_id")
    private Long shippingAddressId;
    
    @Column(name = "status")
    private String status = "ACTIVE";
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "shipping_method_id")
    private Long shippingMethodId;
    @Column(name = "guest_id")
    private String guestId;
    @Column(name = "warehouse_id")
    private Long warehouseId;
    // Nouveau système - Service de Transport
    @Column(name = "carrier_service_id")
    private Long carrierServiceId;
    
    // Dates estimées de livraison
    @Column(name = "estimated_delivery_date")
    private LocalDate estimatedDeliveryDate;
    @Column(name = "latest_delivery_date")
    private LocalDate latestDeliveryDate;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public Basket() {}
    
    public Basket(Long userId, String sessionId) {
        this.userId = userId;
        this.sessionId = sessionId;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getShippingMethodId() {
		return shippingMethodId;
	}

	public void setShippingMethodId(Long shippingMethodId) {
		this.shippingMethodId = shippingMethodId;
	}

	public String getGuestId() {
		return guestId;
	}

	public void setGuestId(String guestId) {
		this.guestId = guestId;
	}

	public Long getWarehouseId() {
		return warehouseId;
	}

	public void setWarehouseId(Long warehouseId) {
		this.warehouseId = warehouseId;
	}

	public Long getCarrierServiceId() {
		return carrierServiceId;
	}

	public void setCarrierServiceId(Long carrierServiceId) {
		this.carrierServiceId = carrierServiceId;
	}

	public LocalDate getEstimatedDeliveryDate() {
		return estimatedDeliveryDate;
	}

	public void setEstimatedDeliveryDate(LocalDate estimatedDeliveryDate) {
		this.estimatedDeliveryDate = estimatedDeliveryDate;
	}

	public LocalDate getLatestDeliveryDate() {
		return latestDeliveryDate;
	}

	public void setLatestDeliveryDate(LocalDate latestDeliveryDate) {
		this.latestDeliveryDate = latestDeliveryDate;
	}
}