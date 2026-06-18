package com.example.shippingmethod.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import com.example.shippingmethod.dto.HolidayDatesDto;
import com.example.shippingmethod.dto.HolidayDatesListDto;
import com.example.shippingmethod.model.HolidayDate;
import com.example.shippingmethod.model.Warehouse;
@Mapper(
	    componentModel = "spring",
	    unmappedTargetPolicy = ReportingPolicy.IGNORE
	)
public interface HolidayDatesMapper {

    @Mapping(source = "warehouse.id", target = "warehouseId")
    @Mapping(source = "warehouse.name", target = "warehouseName")
    @Mapping(source = "warehouse.code", target = "warehouseCode")
    HolidayDatesDto toDto(HolidayDate date);

    @Mapping(source = "warehouse.name", target = "warehouseName")
    HolidayDatesListDto toListDto(HolidayDate date);

    @Mapping(target = "warehouse", expression = "java(createWarehouseFromDto(dto))")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    HolidayDate toEntity(HolidayDatesDto dto);

    List<HolidayDatesDto> toDtoList(List<HolidayDate> dates);

    List<HolidayDatesListDto> toListDtoList(List<HolidayDate> dates);

    default Warehouse createWarehouseFromDto(HolidayDatesDto dto) {
        if (dto.getWarehouseId() == null) {
            return null;
        }

        Warehouse warehouse = new Warehouse();
        warehouse.setId(dto.getWarehouseId());
        return warehouse;
    }
}