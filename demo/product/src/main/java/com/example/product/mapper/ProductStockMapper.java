package com.example.product.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.example.product.dto.ProductStockDto;
import com.example.product.model.ProductStock;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ProductStockMapper {

    /**
     * Entity -> DTO
     */
    @Mapping(target = "warehouseName", ignore = true)
    @Mapping(target = "warehouseCode", ignore = true)
    @Mapping(target = "inStock", expression = "java(stock.getQuantity() != null && stock.getQuantity() > 0)")
    ProductStockDto toDto(ProductStock stock);

    /**
     * DTO -> Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProductStock toEntity(ProductStockDto dto);

    /**
     * Listes
     */
    List<ProductStockDto> toDtoList(List<ProductStock> stocks);

    List<ProductStock> toEntityList(List<ProductStockDto> dtos);
}