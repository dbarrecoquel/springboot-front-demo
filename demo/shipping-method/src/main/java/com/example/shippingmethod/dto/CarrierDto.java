package com.example.shippingmethod.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarrierDto {
	  
	    private Long id;
	    
	    private String name; // Ex: La Poste, Colissimo, DPD, UPS
	    
	    private String code;
	    
	    private String description;
	    
	    private Boolean enabled = true;

	    private LocalDateTime createdAt;
	    
	    private LocalDateTime updatedAt;
}
