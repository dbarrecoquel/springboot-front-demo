package com.example.shippingmethod.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HolidayDatesListDto {
	 
	    private Long id;
	    
	    private String warehouseName;
	   
	    private LocalDate holidayDate;
	    
	    private String name; // Ex: "Noël", "Jour de l'an", "14 juillet"
	    
	    private String country; // Code pays (FR, DE, IT, etc.)
	    
	    private String description;
	    
	    private Boolean isRecurring; // Si c'est un jour férié récurrent chaque année
	    
	    private LocalDateTime createdAt;
	 
	    private LocalDateTime updatedAt;
}
