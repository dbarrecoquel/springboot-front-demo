package com.example.shippingmethod.mapper;

import org.springframework.stereotype.Component;

import com.example.shippingmethod.dto.ShippingMethodDto;
import com.example.shippingmethod.model.ShippingMethod;
@Component
public class ShippingMethodMapper {
	
	public ShippingMethodDto toDto(ShippingMethod shippingMethod)
	{
		ShippingMethodDto dto = new ShippingMethodDto();
		dto.setCost(shippingMethod.getCost());
		dto.setCreatedAt(shippingMethod.getCreatedAt());
		dto.setDescription(shippingMethod.getDescription());
		dto.setDestinationCountry(shippingMethod.getDestinationCountry());
		dto.setEnabled(shippingMethod.getEnabled());
		dto.setEstimatedDays(shippingMethod.getEstimatedDays());
		dto.setId(shippingMethod.getId());
		dto.setName(shippingMethod.getName());
		dto.setUpdatedAt(shippingMethod.getUpdatedAt());
		
		return dto;
	}
}
