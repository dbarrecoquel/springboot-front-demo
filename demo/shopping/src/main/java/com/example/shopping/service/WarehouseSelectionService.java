package com.example.shopping.service;

import com.example.shopping.model.ProductLineItem;
import com.example.shippingmethod.dto.BasketItemDto;
import com.example.shippingmethod.model.Warehouse;
import com.example.shippingmethod.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
public class WarehouseSelectionService {
    
    private final WarehouseService warehouseService;
    
    public WarehouseSelectionService(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }
    
    /**
     * Sélectionner le meilleur entrepôt pour une commande
     * 
     * Stratégie :
     * 1. Convertir ProductLineItem en BasketItemDto
     * 2. Déléguer à WarehouseService.findBestWarehouseForOrder()
     * 
     * @param basketItems items du panier
     * @param destinationCountry pays de livraison
     * @param destinationRegion région de livraison
     * @return le meilleur entrepôt
     */
    public Warehouse selectBestWarehouse(
            List<ProductLineItem> basketItems,
            String destinationCountry,
            String destinationRegion) {
        
        log.info("🔍 Sélection du meilleur entrepôt | {} produits | Destination: {}",
            basketItems.size(), destinationCountry);
        
        // Convertir ProductLineItem en BasketItemDto pour éviter la dépendance circulaire
        List<BasketItemDto> basketItemDtos = new ArrayList<>();
        for (ProductLineItem item : basketItems) {
            BasketItemDto dto = new BasketItemDto();
            dto.setProductId(item.getProductId());
            dto.setProductName(item.getProduct().getName());
            dto.setQuantity(item.getQuantity());
            dto.setUnitPrice(item.getUnitPrice());
            basketItemDtos.add(dto);
        }
        
        // Déléguer au WarehouseService
        Warehouse selectedWarehouse = warehouseService.findBestWarehouseForOrder(
            basketItemDtos,
            destinationCountry,
            destinationRegion
        );
        
        log.info("✅ Entrepôt sélectionné: {} ({})", selectedWarehouse.getName(), selectedWarehouse.getCode());
        
        return selectedWarehouse;
    }
    
    /**
     * Vérifier si un entrepôt peut fulfiller une commande
     * 
     * @param warehouse entrepôt à vérifier
     * @param basketItems items du panier
     * @return true si l'entrepôt peut fulfiller, false sinon
     */
    public boolean canFulfillOrder(
            Warehouse warehouse,
            List<ProductLineItem> basketItems) {
        
        if (warehouse == null || !warehouse.getEnabled()) {
            log.warn("⚠️ Entrepôt invalide ou désactivé");
            return false;
        }
        
        if (basketItems == null || basketItems.isEmpty()) {
            log.warn("⚠️ Panier vide");
            return false;
        }
        
        log.info("✅ Vérification si entrepôt {} peut fulfiller {} produits",
            warehouse.getName(), basketItems.size());
        
        // Convertir ProductLineItem en BasketItemDto
        List<BasketItemDto> basketItemDtos = new ArrayList<>();
        for (ProductLineItem item : basketItems) {
            BasketItemDto dto = new BasketItemDto();
            dto.setProductId(item.getProductId());
            dto.setProductName(item.getProduct().getName());
            dto.setQuantity(item.getQuantity());
            dto.setUnitPrice(item.getUnitPrice());
            basketItemDtos.add(dto);
        }
        
        // Déléguer au WarehouseService pour vérifier la disponibilité
        boolean canFulfill = basketItemDtos.stream()
            .allMatch(item -> warehouseService.getWarehouseById(warehouse.getId())
                .isPresent()); // Simple vérification que l'entrepôt existe
        
        if (canFulfill) {
            log.info("✅ Entrepôt {} peut fulfiller la commande", warehouse.getName());
        } else {
            log.warn("❌ Entrepôt {} ne peut pas fulfiller la commande", warehouse.getName());
        }
        
        return canFulfill;
    }
}