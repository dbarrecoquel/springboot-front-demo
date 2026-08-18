package com.example.shopping.repository;

import com.example.shopping.model.Basket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BasketRepository extends JpaRepository<Basket, Long> {
    Optional<Basket> findByUserId(Long userId);
    Optional<Basket> findByUserIdAndStatus(Long userId, String status);
    Optional<Basket> findBySessionId(String sessionId);
    Optional<Basket> findByGuestId(String guestId);
}