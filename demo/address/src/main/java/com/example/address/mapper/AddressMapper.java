package com.example.address.mapper;

import org.springframework.stereotype.Component;

import com.example.address.dto.AddressDto;
import com.example.address.model.Address;

@Component
public class AddressMapper {

	public AddressDto toDto(Address address)
	{
		if (address == null)
			return null;
		
		AddressDto addressDto = new AddressDto();
		addressDto.setLabel(address.getLabel());
		addressDto.setStreet(address.getStreet());
		addressDto.setPostalCode(address.getPostalCode());
		addressDto.setAddressType(address.getAddressType());
		addressDto.setCity(address.getCity());
		addressDto.setComplement(address.getComplement());
		addressDto.setIsDefault(address.getIsDefault());
		addressDto.setCountry(address.getCountry());
		
		return addressDto;
	}
	
}
