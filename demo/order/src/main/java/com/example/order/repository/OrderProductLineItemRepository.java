package com.example.order.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.order.model.OrderProductLineItem;

public interface OrderProductLineItemRepository extends JpaRepository<OrderProductLineItem, Long> {
    List<OrderProductLineItem> findByOrderId(Long orderId);
    Optional<OrderProductLineItem> findByOrderIdAndProductId(Long orderId, Long productId);
    

}
