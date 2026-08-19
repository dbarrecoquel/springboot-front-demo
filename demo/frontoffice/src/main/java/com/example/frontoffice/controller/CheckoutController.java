package com.example.frontoffice.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.address.model.Address;
import com.example.address.service.AddressService;
import com.example.product.service.ProductStockService;
import com.example.shippingmethod.dto.DeliveryEstimateDTO;
import com.example.shippingmethod.model.ShippingMethod;
import com.example.shippingmethod.model.Warehouse;
import com.example.shippingmethod.service.CarrierServiceService;
import com.example.shippingmethod.service.DeliveryEstimationService;
import com.example.shippingmethod.service.ShippingMethodService;
import com.example.shippingmethod.service.WarehouseService;
import com.example.shopping.model.Basket;
import com.example.shopping.model.ProductLineItem;
import com.example.shopping.service.BasketCalculationService;
import com.example.shopping.service.BasketService;
import com.example.shopping.service.ProductLineItemService;
import com.example.shopping.service.WarehouseSelectionService;
import com.example.user.model.User;
import com.example.user.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {
	
	private static final Logger log = LoggerFactory.getLogger(CheckoutController.class);
	
	private final BasketService basketService;
    private final AddressService addressService;
    private final ProductLineItemService lineItemService;
    private final UserService userService;
    private final BasketCalculationService calcService ;
    private final DeliveryEstimationService deliveryEstimationService;
    private final WarehouseService warehouseService;
    private final WarehouseSelectionService warehouseSelectionService;
    private final CarrierServiceService carrierServiceService;
    private final ProductStockService productStockService;
    
    public CheckoutController(
            BasketService basketService,
            AddressService addressService,
            ProductLineItemService lineItemService,
            UserService userService,
            CarrierServiceService carrierServiceService,
            DeliveryEstimationService deliveryEstimationService,
            WarehouseSelectionService warehouseSelectionService,
            BasketCalculationService calcService,
            WarehouseService warehouseService,
            ProductStockService productStockService) {
        
        this.basketService = basketService;
        this.addressService = addressService;
        this.lineItemService = lineItemService;
        this.userService = userService;
        this.carrierServiceService = carrierServiceService;
        this.deliveryEstimationService = deliveryEstimationService;
        this.warehouseSelectionService = warehouseSelectionService;  
        this.calcService = calcService;
        this.warehouseService = warehouseService;
        this.productStockService = productStockService;
    }
	
	@GetMapping("/addresses")
	public String viewCheckoutAddresses(Authentication authentication, HttpSession session, Model model)
	{
		if (authentication == null || !authentication.isAuthenticated())
			return "redirect:/login";
		
		User user = userService.findByEmail(authentication.getName()).orElseThrow(() -> new RuntimeException("User not found"));
		
		Basket basket = basketService.getOrCreateBasket(user.getId(), session.getId());
		
		List<ProductLineItem> plis = lineItemService.getLineItemsByBasketId(basket.getId());
		
		 Double total = lineItemService.calculateBasketTotal(basket.getId());
		
		if (plis.isEmpty())
			return "redirect:/basket"; 
		
		List<Address> billingAddresses = addressService.getAddressesByUserIdAndType(user.getId(), "BILLING");
        List<Address> shippingAddresses = addressService.getAddressesByUserIdAndType(user.getId(), "SHIPPING");
        
        model.addAttribute("total", total);
        model.addAttribute("items", plis);
        model.addAttribute("basket", basket);
        model.addAttribute("shippingAddresses", shippingAddresses);
        model.addAttribute("billingAddresses", billingAddresses);
        model.addAttribute("selectedShippingId", basket.getShippingAddressId());
        model.addAttribute("selectedBillingId", basket.getBillingAddressId());
		
		
		return "checkout-addresses";
	}
	
	/**
	 * POST - Sélection des adresses et sélection automatique d'entrepôt
	 */
	@PostMapping("/addresses")
	public String selectAddresses(
	        @RequestParam Long billingAddressId,
	        @RequestParam Long shippingAddressId,
	        Authentication auth,
	        HttpSession session,
	        RedirectAttributes redirectAttributes) {
	    
	    if (auth == null || !auth.isAuthenticated()) {
	        return "redirect:/login";
	    }
	    
	    try {
	        User user = userService.findByEmail(auth.getName())
	            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
	        
	        Basket basket = basketService.getOrCreateBasket(user.getId(), session.getId());
	        
	        log.info("📋 Tentative de sélection d'adresses | Facturation: {} | Livraison: {}", 
	            billingAddressId, shippingAddressId);
	        
	        // Valider les adresses
	        Address billing = addressService.getAddressById(billingAddressId)
	            .orElseThrow(() -> new RuntimeException("Adresse de facturation invalide"));
	        
	        Address shipping = addressService.getAddressById(shippingAddressId)
	            .orElseThrow(() -> new RuntimeException("Adresse de livraison invalide"));
	        
	        // Sauvegarder les adresses dans le panier
	        basketService.setCheckoutAddresses(basket.getId(),billingAddressId, shippingAddressId);
	        
	        log.info("✅ Adresses sauvegardées | Facturation: {} | Livraison: {}", 
	            billing.getCity(), shipping.getCity());
	        
	        // Récupérer les articles du panier
	        List<ProductLineItem> basketItems = lineItemService.getLineItemsByBasketId(basket.getId());
	        
	        if (basketItems == null || basketItems.isEmpty()) {
	            log.warn("⚠️ Panier vide");
	            redirectAttributes.addFlashAttribute("error", "Votre panier est vide");
	            return "redirect:/basket";
	        }
	        
	        log.info("📦 {} article(s) dans le panier", basketItems.size());
	        
	        // ✅ Sélectionner automatiquement le meilleur entrepôt
	        try {
	            Warehouse selectedWarehouse = warehouseSelectionService.selectBestWarehouse(
	                basketItems,
	                shipping.getCountry(),
	                shipping.getRegion()
	            );
	            
	            basketService.setWarehouse(basket.getId(), selectedWarehouse.getId());
	            
	            log.info("✅ Entrepôt sélectionné: {} | Redirection vers shipping", 
	                selectedWarehouse.getName());
	            
	        } catch (Exception e) {
	            log.error("❌ Erreur sélection entrepôt", e);
	            redirectAttributes.addFlashAttribute("error", "Aucun entrepôt disponible pour votre région");
	            return "redirect:/checkout/addresses";
	        }
	        
	        redirectAttributes.addFlashAttribute("success", "Adresses configurées avec succès");
	        return "redirect:/checkout/shipping";
	        
	    } catch (Exception e) {
	        log.error("❌ Erreur sélection adresses", e);
	        redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
	        return "redirect:/checkout/addresses";
	    }
	}
	
	@GetMapping("/addresses/new")
	public String showAddAddressForm(@RequestParam(required = false) String type,
            Authentication authentication,
            Model model) {
		
		if (authentication == null || !authentication.isAuthenticated())
			return "redirect:/login";
		
		Address address = new Address();
		
		if (type != null && !type.trim().isEmpty())
			address.setAddressType(type.toUpperCase());
		
		model.addAttribute("address", address);
		model.addAttribute("returnToCheckout", true);
		
		return "address-form";
		
	}
	
	@PostMapping("/addresses/new")
	public String addAddressFromCheckout(@Valid @ModelAttribute Address address,
            BindingResult result,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {
		
		if (authentication == null || !authentication.isAuthenticated())
			return "redirect:/login";
		
		if (result.hasErrors()) {
			model.addAttribute("returnToCheckout", true);
			return "address-form";
		}
					
		
		User user = userService.findByEmail(authentication.getName()).orElseThrow(()-> new RuntimeException("user not found"));
		
		address.setUserId(user.getId());
		
		addressService.saveAddress(address);
		
		redirectAttributes.addFlashAttribute("success", "Adresse ajoutée avec succès");
	        
	    return "redirect:/checkout/addresses";
		
	}
	
	/**
	 * Étape 2 : Sélection du mode de livraison
	 */
	@GetMapping("/shipping")
	public String viewShipping(
	        Authentication auth,
	        HttpSession session,
	        Model model) {
	    
	    if (auth == null || !auth.isAuthenticated()) {
	        return "redirect:/login";
	    }
	    
	    try {
	        User user = userService.findByEmail(auth.getName())
	            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
	        
	        Basket basket = basketService.getOrCreateBasket(user.getId(), session.getId());
	        
	        // Vérifier les adresses
	        if (basket.getShippingAddressId() == null || basket.getBillingAddressId() == null) {
	            return "redirect:/checkout/addresses";
	        }
	        
	        // Récupérer l'adresse de livraison
	        Address shippingAddress = addressService.getAddressById(basket.getShippingAddressId())
	            .orElseThrow(() -> new RuntimeException("Adresse de livraison non trouvée"));
	        
	        // Récupérer l'entrepôt
	        Warehouse warehouse = warehouseService.getWarehouseById(basket.getWarehouseId())
	            .orElseThrow(() -> new RuntimeException("Entrepôt non trouvé"));
	        
	        // Vérifier que l'entrepôt peut fulfiller la commande
	        List<ProductLineItem> basketItems = lineItemService.getLineItemsByBasketId(basket.getId());
	        
	        boolean canFulfill = warehouseSelectionService.canFulfillOrder(warehouse, basketItems);
	        
	        if (!canFulfill) {
	            basket.setWarehouseId(null);
	            basketService.save(basket);
	            return "redirect:/checkout/addresses";
	        }
	        
	        // ✅ Récupérer les options de livraison disponibles
	        Map<String, Double> totals = calcService.calculateBasketTotals(basket);
	        Double total = (Double) totals.get("total");
	        
	        List<DeliveryEstimateDTO> deliveryEstimates = 
	            deliveryEstimationService.getAvailableDeliveryOptions(
	                warehouse.getId(),
	                total,
	                java.time.LocalDateTime.now()
	            );
	        
	        // Ajouter les attributs au modèle
	        model.addAttribute("basket", basket);
	        model.addAttribute("shippingAddress", shippingAddress);
	        model.addAttribute("warehouse", warehouse);
	        model.addAttribute("items", basketItems);
	        model.addAttribute("deliveryEstimates", deliveryEstimates);
	        model.addAttribute("subtotal", totals.get("subtotal"));
	        model.addAttribute("tax", totals.get("tax"));
	        model.addAttribute("total", total);
	        model.addAttribute("selectedCarrierServiceId", basket.getCarrierServiceId());
	        log.info("📦 Items: {}", basketItems.size());
	        log.info("📦 Warehouse: {}", warehouse.getName());
	        log.info("📦 Delivery Estimates: {}", deliveryEstimates.size());
	        log.info("📦 Model attributes added successfully");
	        
	        return "checkout-shipping";
	        
	    } catch (Exception e) {
	        return "redirect:/checkout/addresses";
	    }
	}

	/**
	 * POST - Sélection du service de transport
	 */
	@PostMapping("/shipping")
	public String selectShipping(
	        @RequestParam Long carrierServiceId,
	        Authentication auth,
	        HttpSession session,
	        RedirectAttributes redirectAttributes) {
	    
	    if (auth == null || !auth.isAuthenticated()) {
	        return "redirect:/login";
	    }
	    
	    try {
	        User user = userService.findByEmail(auth.getName())
	            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
	        
	        Basket basket = basketService.getOrCreateBasket(user.getId(), session.getId());
	        
	        // Valider le service de transport
	        carrierServiceService.getServiceDtoById(carrierServiceId)
	            .orElseThrow(() -> new RuntimeException("Service de transport invalide"));
	        
	        // Récupérer les données de livraison
	        java.time.LocalDateTime orderDateTime = java.time.LocalDateTime.now();
	        Map<String, Double> totals = calcService.calculateBasketTotals(basket);
	        Double total = (Double) totals.get("total");
	        
	        // Calculer l'estimation de livraison
	        DeliveryEstimateDTO estimate = deliveryEstimationService.estimateDelivery(
	            carrierServiceId,
	            basket.getWarehouseId(),
	            total,
	            orderDateTime
	        );
	        
	        // Sauvegarder le service et les estimations
	        basketService.setCarrierService(basket.getId(), carrierServiceId);
	        basketService.setDeliveryEstimates(
	            basket.getId(),
	            estimate.getEarliestDeliveryDate(),
	            estimate.getLatestDeliveryDate()
	        );
	        
	        
	        redirectAttributes.addFlashAttribute("success", "Mode de livraison sélectionné");
	        return "redirect:/checkout/payment";
	        
	    } catch (Exception e) {
	        redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
	        return "redirect:/checkout/shipping";
	    }
	}
}
