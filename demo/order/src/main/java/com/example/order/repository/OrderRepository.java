package com.example.order.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.order.model.Order;


public interface OrderRepository extends JpaRepository<Order, Long> {
	Optional<Order> findByUserId(Long userId);
	List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
}
