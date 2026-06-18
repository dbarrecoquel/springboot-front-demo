package com.example.shippingmethod.mapper;

import com.example.shippingmethod.dto.CarrierDto;
import com.example.shippingmethod.model.Carrier;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CarrierMapper {
    
    CarrierMapper INSTANCE = Mappers.getMapper(CarrierMapper.class);
    
    CarrierDto toDto(Carrier carrier);
    
    Carrier toEntity(CarrierDto dto);
    
    List<CarrierDto> toDtoList(List<Carrier> carriers);
    
    List<Carrier> toEntityList(List<CarrierDto> dtos);
}