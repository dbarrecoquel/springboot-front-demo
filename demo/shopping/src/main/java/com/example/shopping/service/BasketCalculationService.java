package com.example.shopping.service;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.example.shippingmethod.model.ShippingMethod;
import com.example.shippingmethod.service.CarrierServiceService;
import com.example.shippingmethod.service.ShippingMethodService;
import com.example.shopping.model.Basket;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BasketCalculationService {
    
    private static final Double DEFAULT_TAX_RATE = 0.20;
    
    private final ProductLineItemService lineItemService;
    private final ShippingMethodService shippingMethodService;
    private final CarrierServiceService carrierServiceService;
    
    public BasketCalculationService(ProductLineItemService pliService, 
                                    ShippingMethodService shippingMethodService,
                                    CarrierServiceService carrierServiceService) {
        
        this.lineItemService = pliService;
        this.shippingMethodService = shippingMethodService;
        this.carrierServiceService = carrierServiceService;
    }
    
    /**
     * Calculer tous les totaux du panier
     */
    public Map<String, Double> calculateBasketTotals(Basket basket) {
        Map<String, Double> totals = new HashMap<>();
        
        Double subtotal = calculateLineItemsTotal(totals, basket);
        
        Double tax = calculateTax(totals, subtotal);
        
        Double shippingTotal = calculateShippingTotal(totals, basket);
        Double carrierTotal = calculateCarrierServiceTotal(totals, basket);
        
        Double total = subtotal + tax + shippingTotal + carrierTotal;
        totals.put("total", total);
        
        log.info("Totaux panier calculés | Subtotal: {} | Tax: {} | Shipping: {} | Carrier: {} | TOTAL: {}", 
            subtotal, tax, shippingTotal, carrierTotal, total);
        
        return totals;
    }
    
    /**
     * Calculer le sous-total des articles
     */
    public Double calculateLineItemsTotal(Map<String, Double> totals, Basket basket) {
        Double subtotal = lineItemService.calculateBasketTotal(basket.getId());
        totals.put("subtotal", subtotal);
        
        log.info("Sous-total calculé pour panier {}: {}", basket.getId(), subtotal);
        
        return subtotal;
    }
    
    /**
     * CALCULER LES TAXES (par défaut 20% du sous-total)
     */
    public Double calculateTax(Map<String, Double> totals, Double subtotal) {
        Double tax = subtotal * DEFAULT_TAX_RATE;
        totals.put("tax", tax);
        
        log.info("Taxes calculées: {} EUR (Taux: {}%)", tax, DEFAULT_TAX_RATE * 100);
        
        return tax;
    }
    
    /**
     * CALCULER LES TAXES AVEC TAUX PERSONNALISÉ
     */
    public Double calculateTaxWithRate(Map<String, Double> totals, Double subtotal, Double taxRate) {
        Double tax = subtotal * taxRate;
        totals.put("tax", tax);
        
        log.info("Taxes calculées: {} EUR (Taux personnalisé: {}%)", tax, taxRate * 100);
        
        return tax;
    }
    
    /**
     * Calculer les frais de livraison (ShippingMethod - ancien système)
     */
    public Double calculateShippingTotal(Map<String, Double> totals, Basket basket) {
        Double shippingCost = 0.0;
        
        if (basket.getShippingMethodId() != null) {
            ShippingMethod method = shippingMethodService.getShippingMethodById(basket.getShippingMethodId())
                .orElse(null);
            
            if (method != null) {
                shippingCost = method.getCost();
            }
        }
        
        totals.put("shipping", shippingCost);
        log.info("Frais de livraison: {}", shippingCost);
        
        return shippingCost;
    }
    
    /**
     * Calculer les frais du service transporteur (CarrierService - nouveau système)
     */
    public Double calculateCarrierServiceTotal(Map<String, Double> totals, Basket basket) {
        Double carrierCost = 0.0;
        
        if (basket.getCarrierServiceId() != null) {
            try {
                var carrierServiceDto = carrierServiceService.getServiceDtoById(basket.getCarrierServiceId());
                if (carrierServiceDto.isPresent()) {
                    carrierCost = carrierServiceDto.get().getCost();
                }
            } catch (Exception e) {
                log.warn("Impossible de récupérer le coût de livraison: {}", e.getMessage());
            }
        }
        
        totals.put("carrierCost", carrierCost);
        log.info("Frais transporteur: {}", carrierCost);
        
        return carrierCost;
    }
    
    /**
     * MÉTHODE UTILE : Obtenir le taux de taxe par défaut
     */
    public Double getTaxRate() {
        return DEFAULT_TAX_RATE;
    }
    
    /**
     * MÉTHODE UTILE : Calculer le prix TTC (avec taxes)
     */
    public Double calculatePriceWithTax(Double price) {
        return price + (price * DEFAULT_TAX_RATE);
    }
    
    /**
     * MÉTHODE UTILE : Résumé complet du panier
     */
    public Map<String, Object> getBasketSummary(Basket basket) {
        Map<String, Double> totals = calculateBasketTotals(basket);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("subtotal", totals.get("subtotal"));
        summary.put("tax", totals.get("tax"));
        summary.put("shipping", totals.get("shipping"));
        summary.put("carrierCost", totals.get("carrierCost"));
        summary.put("total", totals.get("total"));
        summary.put("itemCount", lineItemService.countBasketItems(basket.getId()));
        summary.put("taxRate", DEFAULT_TAX_RATE * 100); // En pourcentage
        
        return summary;
    }
}