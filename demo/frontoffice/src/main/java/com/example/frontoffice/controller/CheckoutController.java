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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.address.model.Address;
import com.example.address.service.AddressService;
import com.example.order.service.OrderService;
import com.example.payment.dto.PaymentMethodDto;
import com.example.payment.dto.TransactionDto;
import com.example.payment.service.PaymentMethodService;
import com.example.payment.service.PaymentService;
import com.example.payment.service.TransactionService;
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
    private final PaymentMethodService paymentMethodService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final TransactionService transactionService;
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
            ProductStockService productStockService,
            PaymentMethodService paymentMethodService,
            OrderService orderService,
            PaymentService paymentService,
            TransactionService transactionService) {
        
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
        this.paymentMethodService = paymentMethodService;
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.transactionService = transactionService;
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
	        
	        log.info("Adresses sauvegardées | Facturation: {} | Livraison: {}", 
	            billing.getCity(), shipping.getCity());
	        
	        // Récupérer les articles du panier
	        List<ProductLineItem> basketItems = lineItemService.getLineItemsByBasketId(basket.getId());
	        
	        if (basketItems == null || basketItems.isEmpty()) {
	            log.warn("Panier vide");
	            redirectAttributes.addFlashAttribute("error", "Votre panier est vide");
	            return "redirect:/basket";
	        }
	        
	        log.info("{} article(s) dans le panier", basketItems.size());
	        
	        // Sélectionner automatiquement le meilleur entrepôt
	        try {
	            Warehouse selectedWarehouse = warehouseSelectionService.selectBestWarehouse(
	                basketItems,
	                shipping.getCountry(),
	                shipping.getRegion()
	            );
	            
	            basketService.setWarehouse(basket.getId(), selectedWarehouse.getId());
	            
	            log.info("Entrepôt sélectionné: {} | Redirection vers shipping", 
	                selectedWarehouse.getName());
	            
	        } catch (Exception e) {
	            log.error("Erreur sélection entrepôt", e);
	            redirectAttributes.addFlashAttribute("error", "Aucun entrepôt disponible pour votre région");
	            return "redirect:/checkout/addresses";
	        }
	        
	        redirectAttributes.addFlashAttribute("success", "Adresses configurées avec succès");
	        return "redirect:/checkout/shipping";
	        
	    } catch (Exception e) {
	        log.error("Erreur sélection adresses", e);
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
	/**
	 * Étape 3 : Afficher la page de paiement
	 */
	/**
	 * Étape 3 : Afficher la page de paiement
	 */
	@GetMapping("/payment")
	public String viewPayment(
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
	        
	        // Vérifier que tous les choix ont été faits
	        if (basket.getShippingAddressId() == null || 
	            basket.getBillingAddressId() == null ||
	            basket.getCarrierServiceId() == null) {
	            log.warn("Validation paiement échouée - Données manquantes");
	            return "redirect:/checkout/addresses";
	        }
	        
	        // Récupérer les adresses
	        Address shippingAddress = addressService.getAddressById(basket.getShippingAddressId())
	            .orElseThrow(() -> new RuntimeException("Adresse de livraison non trouvée"));
	        
	        Address billingAddress = addressService.getAddressById(basket.getBillingAddressId())
	            .orElseThrow(() -> new RuntimeException("Adresse de facturation non trouvée"));
	        
	        // Récupérer l'entrepôt
	        Warehouse warehouse = warehouseService.getWarehouseById(basket.getWarehouseId())
	            .orElseThrow(() -> new RuntimeException("Entrepôt non trouvé"));
	        
	        // Récupérer les articles
	        List<ProductLineItem> basketItems = lineItemService.getLineItemsByBasketId(basket.getId());
	        
	        // Calculer les totaux
	        Map<String, Double> totals = calcService.calculateBasketTotals(basket);
	        Double subtotal = (Double) totals.getOrDefault("subtotal", 0.0);
	        Double tax = (Double) totals.getOrDefault("tax", 0.0);
	        Double total = (Double) totals.getOrDefault("total", 0.0);
	        
	        // ✅ Récupérer le coût de livraison
	        Double shippingCost = 0.0;
	        String carrierName = "";
	        String serviceName = "";
	        
	        if (basket.getCarrierServiceId() != null) {
	            try {
	                var carrierServiceDto = carrierServiceService.getServiceDtoById(basket.getCarrierServiceId());
	                if (carrierServiceDto.isPresent()) {
	                    shippingCost = carrierServiceDto.get().getCost();
	                    serviceName = carrierServiceDto.get().getName();
	                    
	                    // Récupérer le nom du transporteur
	                    var carrier = carrierServiceDto.get(); 
	                    if (carrier != null) {
	                        carrierName = serviceName.split("-", -1)[0].trim(); // Exemple: "Standard-48H" -> "Standard"
	                    }
	                }
	            } catch (Exception e) {
	                log.warn("Impossible de récupérer le coût de livraison: {}", e.getMessage());
	            }
	        }
	        
	        // Récupérer les méthodes de paiement activées
	        List<PaymentMethodDto> paymentMethods = paymentMethodService.getEnabledPaymentMethods();
	        
	        if (paymentMethods.isEmpty()) {
	            log.warn("Aucune méthode de paiement disponible");
	            model.addAttribute("error", "Aucune méthode de paiement disponible");
	            return "checkout-payment";
	        }
	        
	        // Ajouter au modèle
	        model.addAttribute("basket", basket);
	        model.addAttribute("shippingAddress", shippingAddress);
	        model.addAttribute("billingAddress", billingAddress);
	        model.addAttribute("warehouse", warehouse);
	        model.addAttribute("items", basketItems);
	        model.addAttribute("paymentMethods", paymentMethods);
	        model.addAttribute("subtotal", subtotal);
	        model.addAttribute("tax", tax);
	        model.addAttribute("shippingCost", shippingCost);  
	        model.addAttribute("carrierName", carrierName);    
	        model.addAttribute("serviceName", serviceName);     
	        model.addAttribute("total", total);
	        model.addAttribute("selectedPaymentMethodId", basket.getPaymentMethodId());
	        
	        log.info("Etape 3 - Paiement affichée | Utilisateur: {} | Total: {} EUR | Livraison: {} EUR", 
	            user.getEmail(), total, shippingCost);
	        
	        return "checkout-payment";
	        
	    } catch (Exception e) {
	        log.error("Erreur etape 3 - Paiement: {}", e.getMessage());
	        return "redirect:/checkout/addresses";
	    }
	}

	/**
	 * POST - Confirmer le paiement (pour COD)
	 */
	@PostMapping("/payment/confirm")
	public String confirmPayment(
	        @RequestParam Long paymentMethodId,
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
	        
	        // Valider les données du panier
	        if (basket.getShippingAddressId() == null || 
	            basket.getBillingAddressId() == null ||
	            basket.getCarrierServiceId() == null ||
	            basket.getWarehouseId() == null) {
	            log.warn("Confirmation paiement échouée - Données manquantes");
	            redirectAttributes.addFlashAttribute("error", "Données incomplètes");
	            return "redirect:/checkout/addresses";
	        }
	        
	        // Valider la méthode de paiement
	        PaymentMethodDto paymentMethod = paymentMethodService.getPaymentMethodById(paymentMethodId)
	            .orElseThrow(() -> new RuntimeException("Méthode de paiement invalide"));
	        
	        if (!paymentMethod.getEnabled()) {
	            log.warn("Méthode de paiement désactivée: {}", paymentMethod.getType());
	            redirectAttributes.addFlashAttribute("error", "Méthode de paiement non disponible");
	            return "redirect:/checkout/payment";
	        }
	        
	        // Récupérer les articles et totaux
	        List<ProductLineItem> basketItems = lineItemService.getLineItemsByBasketId(basket.getId());
	        Map<String, Double> totals = calcService.calculateBasketTotals(basket);
	        Double total = (Double) totals.getOrDefault("total", 0.0);
	        
	        if (basketItems.isEmpty()) {
	            log.warn("Tentative de paiement avec panier vide");
	            redirectAttributes.addFlashAttribute("error", "Votre panier est vide");
	            return "redirect:/basket";
	        }
	        
	        // ✅ ÉTAPE 1 : VÉRIFIER LE STOCK AVANT DE CRÉER LA COMMANDE
	        Warehouse warehouse = warehouseService.getWarehouseById(basket.getWarehouseId())
	            .orElseThrow(() -> new RuntimeException("Entrepôt non trouvé"));
	        
	        boolean allStockAvailable = true;
	        StringBuilder outOfStockMessage = new StringBuilder();
	        
	        for (ProductLineItem item : basketItems) {
	            boolean hasStock = productStockService.hasStockInWarehouse(
	                item.getProductId(), 
	                warehouse.getId(), 
	                item.getQuantity()
	            );
	            
	            if (!hasStock) {
	                allStockAvailable = false;
	                outOfStockMessage.append(item.getProduct().getName()).append(" (quantité: ").append(item.getQuantity()).append("), ");
	            }
	        }
	        
	        // Si le stock n'est pas disponible, RETOUR sans créer la commande
	        if (!allStockAvailable) {
	            String message = "Stock insuffisant pour: " + outOfStockMessage.toString();
	            message = message.replaceAll(", $", "");
	            
	            log.warn("Stock insuffisant - Commande NON créée: {}", message);
	            redirectAttributes.addFlashAttribute("error", message);
	            return "redirect:/checkout/shipping";  // Retour à l'étape précédente
	        }
	        
	        log.info("Stock vérifié avec succes pour {} article(s)", basketItems.size());
	        
	        // ÉTAPE 2 : CRÉER LA COMMANDE (maintenant que le stock est garanti)
	        Long orderId = null;
	        try {
	            orderId = orderService.createOrderFromBasket(basket.getId()).getId();
	            log.info("Commande créée: {} | Utilisateur: {}", orderId, user.getEmail());
	        } catch (Exception e) {
	            log.error("Erreur création commande: {}", e.getMessage());
	            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création de la commande");
	            return "redirect:/checkout/payment";
	        }
	        
	        // ÉTAPE 3 : CRÉER LA TRANSACTION DE PAIEMENT
	        TransactionDto transaction = null;
	        try {
	            transaction = paymentService.initiateCODPayment(orderId, total);
	            log.info("Paiement COD initialisé | Commande: {} | Transaction: {} | Montant: {} EUR", 
	                orderId, transaction.getTransactionNumber(), total);
	        } catch (Exception e) {
	            log.error("Erreur paiement COD: {}", e.getMessage());
	            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'initiation du paiement");
	            return "redirect:/checkout/payment";
	        }
	        
	        // ÉTAPE 4 : MARQUER LE PANIER COMME COMPLÉTÉ
	        try {
	            basketService.completeBasket(basket.getId());
	            log.info("Panier marqué comme complété | ID: {}", basket.getId());
	        } catch (Exception e) {
	            log.warn("Erreur complétion panier: {}", e.getMessage());
	        }
	        
	        // ÉTAPE 5 : CRÉER UN NOUVEAU PANIER POUR L'UTILISATEUR
	        try {
	            Basket newBasket = basketService.getOrCreateBasket(user.getId(), session.getId());
	            log.info("Nouveau panier créé | ID: {}", newBasket.getId());
	        } catch (Exception e) {
	            log.warn("Erreur création nouveau panier: {}", e.getMessage());
	        }
	        
	        log.info("Paiement confirmé avec succes | Commande: {} | Transaction: {}", 
	            orderId, transaction.getTransactionNumber());
	        
	        redirectAttributes.addFlashAttribute("success", "Commande confirmée avec succes");
	        return "redirect:/checkout/confirmation/" + orderId;
	        
	    } catch (Exception e) {
	        log.error("Erreur confirmation paiement: {}", e.getMessage());
	        redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
	        return "redirect:/checkout/payment";
	    }
	}

	/**
	 * Afficher la page de confirmation
	 */
	@GetMapping("/confirmation/{orderId}")
	public String showConfirmation(
	        @PathVariable Long orderId,
	        Authentication auth,
	        Model model) {
	    
	    if (auth == null || !auth.isAuthenticated()) {
	        return "redirect:/login";
	    }
	    
	    try {
	        User user = userService.findByEmail(auth.getName())
	            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
	        
	        // Récupérer la commande
	        // TODO: Implémenter OrderService.getOrderById()
	        // Order order = orderService.getOrderById(orderId)
	        //     .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
	        
	        // Récupérer la transaction
	        TransactionDto transaction = transactionService.getTransactionByOrderId(orderId)
	            .orElseThrow(() -> new RuntimeException("Transaction non trouvée"));
	        
	        model.addAttribute("orderId", orderId);
	        model.addAttribute("transaction", transaction);
	        
	        log.info("Confirmation affichée | Commande: {} | Utilisateur: {}", orderId, user.getEmail());
	        
	        return "checkout-confirmation";
	        
	    } catch (Exception e) {
	        log.error("Erreur affichage confirmation: {}", e.getMessage());
	        return "redirect:/";
	    }
	}
}
