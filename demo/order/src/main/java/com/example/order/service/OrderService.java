package com.example.order.service;

import com.example.order.dto.OrderDto;
import com.example.order.mapper.OrderMapper;
import com.example.order.model.Order;
import com.example.order.repository.OrderRepository;
import com.example.product.service.ProductStockService;
import com.example.shopping.model.Basket;
import com.example.shopping.model.ProductLineItem;
import com.example.shopping.service.BasketService;
import com.example.shopping.service.ProductLineItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderProductLineItemService lineItemService;
    private final OrderMapper mapper;
    private final BasketService basketService;
    private final ProductLineItemService productLineItemService;
    private final ProductStockService productStockService;
    
    public OrderService(
            OrderRepository orderRepository,
            OrderProductLineItemService lineItemService,
            OrderMapper mapper,
            BasketService basketService,
            ProductLineItemService productLineItemService,
            ProductStockService productStockService) {
        
        this.orderRepository = orderRepository;
        this.lineItemService = lineItemService;
        this.mapper = mapper;
        this.basketService = basketService;
        this.productLineItemService = productLineItemService;
        this.productStockService = productStockService;
    }
    
    /**
     * ⭐ Transformer un panier en commande
     * 
     * Étapes :
     * 1. Valider le panier et les stocks
     * 2. Créer la commande
     * 3. Créer les items de commande
     * 4. Réduire les stocks
     * 5. Marquer le panier comme complété
     */
    public OrderDto createOrderFromBasket(Long basketId) {
        
        log.info("🛒 Transformation du panier {} en commande", basketId);
        
        // Étape 1 : Récupérer le panier
        Basket basket = basketService.getBasketById(basketId)
            .orElseThrow(() -> new RuntimeException("Panier non trouvé"));
        
        // Étape 2 : Valider les données essentielles
        if (basket.getShippingAddressId() == null || basket.getBillingAddressId() == null) {
            throw new RuntimeException("Adresses non définies");
        }
        
       /* if (basket.getWarehouseId() == null) {
            throw new RuntimeException("Entrepôt non défini");
        }
        
        if (basket.getCarrierServiceId() == null) {
            throw new RuntimeException("Service de transport non défini");
        }
        */
        // Étape 3 : Récupérer les items du panier
        List<ProductLineItem> basketItems = productLineItemService.getLineItemsByBasketId(basketId);
        
        if (basketItems.isEmpty()) {
            throw new RuntimeException("Le panier est vide");
        }
        
        // Étape 4 : Vérifier les stocks
       /* for (ProductLineItem item : basketItems) {
            boolean inStock = productStockService.isProductInStock(
                item.getProductId(),
                basket.getWarehouseId(),
                item.getQuantity());
            
            if (!inStock) {
                throw new RuntimeException(
                    "Le produit " + item.getProductName() + 
                    " n'est pas disponible en quantité suffisante");
            }
        }
        */
        // Étape 5 : Créer la commande
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUserId(basket.getUserId());
        order.setBasketId(basketId);
        order.setBillingAddressId(basket.getBillingAddressId());
        order.setShippingAddressId(basket.getShippingAddressId());
        order.setShippingMethodId(basket.getShippingMethodId());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        
        Order savedOrder = orderRepository.save(order);
        
        log.info("✅ Commande créée: {} | Numéro: {}", savedOrder.getId(), savedOrder.getOrderNumber());
        
        // Étape 6 : Créer les items de commande et calculer les montants
        lineItemService.createItemsFromBasketItems(savedOrder, basketItems);
        
        Double subtotal = lineItemService.calculateOrderSubtotal(savedOrder.getId());
        savedOrder.setSubtotal(subtotal);
        
        // TODO: Récupérer le coût de livraison depuis le service de transport
        // savedOrder.setShippingCost(...)
        
        // TODO: Calculer la taxe selon les règles métier
        // savedOrder.setTax(...)
        
        Double total = subtotal + savedOrder.getShippingCost() + savedOrder.getTax();
        savedOrder.setTotal(total);
        
        savedOrder.setStatus("CONFIRMED");
        savedOrder = orderRepository.save(savedOrder);
        
        // Étape 7 : Réduire les stocks
        for (ProductLineItem item : basketItems) {
            productStockService.decreaseStock(
                item.getProductId(),
                basket.getWarehouseId(),
                item.getQuantity());
            
            log.info("📦 Stock réduit | Produit: {} | Qté: {} | Entrepôt: {}", 
                item.getProductId(), item.getQuantity(), basket.getWarehouseId());
        }
        
        // Étape 8 : Marquer le panier comme complété
        basketService.completeBasket(basketId);
        
        log.info("✅ Commande {} finalisée | Total: €{}", savedOrder.getId(), total);
        
        return getOrderDto(savedOrder.getId());
    }
    
    /**
     * Récupérer une commande par ID
     */
    @Transactional(readOnly = true)
    public Optional<OrderDto> getOrderById(Long id) {
        return orderRepository.findById(id).map(this::toDetailedDto);
    }
    
    /**
     * Récupérer les commandes d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<OrderDto> getUserOrders(Long userId) {
        // TODO: Ajouter une query method dans le repository
        return List.of();
    }
    
    /**
     * Mettre à jour le statut d'une commande
     */
    public OrderDto updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
        
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        
        Order updated = orderRepository.save(order);
        
        log.info("Statut de la commande {} mis à jour: {}", orderId, newStatus);
        
        return mapper.toDto(updated);
    }
    
    /**
     * Obtenir le DTO simplifié
     */
    @Transactional(readOnly = true)
    private OrderDto getOrderDto(Long orderId) {
        return orderRepository.findById(orderId)
            .map(mapper::toDto)
            .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
    }
    
    /**
     * Obtenir le DTO détaillé avec items
     */
    @Transactional(readOnly = true)
    private OrderDto toDetailedDto(Order order) {
        OrderDto dto = mapper.toDto(order);
        dto.setItems(lineItemService.getOrderItems(order.getId()));
        return dto;
    }
    
    /**
     * Générer un numéro de commande unique
     * Format: CMD-YYYYMMDD-XXXXXX (derniers 6 chiffres de l'ID)
     */
    private String generateOrderNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long timestamp = System.currentTimeMillis();
        String sequence = String.format("%06d", timestamp % 1000000);
        return "CMD-" + date + "-" + sequence;
    }
}