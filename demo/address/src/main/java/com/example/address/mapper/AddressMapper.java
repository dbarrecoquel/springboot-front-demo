package com.example.address.mapper;

import com.example.address.dto.AddressDto;
import com.example.address.model.Address;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AddressMapper {

    public AddressDto toDto(Address address) {
        if (address == null)
            return null;

        AddressDto addressDto = new AddressDto();
        addressDto.setId(address.getId());  // Ajouter l'ID
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

    // Convertir DTO → Entity
    public Address toEntity(AddressDto addressDto) {
        if (addressDto == null)
            return null;

        Address address = new Address();
        address.setId(addressDto.getId());
        address.setLabel(addressDto.getLabel());
        address.setStreet(addressDto.getStreet());
        address.setPostalCode(addressDto.getPostalCode());
        address.setAddressType(addressDto.getAddressType());
        address.setCity(addressDto.getCity());
        address.setComplement(addressDto.getComplement());
        address.setIsDefault(addressDto.getIsDefault());
        address.setCountry(addressDto.getCountry());

        return address;
    }

    // Convertir une liste d'entités en DTOs
    public List<AddressDto> toDtoList(List<Address> addresses) {
        if (addresses == null)
            return null;

        return addresses.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Convertir une liste de DTOs en entités
    public List<Address> toEntityList(List<AddressDto> addressDtos) {
        if (addressDtos == null)
            return null;

        return addressDtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}