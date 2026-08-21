package com.example.shopping.mapper;

import com.example.shopping.dto.BasketDto;
import com.example.shopping.model.Basket;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BasketMapper {
    
    BasketDto toDto(Basket basket);
    
    Basket toEntity(BasketDto dto);
    
    List<BasketDto> toDtoList(List<Basket> baskets);
    
    List<Basket> toEntityList(List<BasketDto> dtos);
}