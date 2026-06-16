package com.example.shippingmethod.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarrierDto {
	  
	    private Long id;
	    
	    private String name; // Ex: La Poste, Colissimo, DPD, UPS
	    
	    private String code;
	    
	    private String description;
	    
	    private Boolean enabled = true;
}
