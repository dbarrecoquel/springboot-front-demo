package com.example.shippingmethod.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WarehouseDto {
	
	private Long id;
	private String name;
	private String code;
	private String street;
    private String postalCode;  
    private String city;	    
    private String country;	    
    private String region; // Region for delivery estimation
    private Boolean enabled;
}
