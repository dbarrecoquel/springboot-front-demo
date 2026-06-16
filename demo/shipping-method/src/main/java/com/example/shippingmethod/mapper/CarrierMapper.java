package com.example.shippingmethod.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.example.shippingmethod.dto.CarrierDto;
import com.example.shippingmethod.model.Carrier;
@Mapper(
	    componentModel = "spring",
	    unmappedTargetPolicy = ReportingPolicy.IGNORE
	)
public interface CarrierMapper {

    CarrierDto toDto(Carrier carrier);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Carrier toEntity(CarrierDto carrierDto);
}
