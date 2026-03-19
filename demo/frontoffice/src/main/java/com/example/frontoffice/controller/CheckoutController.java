package com.example.frontoffice.controller;

import java.util.List;

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
import com.example.shopping.model.Basket;
import com.example.shopping.model.ProductLineItem;
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
	public CheckoutController(BasketService basketService, AddressService addressService, 
								ProductLineItemService lineItemService,
								UserService userService) {
		
		this.basketService = basketService;
		this.addressService = addressService;
		this.lineItemService = lineItemService;
		this.userService = userService;
		
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
	public String setCheckoutAddresses(@RequestParam Long shippingId, @RequestParam Long billingId,
										Authentication authentication,
										HttpSession session,
										RedirectAttributes redirectAttributes) {
		
		if (authentication == null || !authentication.isAuthenticated())
			return "redirect:/login";
		
		User user = userService.findByEmail(authentication.getName()).orElseThrow(() -> new RuntimeException("User nnot found"));
		
		Basket basket = basketService.getOrCreateBasket(user.getId(), session.getId());
		
		Address shippingAddress = addressService.getAddressById(shippingId).orElseThrow(() -> new RuntimeException("Address not found"));
		
		Address billingAddress = addressService.getAddressById(billingId).orElseThrow(() -> new RuntimeException("Address not found"));
		
		basketService.setCheckoutAddresses(basket.getId(), billingId, shippingId);
		
		addressService.getAddressById(shippingId).orElseThrow(() -> new RuntimeException("Address not found"));
		
		
		
		return "checkout-shipping-method";
		
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
	

}
