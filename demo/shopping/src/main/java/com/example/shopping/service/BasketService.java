package com.example.shopping.service;

import com.example.shopping.model.Basket;
import com.example.shopping.model.ProductLineItem;
import com.example.shopping.repository.BasketRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.Optional;

@Service
@Slf4j
@Transactional
public class BasketService {
    
    private final BasketRepository basketRepository;
    private final ProductLineItemService lineItemService;
    
    public BasketService(BasketRepository basketRepository, 
                        @Lazy ProductLineItemService lineItemService) {
        this.basketRepository = basketRepository;
        this.lineItemService = lineItemService;
    }
    
    public Optional<Basket> getBasketById(Long basketId) {
    	return basketRepository.findById(basketId);
    }
    
    public Basket getOrCreateBasket(Long userId, String sessionId) {
        if (userId != null) {
            return basketRepository.findByUserId(userId)
                    .orElseGet(() -> createBasket(userId, null));
        } else {
            return basketRepository.findBySessionId(sessionId)
                    .orElseGet(() -> createBasket(null, sessionId));
        }
    }
    public Basket getOrCreateBasketForUser(Long userId) {
    	
    	  return basketRepository.findByUserId(userId)
                  .orElseGet(() -> createBasket(userId, null));
    }
    
    private Basket createBasket(Long userId, String sessionId) {
        Basket basket = new Basket(userId, sessionId);
        return basketRepository.save(basket);
    }
    
    /**
     * Fusionne le panier de session avec le panier utilisateur lors de la connexion
     */
    public Basket mergeBaskets(Long userId, String sessionId) {
        System.out.println("🔄 Début fusion panier - UserID: " + userId + ", SessionID: " + sessionId);
        
        Optional<Basket> sessionBasketOpt = basketRepository.findBySessionId(sessionId);
        Optional<Basket> userBasketOpt = basketRepository.findByUserId(userId);
        
        System.out.println("📦 Panier session trouvé: " + sessionBasketOpt.isPresent());
        System.out.println("📦 Panier utilisateur trouvé: " + userBasketOpt.isPresent());
        
        // Si pas de panier de session, retourner ou créer le panier utilisateur
        if (sessionBasketOpt.isEmpty()) {
            System.out.println("⚠️ Pas de panier de session, rien à fusionner");
            return userBasketOpt.orElseGet(() -> createBasket(userId, null));
        }
        
        Basket sessionBasket = sessionBasketOpt.get();
        Basket userBasket = userBasketOpt.orElseGet(() -> createBasket(userId, null));
        
        System.out.println("🔍 Panier session ID: " + sessionBasket.getId());
        System.out.println("🔍 Panier utilisateur ID: " + userBasket.getId());
        
        // Fusionner les articles
        lineItemService.mergeBasketItems(sessionBasket.getId(), userBasket.getId());
        
        System.out.println("🗑️ Suppression du panier de session");
        // Supprimer le panier de session
        basketRepository.delete(sessionBasket);
        
        System.out.println("✅ Fusion terminée, panier utilisateur ID: " + userBasket.getId());
        return userBasket;
    }
    
    public Optional<Basket> getBasketByUserId(Long userId) {
        return basketRepository.findByUserId(userId);
    }
    
    public Optional<Basket> getBasketBySessionId(String sessionId) {
        return basketRepository.findBySessionId(sessionId);
    }
    
    public Basket save(Basket basket) {
        return basketRepository.save(basket);
    }
    
