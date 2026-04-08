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
import com.example.shippingmethod.model.ShippingMethod;
import com.example.shippingmethod.service.ShippingMethodService;
import com.example.shopping.model.Basket;
import com.example.shopping.model.ProductLineItem;
import com.example.shopping.service.BasketCalculationService;
import com.example.shopping.service.BasketService;
import com.example.shopping.service.ProductLineItemService;
import com.example.user.model.User;
import com.example.user.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {
	
	private static final Logger logger = LoggerFactory.getLogger(CheckoutController.class);
	
	private final BasketService basketService;
    private final AddressService addressService;
    private final ProductLineItemService lineItemService;
    private final UserService userService;
    private final ShippingMethodService shippingMethodService;
    private final BasketCalculationService calcService ;
	public CheckoutController(BasketService basketService, AddressService addressService, 
								ProductLineItemService lineItemService,
								UserService userService,
								ShippingMethodService shippingMethodService,
								BasketCalculationService calcService) {
		
		this.basketService = basketService;
		this.addressService = addressService;
		this.lineItemService = lineItemService;
		this.userService = userService;
		this.shippingMethodService = shippingMethodService;
		this.calcService = calcService;
		
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
	
	@PostMapping("/addresses")
	public String setCheckoutAddresses(@RequestParam Long shippingAddressId, @RequestParam Long billingAddressId,
										Authentication authentication,
										HttpSession session,
										RedirectAttributes redirectAttributes) {
		
		if (authentication == null || !authentication.isAuthenticated())
			return "redirect:/login";
		
		User user = userService.findByEmail(authentication.getName()).orElseThrow(() -> new RuntimeException("User nnot found"));
		
		Basket basket = basketService.getOrCreateBasket(user.getId(), session.getId());
		
		Address shippingAddress = addressService.getAddressById(shippingAddressId).orElseThrow(() -> new RuntimeException("Address not found"));
		
		Address billingAddress = addressService.getAddressById(billingAddressId).orElseThrow(() -> new RuntimeException("Address not found"));
		
		basketService.setCheckoutAddresses(basket.getId(), billingAddress.getId(), shippingAddress.getId());
		
		
		
		return "redirect:/checkout/shipping";
		
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
	
	@GetMapping("/shipping")
	public String viewShippingMethods(Authentication auth, HttpSession session,Model model) {
		
		if (auth == null || !auth.isAuthenticated())
			return "redirect:/login";
		
		User user = userService.findByEmail(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
		
		Basket basket = basketService.getOrCreateBasket(user.getId(), session.getId());
		
		if (basket.getShippingAddressId() == null || basket.getBillingAddressId() == null)
			return "redirect:/checkout/addresses";
		
		Address shippingAddress = addressService.getAddressById(basket.getShippingAddressId()).orElseThrow(()-> new RuntimeException("No shipping address"));
		
		List<ShippingMethod> availableShippingMethod = shippingMethodService.getAvailaShippingMethods(shippingAddress.getCountry());
		
		List<ProductLineItem> lineItems = lineItemService.getLineItemsByBasketId(basket.getId());
		
		Map<String, Double> totals = calcService.calculateBasketTotals(basket);
		
		model.addAttribute("basket", basket);
	    model.addAttribute("items", lineItems);
	    model.addAttribute("shippingMethods", availableShippingMethod);
	    model.addAttribute("shippingAddress", shippingAddress);
	    model.addAttribute("subtotal", totals.get("subtotal"));
	    model.addAttribute("shippingCost", totals.get("shipping"));
	    model.addAttribute("total", totals.get("total"));
	    model.addAttribute("selectedShippingMethodId", basket.getShippingMethodId());
	    
	    return "checkout-shipping";
		
	}
	
	@PostMapping("/shipping")
	public String setShippingMethod(@RequestParam Long id,
									Authentication auth,
									HttpSession session,
									RedirectAttributes redirectAttributes) {
		
		if (auth == null || !auth.isAuthenticated())
			return "redirect:/login";
		
		User user = userService.findByEmail(auth.getName()).orElseThrow(()-> new RuntimeException("user not found"));
		
		Basket basket = basketService.getOrCreateBasket(user.getId(), session.getId());
		
		ShippingMethod shippingMethod = shippingMethodService.getShippingMethodById(id)
										.orElseThrow(() -> new RuntimeException());
		
		Address address = addressService.getAddressById(basket.getShippingAddressId())
							.orElseThrow(() -> new RuntimeException());
		
		if (!shippingMethod.isAvailableForCountry(address.getCountry())) {
			redirectAttributes.addFlashAttribute("error", "Cette méthode de livraison n'est pas disponible pour votre pays");
	        return "redirect:/checkout/shipping";
	    }
		basketService.setShippingMethod(basket.getId(), id);

	    redirectAttributes.addFlashAttribute("success", "Méthode de livraison sélectionnée");
	    return "redirect:/checkout/payment";
		
	}

}
