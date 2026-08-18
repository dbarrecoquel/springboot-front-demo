package com.example.order.mapper;

import com.example.order.dto.OrderDto;
import com.example.order.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    
    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);
    
    OrderDto toDto(Order order);
    
    Order toEntity(OrderDto dto);
    
    List<OrderDto> toDtoList(List<Order> orders);
    
    List<Order> toEntityList(List<OrderDto> dtos);
}