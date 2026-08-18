package com.example.order.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.order.dto.OrderProductLineItemDto;
import com.example.order.mapper.OrderProductLineItemMapper;
import com.example.order.model.Order;
import com.example.order.model.OrderProductLineItem;
import com.example.order.repository.OrderProductLineItemRepository;
import com.example.shopping.model.ProductLineItem;

@Service
@Transactional
public class OrderProductLineItemService {

	private final OrderProductLineItemRepository orderProductLineItemRepository;
	private final OrderProductLineItemMapper orderProductLineItemMapper;
	
	public OrderProductLineItemService(OrderProductLineItemRepository orderProductLineItemRepository,
										OrderProductLineItemMapper orderProductLineItemMapper) {
		this.orderProductLineItemRepository = orderProductLineItemRepository;
		this.orderProductLineItemMapper = orderProductLineItemMapper;
	}
	
	public OrderProductLineItem createItemFromBasketItem(Order order, ProductLineItem basketItem) {
		
		OrderProductLineItem item = new OrderProductLineItem();
		item.setOrderId(order.getId());
		item.setOrder(order);
		item.setProductId(basketItem.getProductId());
		item.setProductName(basketItem.getProduct().getName());
		item.setProductSku(basketItem.getProduct().getSku());
		item.setQuantity(basketItem.getQuantity());
		item.setUnitPrice(basketItem.getUnitPrice());
		item.setSubtotal(basketItem.getQuantity() * basketItem.getUnitPrice());
		item.setCreatedAt(LocalDateTime.now());
		
		OrderProductLineItem saved = orderProductLineItemRepository.save(item);
		
		return saved;
	}
	public List<OrderProductLineItem> createItemsFromBasketItems(
            Order order,
            List<ProductLineItem> basketItems) {
        
        return basketItems.stream()
            .map(item -> createItemFromBasketItem(order, item))
            .collect(Collectors.toList());
    }
	@Transactional(readOnly = true)
	public List<OrderProductLineItemDto> getOrderItems(Long orderId) {
	        return orderProductLineItemMapper.toDtoList(orderProductLineItemRepository.findByOrderId(orderId));
	}
	@Transactional(readOnly = true)
    public Optional<OrderProductLineItemDto> getOrderItem(Long orderId, Long productId) {
        return orderProductLineItemRepository.findByOrderIdAndProductId(orderId, productId)
            .map(orderProductLineItemMapper::toDto);
    }
	@Transactional(readOnly = true)
    public Double calculateOrderSubtotal(Long orderId) {
        return orderProductLineItemRepository.findByOrderId(orderId).stream()
            .mapToDouble(OrderProductLineItem::getSubtotal)
            .sum();
    }
	
	
}
