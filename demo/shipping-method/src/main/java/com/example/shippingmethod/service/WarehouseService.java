package com.example.shippingmethod.service;

import com.example.product.dto.WarehouseAvailabilityDto;
import com.example.product.dto.ProductStockDto;
import com.example.product.service.ProductStockService;
import com.example.shippingmethod.dto.BasketItemDto;
import com.example.shippingmethod.model.Warehouse;
import com.example.shippingmethod.repository.WarehouseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
public class WarehouseService {
    
    private final WarehouseRepository warehouseRepository;
    private final ProductStockService productStockService;
    
    public WarehouseService(WarehouseRepository warehouseRepository, 
                           ProductStockService productStockService) {
        this.warehouseRepository = warehouseRepository;
        this.productStockService = productStockService;
    }
    
    /**
     * Récupérer tous les entrepôts
     */
    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAll();
    }
    
    /**
     * Récupérer les entrepôts actifs
     */
    public List<Warehouse> getActiveWarehouses() {
        return warehouseRepository.findByEnabledTrue();
    }
    
    /**
     * Récupérer un entrepôt par ID
     */
    public Optional<Warehouse> getWarehouseById(Long id) {
        return warehouseRepository.findById(id);
    }
    
    /**
     * Récupérer un entrepôt par code
     */
    public Optional<Warehouse> getWarehouseByCode(String code) {
        return warehouseRepository.findByCode(code);
    }
    
    /**
     * Récupérer les entrepôts par région
     */
    public List<Warehouse> getWarehousesByRegion(String region) {
        return warehouseRepository.findByRegion(region);
    }
    
    /**
     * Récupérer les entrepôts par pays
     */
    public List<Warehouse> getWarehousesByCountry(String country) {
        return warehouseRepository.findByCountryAndEnabledTrue(country);
    }
    
    /**
     * ⭐ Trouver le meilleur entrepôt capable de fulfiller la commande
     * 
     * Stratégie :
     * 1. Trouver tous les entrepôts qui contiennent TOUS les produits avec les quantités requises
     * 2. Parmi ceux-ci, sélectionner celui du pays de livraison (priorité)
     * 3. Si aucun dans le pays, prendre celui de la région
     * 4. Fallback : premier entrepôt actif disponible
     * 
     * Utilise BasketItemDto pour éviter la dépendance circulaire
     */
    public Warehouse findBestWarehouseForOrder(
            List<BasketItemDto> basketItems,
            String destinationCountry,
            String destinationRegion) {
        
        log.info("🔍 Recherche du meilleur entrepôt pour {} produits | Destination: {}",
            basketItems.size(), destinationCountry);
        
        // Étape 1 : Trouver les entrepôts qui peuvent fulfiller la commande entière
        List<Warehouse> availableWarehouses = findWarehousesWithAllProducts(basketItems);
        
        if (availableWarehouses.isEmpty()) {
            log.warn("⚠️ Aucun entrepôt ne peut fulfiller la commande | Fallback sur le premier actif");
            return getActiveWarehouses().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aucun entrepôt actif disponible"));
        }
        
        log.info("✅ {} entrepôt(s) peuvent fulfiller la commande", availableWarehouses.size());
        
        // Étape 2 : Priorité au pays de livraison
        Optional<Warehouse> warehouseInCountry = availableWarehouses.stream()
            .filter(w -> w.getCountry().equalsIgnoreCase(destinationCountry))
            .findFirst();
        
        if (warehouseInCountry.isPresent()) {
            log.info("✅ Entrepôt trouvé dans le pays: {}", warehouseInCountry.get().getName());
            return warehouseInCountry.get();
        }
        
        // Étape 3 : Région
        Optional<Warehouse> warehouseInRegion = availableWarehouses.stream()
            .filter(w -> w.getRegion().equalsIgnoreCase(destinationRegion))
            .findFirst();
        
        if (warehouseInRegion.isPresent()) {
            log.info("✅ Entrepôt trouvé dans la région: {}", warehouseInRegion.get().getName());
            return warehouseInRegion.get();
        }
        
        // Étape 4 : Fallback - Premier dispo
        log.warn("⚠️ Aucun entrepôt dans le pays/région | Fallback sur le premier disponible");
        return availableWarehouses.get(0);
    }
    
    /**
     * Trouver tous les entrepôts qui contiennent TOUS les produits du panier
     * avec les quantités requises
     */
    private List<Warehouse> findWarehousesWithAllProducts(List<BasketItemDto> basketItems) {
        if (basketItems.isEmpty()) {
            return getActiveWarehouses();
        }
        
        // Pour chaque produit, trouver les entrepôts le contenant
        Map<Long, Set<Long>> productToWarehouses = new HashMap<>();
        
        for (BasketItemDto item : basketItems) {
            Set<Long> warehouseIds = productStockService
                .findWarehouseIdsWithProduct(item.getProductId())
                .stream()
                .collect(Collectors.toSet());
            
            productToWarehouses.put(item.getProductId(), warehouseIds);
            
            log.debug("Produit {} trouvé dans {} entrepôt(s)", item.getProductId(), warehouseIds.size());
        }
        
        // Intersection : les entrepôts qui contiennent TOUS les produits
        if (productToWarehouses.isEmpty()) {
            return new ArrayList<>();
        }
        
        Set<Long> commonWarehouses = null;
        for (Set<Long> warehouseIds : productToWarehouses.values()) {
            if (commonWarehouses == null) {
                commonWarehouses = new HashSet<>(warehouseIds);
            } else {
                commonWarehouses.retainAll(warehouseIds);
            }
        }
        
        if (commonWarehouses == null || commonWarehouses.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Récupérer les entrepôts actifs de la liste commune
        return commonWarehouses.stream()
            .map(id -> warehouseRepository.findById(id))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(Warehouse::getEnabled)
            .collect(Collectors.toList());
    }
    
    /**
     * Obtenir la disponibilité pour un entrepôt avec BasketItemDto
     */
    public WarehouseAvailabilityDto getAvailabilityForWarehouse(
            Warehouse warehouse,
            List<BasketItemDto> basketItems) {
        
        List<ProductStockDto> availability = basketItems.stream()
            .map(item -> {
                ProductStockDto dto = new ProductStockDto();
                dto.setProductId(item.getProductId());
                dto.setWarehouseId(warehouse.getId());
                
                boolean inStock = productStockService.isProductInStock(
                    item.getProductId(),
                    warehouse.getId(),
                    item.getQuantity());
                
                dto.setInStock(inStock);
                return dto;
            })
            .collect(Collectors.toList());
        
        int available = (int) availability.stream().filter(ProductStockDto::getInStock).count();
        int total = availability.size();
        
        return WarehouseAvailabilityDto.builder()
            .warehouseId(warehouse.getId())
            .warehouseName(warehouse.getName())
            .warehouseCode(warehouse.getCode())
            .country(warehouse.getCountry())
            .region(warehouse.getRegion())
            .canFulfillOrder(available == total)
            .productAvailability(availability)
            .availableProducts(available)
            .totalProducts(total)
            .build();
    }
    
    /**
     * Obtenir la disponibilité de tous les entrepôts pour les articles du panier
     */
    public List<WarehouseAvailabilityDto> getAvailabilityForAllWarehouses(
            List<BasketItemDto> basketItems) {
        
        List<Warehouse> warehouses = getActiveWarehouses();
        
        return warehouses.stream()
            .map(warehouse -> getAvailabilityForWarehouse(warehouse, basketItems))
            .collect(Collectors.toList());
    }
	@Transactional
    public Warehouse saveWarehouse(Warehouse warehouse) {
		if (warehouseRepository.findByCode(warehouse.getCode()).isPresent()) {
            throw new RuntimeException("Un transporteur avec ce code existe déjà");
        }
        
		warehouse.setCreatedAt(LocalDateTime.now());
		warehouse.setUpdatedAt(LocalDateTime.now());
        
        Warehouse saved = warehouseRepository.save(warehouse);
        log.info("Warehouse created: {} ({})", warehouse.getName(), warehouse.getCode());
        
        return saved;
    }
	@Transactional
	 public Warehouse updateWarehouse(Long id, Warehouse warehouse) {
	        Warehouse existing = warehouseRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("Carrier not found"));
	        
	        // Vérifier que le code est unique (si modifié)
	        if (!existing.getCode().equals(warehouse.getCode()) && 
	            warehouseRepository.findByCode(warehouse.getCode()).isPresent()) {
	            throw new RuntimeException("Un entrepot avec ce code existe déjà");
	        }
	        
	        existing.setName(warehouse.getName());
	        existing.setCode(warehouse.getCode());
	        existing.setCity(warehouse.getCity());
	        existing.setCountry(warehouse.getCountry());
	        existing.setPostalCode(warehouse.getPostalCode());
	        existing.setRegion(warehouse.getRegion());
	        existing.setStreet(warehouse.getStreet());
	       
	        existing.setEnabled(warehouse.getEnabled());
	        existing.setUpdatedAt(LocalDateTime.now());
	        
	        Warehouse updated = warehouseRepository.save(existing);
	        log.info("Carrier updated: {} ({})", warehouse.getName(), warehouse.getCode());
	        
	        return updated;
	 }
	@Transactional
    public void deleteWarehouse(Long id) {
        warehouseRepository.deleteById(id);
    }
}