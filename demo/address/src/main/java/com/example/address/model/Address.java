package com.example.address.model;

import com.example.user.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "addresses")
public class Address {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @NotBlank(message = "Address label is required")
    private String label; // "Home", "Work", "Billing", "Shipping", etc.
    
    @NotBlank(message = "Street is required")
    private String street;
    
    private String complement;
    
    @NotBlank(message = "City is required")
    private String city;
    
    @NotBlank(message = "Postal code is required")
    private String postalCode;
    
    @NotBlank(message = "Country is required")
    private String country;
    
    @Column(nullable = false)
    private String addressType; // "BILLING" or "SHIPPING"
    
    @Column(nullable = false)
    private Boolean isDefault = false;
    
    // Constructors
    public Address() {}
    
    public Address(Long userId, String label, String street, String city, String postalCode, String country, String addressType) {
        this.userId = userId;
        this.label = label;
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
        this.country = country;
        this.addressType = addressType;
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
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public String getStreet() {
        return street;
    }
    
    public void setStreet(String street) {
        this.street = street;
    }
    
    public String getComplement() {
        return complement;
    }
    
    public void setComplement(String complement) {
        this.complement = complement;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getPostalCode() {
        return postalCode;
    }
    
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getAddressType() {
        return addressType;
    }
    
    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }
    
    public Boolean getIsDefault() {
        return isDefault;
    }
    
    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(street);
        if (complement != null && !complement.isEmpty()) {
            sb.append(", ").append(complement);
        }
        sb.append(", ").append(postalCode).append(" ").append(city);
        sb.append(", ").append(country);
        return sb.toString();
    }
}