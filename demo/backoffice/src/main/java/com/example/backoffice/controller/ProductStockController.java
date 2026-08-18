package com.example.backoffice.controller;

import com.example.product.dto.ProductStockDto;
import com.example.product.enums.StockStatus;
import com.example.product.service.ProductStockService;
import com.example.shippingmethod.model.Warehouse;
import com.example.shippingmethod.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/product-stocks")
@Slf4j
public class ProductStockController {
    
    private final ProductStockService productStockService;
    private final WarehouseService warehouseService;
    
    private static final int PAGE_SIZE = 10;
    
    public ProductStockController(ProductStockService productStockService,
                                 WarehouseService warehouseService) {
        this.productStockService = productStockService;
        this.warehouseService = warehouseService;
    }
    
    /**
     * Afficher la liste des stocks avec filtrage et pagination
     */
    @GetMapping
    public String list(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir,
            Model model) {
        
        try {
            // Récupérer les entrepôts pour le filtre
            List<Warehouse> warehouses = warehouseService.getActiveWarehouses();
            model.addAttribute("warehouses", warehouses);
            
            // Sauvegarder les filtres actuels
            model.addAttribute("selectedWarehouseId", warehouseId);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", pageSize);
            StockStatus statusEnum = null;
            if (status != null && !status.isEmpty()) {
                try {
                    statusEnum = StockStatus.valueOf(status.toUpperCase());
                    log.info("✅ Statut filtré converti: {} -> {}", status, statusEnum);
                } catch (IllegalArgumentException e) {
                    log.warn("Statut invalide: {}. Filtre ignoré.", status);
                    statusEnum = null;
                }
            }
            // Créer la pagination
            Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sortDir, sortBy));
            
            // Récupérer les stocks filtrés
            Page<ProductStockDto> stocksPage = productStockService.getFilteredStocks(warehouseId, statusEnum, pageable);
            
            // Ajouter au modèle
            model.addAttribute("stocks", stocksPage.getContent());
            model.addAttribute("totalPages", stocksPage.getTotalPages());
            model.addAttribute("totalElements", stocksPage.getTotalElements());
            model.addAttribute("currentPage", page);
            model.addAttribute("hasPreviousPage", stocksPage.hasPrevious());
            model.addAttribute("hasNextPage", stocksPage.hasNext());
            model.addAttribute("isFirstPage", stocksPage.isFirst());
            model.addAttribute("isLastPage", stocksPage.isLast());
            
            log.info("Affichage de la liste des stocks | Page: {} | Total: {} | Entrepôt: {} | Statut: {}", 
                page, stocksPage.getTotalElements(), warehouseId, status);
            
            return "product-stocks/list";
        } catch (Exception e) {
            log.error("Erreur lors du chargement de la liste des stocks", e);
            return "redirect:/";
        }
    }
    
    /**
     * Afficher le formulaire de création
     */
    @GetMapping("/create")
    public String create(Model model) {
        try {
            List<Warehouse> warehouses = warehouseService.getActiveWarehouses();
            ProductStockDto emptyStock = new ProductStockDto();
            
            model.addAttribute("warehouses", warehouses);
            model.addAttribute("stock", emptyStock); 
            model.addAttribute("isEdit", false);
            
            return "product-stocks/form";
        } catch (Exception e) {
            log.error("Erreur lors de l'affichage du formulaire", e);
            return "redirect:/product-stocks";
        }
    }
    
    /**
     * Créer un nouveau stock
     */
    @PostMapping
    public String save(
            @RequestParam Long productId,
            @RequestParam Long warehouseId,
            @RequestParam Integer quantity,
            @RequestParam(defaultValue = "10") Integer minimumStock,
            RedirectAttributes redirectAttributes) {
        
        try {
            productStockService.createOrUpdateStock(productId, warehouseId, quantity, minimumStock);
            redirectAttributes.addFlashAttribute("success", "Stock créé avec succès");
            return "redirect:/product-stocks";
        } catch (Exception e) {
            log.error("Erreur lors de la création du stock", e);
            redirectAttributes.addFlashAttribute("error",  e.getMessage());
            return "redirect:/product-stocks/create";
        }
    }
    
    /**
     * Afficher le formulaire d'édition
     */
    @GetMapping("/{productId}/warehouse/{warehouseId}/edit")
    public String edit(
            @PathVariable Long productId,
            @PathVariable Long warehouseId,
            Model model) {
        
        try {
            ProductStockDto stock = productStockService.getProductStockDto(productId, warehouseId)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé"));
            
            List<Warehouse> warehouses = warehouseService.getActiveWarehouses();
            
            model.addAttribute("stock", stock);
            model.addAttribute("warehouses", warehouses);
            model.addAttribute("isEdit", true);
            
            return "product-stocks/form";
        } catch (Exception e) {
            log.error("Erreur lors du chargement du stock", e);
            return "redirect:/product-stocks";
        }
    }
    
    /**
     * Mettre à jour un stock
     */
    @PostMapping("/{productId}/warehouse/{warehouseId}")
    public String update(
            @PathVariable Long productId,
            @PathVariable Long warehouseId,
            @RequestParam Integer quantity,
            @RequestParam(defaultValue = "10") Integer minimumStock,
            RedirectAttributes redirectAttributes) {
        
        try {
            productStockService.createOrUpdateStock(productId, warehouseId, quantity, minimumStock);
            redirectAttributes.addFlashAttribute("success", "Stock mis à jour avec succès");
            return "redirect:/product-stocks";
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du stock", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/product-stocks";
        }
    }
    
    /**
     * Augmenter le stock
     */
    @PostMapping("/{productId}/warehouse/{warehouseId}/increase")
    public String increase(
            @PathVariable Long productId,
            @PathVariable Long warehouseId,
            @RequestParam Integer quantity,
            RedirectAttributes redirectAttributes) {
        
        try {
            productStockService.increaseStock(productId, warehouseId, quantity);
            redirectAttributes.addFlashAttribute("success", "Stock augmenté de " + quantity + " unité(s)");
            return "redirect:/product-stocks";
        } catch (Exception e) {
            log.error("Erreur lors de l'augmentation du stock", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/product-stocks";
        }
    }
    
    /**
     * Réduire le stock
     */
    @PostMapping("/{productId}/warehouse/{warehouseId}/decrease")
    public String decrease(
            @PathVariable Long productId,
            @PathVariable Long warehouseId,
            @RequestParam Integer quantity,
            RedirectAttributes redirectAttributes) {
        
        try {
            productStockService.decreaseStock(productId, warehouseId, quantity);
            redirectAttributes.addFlashAttribute("success", "Stock réduit de " + quantity + " unité(s)");
            return "redirect:/product-stocks";
        } catch (Exception e) {
            log.error("Erreur lors de la réduction du stock", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/product-stocks";
        }
    }
    
    /**
     * Supprimer un stock
     */
    @GetMapping("/{productId}/warehouse/{warehouseId}/delete")
    public String delete(
            @PathVariable Long productId,
            @PathVariable Long warehouseId,
            RedirectAttributes redirectAttributes) {
        
        try {
            productStockService.deleteStock(productId, warehouseId);
            redirectAttributes.addFlashAttribute("success", "Stock supprimé avec succès");
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du stock", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/product-stocks";
    }
}