    public void deleteBasket(Long basketId) {
        basketRepository.deleteById(basketId);
    }
    public Basket setCheckoutAddresses(Long basketId, Long billingAddressId, Long shippingAddressId) {
    	Basket basket = basketRepository.findById(basketId).orElseThrow(() -> new RuntimeException("Basket not found: " + basketId));
    	
    	basket.setBillingAddressId(billingAddressId);
    	basket.setShippingAddressId(shippingAddressId);
    	basket.setStatus("CHECKOUT");
    	
    	return basketRepository.save(basket);
    }
    public Basket updateBasketStatus(Long basketId, String status) {
    	Basket basket = basketRepository.findById(basketId).orElseThrow(() -> new RuntimeException("Basket not found: " + basketId));
    	basket.setStatus(status);
    	
    	return basketRepository.save(basket);
    }
    public Basket setShippingMethod(Long basketId, Long shippingMethodId)
    {
    	Basket basket = basketRepository.findById(basketId).orElseThrow(() -> new RuntimeException("Basket not found"));
    	basket.setShippingMethodId(shippingMethodId);
    	return basketRepository.save(basket);
    }
    public Basket getOrCreateBasketForGuest(String guestId) {
        return basketRepository.findByGuestId(guestId)
                .orElseGet(() -> {
                    Basket basket = new Basket();
                    basket.setGuestId(guestId);
                    return basketRepository.save(basket);
                });
    }
    public Basket mergeGuestBasketToUser(Long userId, String guestId) {
        Basket userBasket = getOrCreateBasketForUser(userId);
        Optional<Basket> guestBasketOpt = basketRepository.findByGuestId(guestId);

        if (guestBasketOpt.isPresent()) {
            Basket guestBasket = guestBasketOpt.get();
            // Déplacer les items du panier invité vers le panier utilisateur
            List<ProductLineItem> guestItems = lineItemService.getLineItemsByBasketId(guestBasket.getId());
            for (ProductLineItem item : guestItems) {
                lineItemService.addOrUpdateLineItem(
                    userBasket.getId(),
                    item.getProductId(),
                    item.getQuantity(),
                    item.getUnitPrice()
                );
            }
            // Supprimer le panier invité
            lineItemService.clearBasket(guestBasket.getId());
            basketRepository.delete(guestBasket);
        }
        return userBasket;
    }
    /**
     * Définir l'entrepôt
     */
    public Basket setWarehouse(Long basketId, Long warehouseId) {
        Basket basket = basketRepository.findById(basketId)
            .orElseThrow(() -> new RuntimeException("Panier non trouvé"));
        
        basket.setWarehouseId(warehouseId);
        basket.setUpdatedAt(LocalDateTime.now());
        
        log.info("Entrepôt défini pour le panier: {} | Warehouse: {}", basketId, warehouseId);
        
        return basketRepository.save(basket);
    }
    /**
     * Définir le service de transport (nouveau système)
     */
    public Basket setCarrierService(Long basketId, Long carrierServiceId) {
        Basket basket = basketRepository.findById(basketId)
            .orElseThrow(() -> new RuntimeException("Panier non trouvé"));
        
        basket.setCarrierServiceId(carrierServiceId);
        basket.setUpdatedAt(LocalDateTime.now());
        
        log.info("Service de transport défini pour le panier: {} | CarrierService: {}", 
            basketId, carrierServiceId);
        
        return basketRepository.save(basket);
    }
    public Basket setDeliveryEstimates(Long basketId, LocalDate estimate, LocalDate latestEstimate) {
    	Basket basket = basketRepository.findById(basketId)
    			.orElseThrow(() -> new RuntimeException("Panier non trouvé"));
    	basket.setEstimatedDeliveryDate(estimate);
    	basket.setLatestDeliveryDate(latestEstimate);
    	basket.setUpdatedAt(LocalDateTime.now());
    	
    	return basketRepository.save(basket);
    }
    /**
     * Marquer le panier comme complété
     */
    public Basket completeBasket(Long basketId) {
        Basket basket = basketRepository.findById(basketId)
            .orElseThrow(() -> new RuntimeException("Panier non trouvé"));
        
        basket.setStatus("COMPLETED");
        basket.setUpdatedAt(LocalDateTime.now());
        
        log.info("Panier complété: {}", basketId);
        
        return basketRepository.save(basket);

    }
}