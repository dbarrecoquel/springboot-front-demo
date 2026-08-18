package com.example.order.mapper;

import com.example.order.dto.OrderProductLineItemDto;
import com.example.order.model.OrderProductLineItem;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderProductLineItemMapper {
    
    OrderProductLineItemMapper INSTANCE = Mappers.getMapper(OrderProductLineItemMapper.class);
    
    OrderProductLineItemDto toDto(OrderProductLineItem item);
    
    OrderProductLineItem toEntity(OrderProductLineItemDto dto);
    
    List<OrderProductLineItemDto> toDtoList(List<OrderProductLineItem> items);
    
    List<OrderProductLineItem> toEntityList(List<OrderProductLineItemDto> dtos);
}