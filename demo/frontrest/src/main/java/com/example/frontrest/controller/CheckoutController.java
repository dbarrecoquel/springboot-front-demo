package com.example.frontrest.controller;

import org.springframework.security.core.Authentication;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.frontrest.models.BasketResponse;
import com.example.shopping.model.Basket;
import com.example.shopping.model.ProductLineItem;
import com.example.shopping.service.BasketService;
import com.example.shopping.service.ProductLineItemService;
import com.example.user.model.User;
import com.example.user.service.UserService;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {
	private final BasketService basketService;
	private final UserService userService;
	private final ProductLineItemService lineItemService;
	
	public CheckoutController(BasketService basketService,UserService userService, ProductLineItemService lineItemService) {
		
		this.basketService = basketService;
		this.userService = userService;
		this.lineItemService = lineItemService;
		
	}
	
	@GetMapping("/addresses")
	public ResponseEntity<BasketResponse> viewCheckoutAddresses(Authentication auth) {
		
		if (auth == null || !auth.isAuthenticated())
			return ResponseEntity.status(403).build();
		
		User user = userService.findByEmail(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
		
		Basket basket = basketService.getOrCreateBasket(user.getId(),null);
		
		List<ProductLineItem> plis = lineItemService.getLineItemsByBasketId(basket.getId());
		
		Double total = lineItemService.calculateBasketTotal(basket.getId());
		
		if (plis.isEmpty())
			return ResponseEntity.badRequest().build();
		
		BasketResponse response = new BasketResponse();
		
		response.setBasketId(basket.getId());
		response.setItemCount(plis.size());
		response.setItems(plis);
		response.setBillingAddressId(basket.getBillingAddressId());
		response.setShippingAddressId(basket.getShippingAddressId());
		
		
		return ResponseEntity.ok(response);
	}
	
}
