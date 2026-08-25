package com.example.order.service;

import com.example.order.dto.OrderDto;
import com.example.order.mapper.OrderMapper;
import com.example.order.model.Order;
import com.example.order.model.OrderProductLineItem;
import com.example.order.repository.OrderRepository;
import com.example.product.service.ProductStockService;
import com.example.shippingmethod.service.CarrierServiceService;
import com.example.shopping.model.Basket;
import com.example.shopping.model.ProductLineItem;
import com.example.shopping.repository.BasketRepository;
import com.example.shopping.service.BasketService;
import com.example.shopping.service.ProductLineItemService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final BasketRepository basketRepository;
    private final CarrierServiceService carrierServiceService;
    public OrderService(
            OrderRepository orderRepository,
            OrderProductLineItemService lineItemService,
            OrderMapper mapper,
            BasketService basketService,
            ProductLineItemService productLineItemService,
            ProductStockService productStockService,
            BasketRepository basketRepository,
            CarrierServiceService carrierServiceService) {
        
        this.orderRepository = orderRepository;
        this.lineItemService = lineItemService;
        this.mapper = mapper;
        this.basketService = basketService;
        this.productLineItemService = productLineItemService;
        this.productStockService = productStockService;
        this.basketRepository = basketRepository;
        this.carrierServiceService = carrierServiceService;
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
    @Transactional
    public Order createOrderFromBasket(Long basketId) {
        Basket basket = basketRepository.findById(basketId)
            .orElseThrow(() -> new RuntimeException("Panier non trouvé"));
        
        List<ProductLineItem> basketItems = productLineItemService.getLineItemsByBasketId(basketId);
        
        if (basketItems.isEmpty()) {
            throw new RuntimeException("Le panier est vide");
        }
        
        // Calculer les totaux
        Double subtotal = basketItems.stream()
            .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
            .sum();
        
        Double tax = subtotal * 0.20; // 20%
        
        // ✅ RÉCUPÉRER LE SHIPPING COST DU CARRIER SERVICE
        Double shippingCost = 0.0;
        if (basket.getCarrierServiceId() != null) {
            try {
                // Supposant que CarrierServiceService a une méthode getCostById()
                shippingCost = carrierServiceService.getServiceById(basket.getCarrierServiceId())
                    .map(cs -> cs.getCost() != null ? cs.getCost() : 0.0)
                    .orElse(0.0);
            } catch (Exception e) {
                log.warn("Impossible de récupérer le coût de livraison: {}", e.getMessage());
            }
        }
        
        Double total = subtotal + tax + shippingCost;
        
        // ✅ Réserver le stock
        for (ProductLineItem item : basketItems) {
            productStockService.reserveStock(
                item.getProductId(),
                basket.getWarehouseId(),
                item.getQuantity()
            );
        }
        
        // Créer la commande
        String orderNumber = generateOrderNumber();
        
        Order order = Order.builder()
            .orderNumber(orderNumber)
            .userId(basket.getUserId())
            .basketId(basket.getId())
            .billingAddressId(basket.getBillingAddressId())
            .shippingAddressId(basket.getShippingAddressId())
            .warehouseId(basket.getWarehouseId())
            .carrierServiceId(basket.getCarrierServiceId())
            .estimatedDeliveryDate(basket.getEstimatedDeliveryDate())
            .latestDeliveryDate(basket.getLatestDeliveryDate())
            .subtotal(subtotal)
            .tax(tax)
            .shippingCost(shippingCost)  
            .total(total)
            .status("PENDING")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        Order savedOrder = orderRepository.save(order);
        List<OrderProductLineItem> createdItems = lineItemService
        	    .createItemsFromBasketItems(savedOrder, basketItems);

        log.info("Commande créée | Order ID: {} | Subtotal: {} | Tax: {} | Shipping: {} | Total: {}", 
            savedOrder.getId(), subtotal, tax, shippingCost, total);
        
        return savedOrder;
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
    @Transactional(readOnly = true)
    public Double calculateOrderTotal(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
        
        return order.getSubtotal() + order.getTax() + order.getShippingCost();
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
     * Recuperer les commandes d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
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
     * Récupérer toutes les commandes paginées
     */
    @Transactional(readOnly = true)
    public Page<Order> getAllOrdersPaginated(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    /**
     * Rechercher les commandes par numéro
     */
    @Transactional(readOnly = true)
    public Page<Order> searchOrdersByNumber(String orderNumber, Pageable pageable) {
        return orderRepository.findByOrderNumberContainingIgnoreCase(orderNumber, pageable);
    }

    /**
     * Récupérer les commandes par statut
     */
    @Transactional(readOnly = true)
    public Page<Order> getOrdersByStatus(String status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
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