package com.example.shippingmethod.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "shipping_methods")
public class ShippingMethod {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotBlank(message = "name is required")
	private String name;
	
	@Column(columnDefinition = "TEXT")
	private String description;
	
	@NotBlank(message = "Destination country is required")
    @Column(nullable = false)
    private String destinationCountry;
	
	@NotNull(message = "Cost is required")
    @Positive(message = "Cost must be positive")
    @Column(nullable = false)
    private Double cost;
	
	@Column(nullable = false)
    private Integer estimatedDays = 0; // Délai de livraison estimé en jours
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public ShippingMethod() {}
    
    public ShippingMethod(String name, String destination, Double cost) {
    	this.name = name;
    	this.destinationCountry = destination;
    	this.cost = cost;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDestinationCountry() {
		return destinationCountry;
	}

	public void setDestinationCountry(String destinationCountry) {
		this.destinationCountry = destinationCountry;
	}

	public Double getCost() {
		return cost;
	}

	public void setCost(Double cost) {
		this.cost = cost;
	}

	public Integer getEstimatedDays() {
		return estimatedDays;
	}

	public void setEstimatedDays(Integer estimatedDays) {
		this.estimatedDays = estimatedDays;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
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
	public boolean isAvailableForCountry(String country) {
        if (!enabled) {
            return false;
        }
        
        if ("ALL".equalsIgnoreCase(destinationCountry)) {
            return true;
        }
        
        if ("EU".equalsIgnoreCase(destinationCountry)) {
            return isEUCountry(country);
        }
        
        return destinationCountry.equalsIgnoreCase(country);
    }
    
    private boolean isEUCountry(String country) {
        String[] euCountries = {
            "France", "Allemagne", "Belgique", "Pays-Bas", "Luxembourg",
            "Italie", "Espagne", "Portugal", "Autriche", "Irlande"
        };
        
        for (String euCountry : euCountries) {
            if (euCountry.equalsIgnoreCase(country)) {
                return true;
            }
        }
        
        return false;
    }
}

