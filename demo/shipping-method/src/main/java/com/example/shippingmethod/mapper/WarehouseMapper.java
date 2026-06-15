package com.example.shippingmethod.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.example.shippingmethod.dto.WarehouseDto;
import com.example.shippingmethod.model.Warehouse;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface WarehouseMapper {

    WarehouseDto toDto(Warehouse warehouse);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Warehouse toEntity(WarehouseDto warehouseDto);
}