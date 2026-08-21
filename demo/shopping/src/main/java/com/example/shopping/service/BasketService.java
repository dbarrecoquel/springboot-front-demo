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
    
    // Constantes pour les statuts
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_ABANDONED = "ABANDONED";
    private static final String STATUS_CHECKOUT = "CHECKOUT";
    
    public BasketService(BasketRepository basketRepository, 
                        @Lazy ProductLineItemService lineItemService) {
        this.basketRepository = basketRepository;
        this.lineItemService = lineItemService;
    }
    
    public Optional<Basket> getBasketById(Long basketId) {
        return basketRepository.findById(basketId);
    }
    
    /**
     * Obtenir un panier actif ou créer un nouveau
     * - Si un panier ACTIVE existe → le retourner
     * - Si le panier est COMPLETED → créer un nouveau
     * - Si aucun panier n'existe → créer un nouveau
     */
    @Transactional
    public Basket getOrCreateBasket(Long userId, String sessionId) {
        
        if (userId != null) {
            // Chercher un panier ACTIF pour cet utilisateur
            Optional<Basket> activeBasket = basketRepository.findByUserIdAndStatus(userId, STATUS_ACTIVE);
            
            if (activeBasket.isPresent()) {
                log.info("Panier ACTIF trouvé pour utilisateur {}", userId);
                return activeBasket.get();
            }
            
            // Chercher un panier COMPLÉTÉ pour cet utilisateur
            Optional<Basket> completedBasket = basketRepository.findByUserIdAndStatus(userId, STATUS_COMPLETED);
            
            if (completedBasket.isPresent()) {
                log.info("Panier COMPLÉTÉ trouvé - Création d'un nouveau panier pour utilisateur {}", userId);
            }
            
            // Créer un nouveau panier
            return createBasket(userId, null);
            
        } else if (sessionId != null) {
            // Chercher un panier ACTIF pour cette session
            Optional<Basket> activeBasket = basketRepository.findBySessionIdAndStatus(sessionId, STATUS_ACTIVE);
            
            if (activeBasket.isPresent()) {
                log.info("Panier ACTIF trouvé pour session {}", sessionId);
                return activeBasket.get();
            }
            
            // Créer un nouveau panier
            return createBasket(null, sessionId);
            
        } else {
            throw new RuntimeException("userId et sessionId ne peuvent pas être tous les deux null");
        }
    }
    
    /**
     * Créer un nouveau panier
     */
    private Basket createBasket(Long userId, String sessionId) {
        Basket basket = new Basket();
        basket.setUserId(userId);
        basket.setSessionId(sessionId);
        basket.setStatus(STATUS_ACTIVE);
        basket.setCreatedAt(LocalDateTime.now());
        basket.setUpdatedAt(LocalDateTime.now());
        
        Basket saved = basketRepository.save(basket);
        
        log.info("Nouveau panier créé | ID: {} | Utilisateur: {} | Session: {}", 
            saved.getId(), userId, sessionId);
        
        return saved;
    }
    
    public Basket getOrCreateBasketForUser(Long userId) {
        return basketRepository.findByUserId(userId)
            .orElseGet(() -> createBasket(userId, null));
    }
    
    /**
     * Fusionne le panier de session avec le panier utilisateur lors de la connexion
     */
    /**
     * Fusionner le panier de session (utilisateur non authentifié) avec le panier utilisateur (après authentification)
     */
    @Transactional
    public Basket mergeBaskets(Long userId, String sessionId) {
        log.info("Fusion panier - UserID: {} | SessionID: {}", userId, sessionId);
        
        try {
            Optional<Basket> sessionBasketOpt = basketRepository.findBySessionId(sessionId);
            
            log.info("Panier de session trouvé avec sessionId: {}", sessionId);
            
            if (sessionBasketOpt.isEmpty()) {
                log.warn("Aucun panier de session trouvé pour sessionId: {}", sessionId);
            }
            
            Optional<Basket> userBasketOpt = basketRepository.findByUserIdAndStatus(userId, STATUS_ACTIVE);
            
            Basket userBasket;
            if (userBasketOpt.isPresent()) {
                userBasket = userBasketOpt.get();
                log.info("Panier utilisateur ACTIF trouvé: {}", userBasket.getId());
            } else {
                userBasket = createBasket(userId, null);
                log.info("Nouveau panier utilisateur créé: {}", userBasket.getId());
            }
            
            if (sessionBasketOpt.isPresent()) {
                Basket sessionBasket = sessionBasketOpt.get();
                
                log.info("Fusion des articles | Panier session: {} → Panier utilisateur: {}", 
                    sessionBasket.getId(), userBasket.getId());
                
                // Récupérer les articles du panier de session
                List<ProductLineItem> sessionItems = lineItemService.getLineItemsByBasketId(sessionBasket.getId());
                
                log.info("Articles à fusionner: {}", sessionItems.size());
                
                // Fusionner chaque article
                for (ProductLineItem sessionItem : sessionItems) {
                    try {
                        lineItemService.addOrUpdateLineItem(
                            userBasket.getId(),
                            sessionItem.getProductId(),
                            sessionItem.getQuantity(),
                            sessionItem.getUnitPrice()
                        );
                        log.info("Article fusionné: Produit {} (Quantité: {})", 
                            sessionItem.getProductId(), sessionItem.getQuantity());
                    } catch (Exception e) {
                        log.error("Erreur fusion article: {}", e.getMessage());
                    }
                }
                
                // Supprimer le panier de session
                basketRepository.delete(sessionBasket);
                log.info("Panier de session supprimé: {}", sessionBasket.getId());
                
            } else {
                log.info("Aucun panier de session à fusionner");
            }
            
            userBasket.setSessionId(sessionId);
            userBasket.setUpdatedAt(LocalDateTime.now());
            basketRepository.save(userBasket);
            
            log.info("Fusion terminée | Panier utilisateur final: {} | Articles: {}", 
                userBasket.getId(), lineItemService.getLineItemsByBasketId(userBasket.getId()).size());
            
            return userBasket;
            
        } catch (Exception e) {
            log.error("Erreur lors de la fusion des paniers: {}", e.getMessage());
            e.printStackTrace();
            
            // Fallback : créer un panier utilisateur s'il y a erreur
            return basketRepository.findByUserIdAndStatus(userId, STATUS_ACTIVE)
                .orElseGet(() -> createBasket(userId, null));
        }
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
    
    /**
     * Définir les adresses de checkout
     */
    public Basket setCheckoutAddresses(Long basketId, Long billingAddressId, Long shippingAddressId) {
        Basket basket = basketRepository.findById(basketId)
            .orElseThrow(() -> new RuntimeException("Panier non trouvé: " + basketId));
        
        basket.setBillingAddressId(billingAddressId);
        basket.setShippingAddressId(shippingAddressId);
        basket.setUpdatedAt(LocalDateTime.now());
        
        log.info("Adresses de checkout définies pour panier: {}", basketId);
        
        return basketRepository.save(basket);
    }
    
    /**
     * Mettre à jour le statut du panier
     */
    public Basket updateBasketStatus(Long basketId, String status) {
        Basket basket = basketRepository.findById(basketId)
            .orElseThrow(() -> new RuntimeException("Panier non trouvé"));
        
        basket.setStatus(status);
        basket.setUpdatedAt(LocalDateTime.now());
        
        return basketRepository.save(basket);
    }
    
    /**
     * Définir l'entrepôt
     */
    public Basket setWarehouse(Long basketId, Long warehouseId) {
        Basket basket = basketRepository.findById(basketId)
            .orElseThrow(() -> new RuntimeException("Panier non trouvé"));
        
        basket.setWarehouseId(warehouseId);
        basket.setUpdatedAt(LocalDateTime.now());
        
        log.info("Entrepôt défini pour panier: {} | Warehouse: {}", basketId, warehouseId);
        
        return basketRepository.save(basket);
    }
    
    /**
     * Définir le service de transport
     */
    public Basket setCarrierService(Long basketId, Long carrierServiceId) {
        Basket basket = basketRepository.findById(basketId)
            .orElseThrow(() -> new RuntimeException("Panier non trouvé"));
        
        basket.setCarrierServiceId(carrierServiceId);
        basket.setUpdatedAt(LocalDateTime.now());
        
        log.info("Service de transport défini pour panier: {} | CarrierService: {}", 
            basketId, carrierServiceId);
        
        return basketRepository.save(basket);
    }
    
    /**
     * Définir les estimations de livraison
     */
    public Basket setDeliveryEstimates(Long basketId, LocalDate estimatedDeliveryDate, LocalDate latestDeliveryDate) {
        Basket basket = basketRepository.findById(basketId)
            .orElseThrow(() -> new RuntimeException("Panier non trouvé"));
        
        basket.setEstimatedDeliveryDate(estimatedDeliveryDate);
        basket.setLatestDeliveryDate(latestDeliveryDate);
        basket.setUpdatedAt(LocalDateTime.now());
        
        log.info("Estimations de livraison définies pour panier: {}", basketId);
        
        return basketRepository.save(basket);
    }
    
    /**
     * Définir la méthode de paiement
     */
    public void setPaymentMethod(Long basketId, Long paymentMethodId) {
        Basket basket = basketRepository.findById(basketId)
            .orElseThrow(() -> new RuntimeException("Panier non trouvé"));
        
        basket.setPaymentMethodId(paymentMethodId);
        basket.setUpdatedAt(LocalDateTime.now());
        basketRepository.save(basket);
        
        log.info("Methode de paiement définie: {} pour panier: {}", paymentMethodId, basketId);
    }
    
    /**
     * Marquer le panier comme complété
     */
    @Transactional
    public Basket completeBasket(Long basketId) {
        Basket basket = basketRepository.findById(basketId)
            .orElseThrow(() -> new RuntimeException("Panier non trouvé"));
        
        basket.setStatus(STATUS_COMPLETED);
        basket.setUpdatedAt(LocalDateTime.now());
        basket.setCompletedAt(LocalDateTime.now());
        
        Basket saved = basketRepository.save(basket);
        
        log.info("Panier complété: {}", basketId);
        
        return saved;
    }
    
    /**
     * Vérifier si un panier est actif
     */
    @Transactional(readOnly = true)
    public boolean isBasketActive(Long basketId) {
        return basketRepository.findById(basketId)
            .map(basket -> STATUS_ACTIVE.equals(basket.getStatus()))
            .orElse(false);
    }
    
    /**
     * Obtenir le statut du panier
     */
    @Transactional(readOnly = true)
    public String getBasketStatus(Long basketId) {
        return basketRepository.findById(basketId)
            .map(Basket::getStatus)
            .orElseThrow(() -> new RuntimeException("Panier non trouvé"));
    }
    
    /**
     * Obtenir les paniers actifs d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<Basket> getActiveBaskets(Long userId) {
        return basketRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, STATUS_ACTIVE);
    }
    
    /**
     * Abandonner un panier
     */
    @Transactional
    public void abandonBasket(Long basketId) {
        Basket basket = basketRepository.findById(basketId)
            .orElseThrow(() -> new RuntimeException("Panier non trouvé"));
        
        basket.setStatus(STATUS_ABANDONED);
        basket.setUpdatedAt(LocalDateTime.now());
        basketRepository.save(basket);
        
        log.info("Panier abandonné: {}", basketId);
    }
    
    public Basket getOrCreateBasketForGuest(String guestId) {
        return basketRepository.findByGuestId(guestId)
            .orElseGet(() -> {
                Basket basket = new Basket();
                basket.setGuestId(guestId);
                basket.setStatus(STATUS_ACTIVE);
                basket.setCreatedAt(LocalDateTime.now());
                basket.setUpdatedAt(LocalDateTime.now());
                return basketRepository.save(basket);
            });
    }
    
    public Basket mergeGuestBasketToUser(Long userId, String guestId) {
        Basket userBasket = getOrCreateBasketForUser(userId);
        Optional<Basket> guestBasketOpt = basketRepository.findByGuestId(guestId);
        
        if (guestBasketOpt.isPresent()) {
            Basket guestBasket = guestBasketOpt.get();
            List<ProductLineItem> guestItems = lineItemService.getLineItemsByBasketId(guestBasket.getId());
            for (ProductLineItem item : guestItems) {
                lineItemService.addOrUpdateLineItem(
                    userBasket.getId(),
                    item.getProductId(),
                    item.getQuantity(),
                    item.getUnitPrice()
                );
            }
            lineItemService.clearBasket(guestBasket.getId());
            basketRepository.delete(guestBasket);
        }
        return userBasket;
    }
}