package com.example.order.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class OrderProductLineItemService {

    private final OrderProductLineItemRepository orderProductLineItemRepository;
    private final OrderProductLineItemMapper orderProductLineItemMapper;

    public OrderProductLineItemService(OrderProductLineItemRepository orderProductLineItemRepository,
                                        OrderProductLineItemMapper orderProductLineItemMapper) {
        this.orderProductLineItemRepository = orderProductLineItemRepository;
        this.orderProductLineItemMapper = orderProductLineItemMapper;
    }

    /**
     * Créer un article de commande à partir d'un article du panier
     */
    public OrderProductLineItem createItemFromBasketItem(Order order, ProductLineItem basketItem) {
        try {
            OrderProductLineItem item = new OrderProductLineItem();
            item.setOrderId(order.getId());
           
            item.setProductId(basketItem.getProductId());
            item.setProductName(basketItem.getProduct().getName());
            item.setProductSku(basketItem.getProduct().getSku());
            item.setQuantity(basketItem.getQuantity());
            item.setUnitPrice(basketItem.getUnitPrice());
            item.setSubtotal(basketItem.getQuantity() * basketItem.getUnitPrice());
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());

            OrderProductLineItem saved = orderProductLineItemRepository.save(item);

            log.info("Article créé pour la commande {} | Produit: {} | Quantité: {} | Sous-total: {}", 
                order.getId(), basketItem.getProduct().getName(), basketItem.getQuantity(), item.getSubtotal());

            return saved;
        } catch (Exception e) {
            log.error("Erreur création article commande: {}", e.getMessage());
            throw new RuntimeException("Erreur création article: " + e.getMessage());
        }
    }

    /**
     * Créer plusieurs articles de commande à partir des articles du panier
     */
    public List<OrderProductLineItem> createItemsFromBasketItems(
            Order order,
            List<ProductLineItem> basketItems) {
        try {
            List<OrderProductLineItem> createdItems = basketItems.stream()
                .map(item -> createItemFromBasketItem(order, item))
                .collect(Collectors.toList());

            log.info("Articles créés pour la commande {} | Nombre: {}", order.getId(), createdItems.size());

            return createdItems;
        } catch (Exception e) {
            log.error("Erreur création articles commande: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Récupérer les articles d'une commande (entités)
     */
    @Transactional(readOnly = true)
    public List<OrderProductLineItem> getOrderItemsByOrderId(Long orderId) {
        try {
            List<OrderProductLineItem> items = orderProductLineItemRepository.findByOrderId(orderId);
            log.info("Articles récupérés pour la commande {}: {} articles", orderId, items.size());
            return items;
        } catch (Exception e) {
            log.error("Erreur récupération articles: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Récupérer les articles d'une commande (DTO)
     */
    @Transactional(readOnly = true)
    public List<OrderProductLineItemDto> getOrderItems(Long orderId) {
        try {
            List<OrderProductLineItem> items = orderProductLineItemRepository.findByOrderId(orderId);
            List<OrderProductLineItemDto> dtos = orderProductLineItemMapper.toDtoList(items);
            log.info("DTOs articles récupérés pour la commande {}: {} DTOs", orderId, dtos.size());
            return dtos;
        } catch (Exception e) {
            log.error("Erreur récupération DTOs articles: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Récupérer un article spécifique (DTO)
     */
    @Transactional(readOnly = true)
    public Optional<OrderProductLineItemDto> getOrderItem(Long orderId, Long productId) {
        try {
            Optional<OrderProductLineItemDto> dto = orderProductLineItemRepository
                .findByOrderIdAndProductId(orderId, productId)
                .map(orderProductLineItemMapper::toDto);
            
            if (dto.isPresent()) {
                log.info("Article récupéré | Commande: {} | Produit: {}", orderId, productId);
            } else {
                log.warn("Article non trouvé | Commande: {} | Produit: {}", orderId, productId);
            }
            return dto;
        } catch (Exception e) {
            log.error("Erreur récupération article: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Calculer le sous-total d'une commande
     */
    @Transactional(readOnly = true)
    public Double calculateOrderSubtotal(Long orderId) {
        try {
            Double subtotal = orderProductLineItemRepository.findByOrderId(orderId).stream()
                .mapToDouble(OrderProductLineItem::getSubtotal)
                .sum();
            
            log.info("Sous-total calculé pour la commande {}: {}", orderId, subtotal);
            return subtotal;
        } catch (Exception e) {
            log.error("Erreur calcul sous-total: {}", e.getMessage());
            return 0.0;
        }
    }

    /**
     * Calculer le nombre d'articles d'une commande
     */
    @Transactional(readOnly = true)
    public Integer countOrderItems(Long orderId) {
        try {
            Integer count = orderProductLineItemRepository.findByOrderId(orderId).stream()
                .mapToInt(OrderProductLineItem::getQuantity)
                .sum();
            
            log.info("Nombre d'articles calculé pour la commande {}: {}", orderId, count);
            return count;
        } catch (Exception e) {
            log.error("Erreur comptage articles: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Supprimer un article de la commande
     */
    public void removeOrderItem(Long itemId) {
        try {
            orderProductLineItemRepository.deleteById(itemId);
            log.info("Article supprimé: {}", itemId);
        } catch (Exception e) {
            log.error("Erreur suppression article: {}", e.getMessage());
            throw new RuntimeException("Erreur suppression article: " + e.getMessage());
        }
    }

    /**
     * Supprimer tous les articles d'une commande
     */
    public void removeOrderItems(Long orderId) {
        try {
            List<OrderProductLineItem> items = orderProductLineItemRepository.findByOrderId(orderId);
            orderProductLineItemRepository.deleteAll(items);
            log.info("Tous les articles de la commande {} supprimés | Nombre: {}", orderId, items.size());
        } catch (Exception e) {
            log.error("Erreur suppression articles commande: {}", e.getMessage());
            throw new RuntimeException("Erreur suppression articles: " + e.getMessage());
        }
    }

    /**
     * Mettre à jour la quantité d'un article
     */
    public OrderProductLineItem updateItemQuantity(Long itemId, Integer newQuantity) {
        try {
            OrderProductLineItem item = orderProductLineItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Article non trouvé: " + itemId));

            Integer oldQuantity = item.getQuantity();
            item.setQuantity(newQuantity);
            item.setSubtotal(newQuantity * item.getUnitPrice());
            item.setUpdatedAt(LocalDateTime.now());

            OrderProductLineItem saved = orderProductLineItemRepository.save(item);

            log.info("Quantité mise à jour | Article: {} | Ancien: {} | Nouveau: {}", 
                itemId, oldQuantity, newQuantity);

            return saved;
        } catch (Exception e) {
            log.error("Erreur mise à jour quantité: {}", e.getMessage());
            throw new RuntimeException("Erreur mise à jour quantité: " + e.getMessage());
        }
    }

    /**
     * Vérifier si une commande a des articles
     */
    @Transactional(readOnly = true)
    public boolean hasOrderItems(Long orderId) {
        try {
            long count = orderProductLineItemRepository.findByOrderId(orderId).size();
            return count > 0;
        } catch (Exception e) {
            log.error("Erreur vérification articles: {}", e.getMessage());
            return false;
        }
    }
}