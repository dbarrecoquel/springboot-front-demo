package com.example.shopping.service;

import com.example.shopping.model.ProductLineItem;
import com.example.shopping.repository.ProductLineItemRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class ProductLineItemService {
    
    private final ProductLineItemRepository lineItemRepository;
    
    public ProductLineItemService(ProductLineItemRepository lineItemRepository) {
        this.lineItemRepository = lineItemRepository;
    }
    
    public List<ProductLineItem> getLineItemsByBasketId(Long basketId) {
        return lineItemRepository.findByBasketId(basketId);
    }
    
    /**
     * Ajouter ou mettre à jour un article dans le panier
     */
    @Transactional
    public ProductLineItem addOrUpdateLineItem(Long basketId, Long productId, Integer quantity, Double unitPrice) {
        
        try {
            // Chercher si l'article existe déjà dans le panier
            Optional<ProductLineItem> existingItem = lineItemRepository.findByBasketIdAndProductId(basketId, productId);
            
            if (existingItem.isPresent()) {
                // Mettre à jour la quantité
                ProductLineItem item = existingItem.get();
                item.setQuantity(item.getQuantity() + quantity);
                item.setUpdatedAt(LocalDateTime.now());
                
                log.info("Article mis à jour: Panier {} | Produit {} | Nouvelle quantité: {}", 
                    basketId, productId, item.getQuantity());
                
                return lineItemRepository.save(item);
            } else {
                // Créer un nouvel article
                ProductLineItem newItem = new ProductLineItem();
                newItem.setBasketId(basketId);
                newItem.setProductId(productId);
                newItem.setQuantity(quantity);
                newItem.setUnitPrice(unitPrice);
                
                log.info("Article ajouté: Panier {} | Produit {} | Quantité: {}", 
                    basketId, productId, quantity);
                
                return lineItemRepository.save(newItem);
            }
        } catch (Exception e) {
            log.error("Erreur ajout/mise à jour article: {}", e.getMessage());
            throw new RuntimeException("Erreur ajout article: " + e.getMessage());
        }
    }
    
    /**
     * Fusionne les articles d'un panier source vers un panier destination
     */
    public void mergeBasketItems(Long sourceBasketId, Long targetBasketId) {
        System.out.println("🔀 Fusion articles - Source: " + sourceBasketId + " → Cible: " + targetBasketId);
        
        List<ProductLineItem> sourceItems = getLineItemsByBasketId(sourceBasketId);
        
        System.out.println("📊 Nombre d'articles à fusionner: " + sourceItems.size());
        
        for (ProductLineItem sourceItem : sourceItems) {
            System.out.println("  ➕ Ajout produit ID: " + sourceItem.getProductId() + " (Qté: " + sourceItem.getQuantity() + ")");
            addOrUpdateLineItem(
                targetBasketId,
                sourceItem.getProductId(),
                sourceItem.getQuantity(),
                sourceItem.getUnitPrice()
            );
        }
        
        // Supprimer les articles du panier source
        System.out.println("🗑️ Nettoyage panier source");
        clearBasket(sourceBasketId);
        
        System.out.println("✅ Articles fusionnés avec succès");
    }
    
    public ProductLineItem updateQuantity(Long lineItemId, Integer quantity) {
        ProductLineItem item = lineItemRepository.findById(lineItemId)
                .orElseThrow(() -> new RuntimeException("Line item not found"));
        item.setQuantity(quantity);
        return lineItemRepository.save(item);
    }
    
    public void deleteLineItem(Long lineItemId) {
        lineItemRepository.deleteById(lineItemId);
    }
    
    public void clearBasket(Long basketId) {
        lineItemRepository.deleteByBasketId(basketId);
    }
    
    public Double calculateBasketTotal(Long basketId) {
        List<ProductLineItem> items = getLineItemsByBasketId(basketId);
        return items.stream()
                .mapToDouble(ProductLineItem::getSubtotal)
                .sum();
    }
    
    public int getBasketItemCount(Long basketId) {
        List<ProductLineItem> items = getLineItemsByBasketId(basketId);
        return items.stream()
                .mapToInt(ProductLineItem::getQuantity)
                .sum();
    }
}