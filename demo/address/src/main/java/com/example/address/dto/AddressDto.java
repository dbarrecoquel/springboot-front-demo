package com.example.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AddressDto {
    
    private Long id;
    
    @NotBlank(message = "Le libellé est obligatoire")
    @Size(max = 100, message = "Le libellé ne peut pas dépasser 100 caractères")
    private String label;
    
    @NotBlank(message = "La rue est obligatoire")
    @Size(max = 255, message = "La rue ne peut pas dépasser 255 caractères")
    private String street;
    
    @Size(max = 255, message = "Le complément ne peut pas dépasser 255 caractères")
    private String complement;
    
    @NotBlank(message = "Le code postal est obligatoire")
    @Size(max = 20, message = "Le code postal ne peut pas dépasser 20 caractères")
    private String postalCode;
    
    @NotBlank(message = "La ville est obligatoire")
    @Size(max = 100, message = "La ville ne peut pas dépasser 100 caractères")
    private String city;
    
    @NotBlank(message = "Le pays est obligatoire")
    @Size(max = 100, message = "Le pays ne peut pas dépasser 100 caractères")
    private String country;
    
    @NotNull(message = "Le type d'adresse est obligatoire")
    private String addressType; // "SHIPPING" ou "BILLING"
    
    private Boolean isDefault;
    
    // Constructeurs
    public AddressDto() {
        this.isDefault = false;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public String getPostalCode() {
        return postalCode;
    }
    
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
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
}