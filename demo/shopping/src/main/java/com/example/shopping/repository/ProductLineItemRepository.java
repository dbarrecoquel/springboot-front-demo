package com.example.shopping.repository;

import com.example.shopping.model.ProductLineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductLineItemRepository extends JpaRepository<ProductLineItem, Long> {
    List<ProductLineItem> findByBasketId(Long basketId);
    Optional<ProductLineItem> findByBasketIdAndProductId(Long basketId, Long productId);
    void deleteByBasketId(Long basketId);
}