package com.example.frontrest.controller;

import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.address.model.Address;
import com.example.address.service.AddressService;
import com.example.frontrest.models.BasketResponse;
import com.example.frontrest.models.CheckoutAddressesRequest;
import com.example.shippingmethod.dto.ShippingMethodDto;
import com.example.shippingmethod.mapper.ShippingMethodMapper;
import com.example.shippingmethod.service.ShippingMethodService;
import com.example.shopping.model.Basket;
import com.example.shopping.model.ProductLineItem;
import com.example.shopping.service.BasketService;
import com.example.shopping.service.ProductLineItemService;
import com.example.user.model.User;
import com.example.user.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {
	private final BasketService basketService;
	private final UserService userService;
	private final ProductLineItemService lineItemService;
	private final ShippingMethodService shippingMethodService;
	private final AddressService addressService;
	private final ShippingMethodMapper shippMapper;
	
	public CheckoutController(BasketService basketService,UserService userService, 
			ProductLineItemService lineItemService,
			ShippingMethodService shippingMethodService,
			AddressService addressService,
			ShippingMethodMapper mapper) {
		
		this.basketService = basketService;
		this.userService = userService;
		this.lineItemService = lineItemService;
		this.shippingMethodService = shippingMethodService;
		this.addressService = addressService;
		this.shippMapper = mapper;
		
	}
	
	@GetMapping("/addresses")
	public ResponseEntity<BasketResponse> viewCheckoutAddresses(
	        Authentication auth,
	        @CookieValue(value = "guestId", required = false) String guestId) {

	    if (auth == null || !auth.isAuthenticated())
	        return ResponseEntity.status(403).build();

	    User user = userService.findByEmail(auth.getName())
	            .orElseThrow(() -> new RuntimeException("User not found"));

	    // Utiliser mergeGuestBasketToUser au lieu de getOrCreateBasket
	    Basket basket = (guestId != null)
	            ? basketService.mergeGuestBasketToUser(user.getId(), guestId)
	            : basketService.getOrCreateBasketForUser(user.getId());

	    List<ProductLineItem> plis = lineItemService.getLineItemsByBasketId(basket.getId());

	    if (plis.isEmpty())
	        return ResponseEntity.badRequest().build();

	    Double total = lineItemService.calculateBasketTotal(basket.getId());

	    BasketResponse response = new BasketResponse();
	    response.setBasketId(basket.getId());
	    response.setItemCount(plis.size());
	    response.setItems(plis);
	    response.setBillingAddressId(basket.getBillingAddressId());
	    response.setShippingAddressId(basket.getShippingAddressId());
	    response.setTotal(total);

	    return ResponseEntity.ok(response);
	}

	@PostMapping("/addresses")
	public ResponseEntity<Void> setCheckoutAddresses(
	        @Valid @RequestBody CheckoutAddressesRequest request,
	        Authentication auth,
	        @CookieValue(value = "guestId", required = false) String guestId) {

	    if (auth == null || !auth.isAuthenticated())
	        return ResponseEntity.status(403).build();

	    User user = userService.findByEmail(auth.getName())
	            .orElseThrow(() -> new RuntimeException("User not found"));

	    Basket basket = (guestId != null)
	            ? basketService.mergeGuestBasketToUser(user.getId(), guestId)
	            : basketService.getOrCreateBasketForUser(user.getId());

	    List<ProductLineItem> plis = lineItemService.getLineItemsByBasketId(basket.getId());

	    if (plis.isEmpty())
	        return ResponseEntity.badRequest().build();

	    Address addressShipping = addressService.getAddressById(request.getShippingAddressId())
	            .orElseThrow(() -> new RuntimeException("Cant get address"));
	    Address addressInvoice = addressService.getAddressById(request.getBillingAddressId())
	            .orElseThrow(() -> new RuntimeException("Cant get address"));

	    basket.setBillingAddressId(addressInvoice.getId());
	    basket.setShippingAddressId(addressShipping.getId());
	    basketService.save(basket);

	    return ResponseEntity.ok().build();
	}

	@GetMapping("/shipping-methods")
	public ResponseEntity<List<ShippingMethodDto>> viewCheckoutShippingMethods(
	        Authentication auth,
	        @CookieValue(value = "guestId", required = false) String guestId) {

	    if (auth == null || !auth.isAuthenticated())
	        return ResponseEntity.status(403).build();

	    User user = userService.findByEmail(auth.getName())
	            .orElseThrow(() -> new RuntimeException("User not found"));

	    Basket basket = (guestId != null)
	            ? basketService.mergeGuestBasketToUser(user.getId(), guestId)
	            : basketService.getOrCreateBasketForUser(user.getId());

	    List<ProductLineItem> plis = lineItemService.getLineItemsByBasketId(basket.getId());

	    if (plis.isEmpty())
	        return ResponseEntity.badRequest().build();

	    // ← OR au lieu de AND : si l'une OU l'autre est null, on bloque
	    if (basket.getShippingAddressId() == null || basket.getBillingAddressId() == null)
	        return ResponseEntity.status(400).body(null);

	    Address address = addressService.getAddressById(basket.getShippingAddressId())
	            .orElseThrow(() -> new RuntimeException("Cant get address"));

	    List<ShippingMethodDto> ship = shippingMethodService
	            .getAvailaShippingMethods(address.getCountry())
	            .stream()
	            .map(shippMapper::toDto)
	            .collect(Collectors.toList());

	    return ResponseEntity.ok(ship);
	}

	@PostMapping("/shipping-methods")
	public ResponseEntity<Void> setCheckoutShippingMethod(
	        @Valid @RequestBody Long shippingMethodId,
	        Authentication auth,
	        @CookieValue(value = "guestId", required = false) String guestId) {

	    if (auth == null || !auth.isAuthenticated())
	        return ResponseEntity.status(403).build();

	    User user = userService.findByEmail(auth.getName())
	            .orElseThrow(() -> new RuntimeException("User not found"));

	    Basket basket = (guestId != null)
	            ? basketService.mergeGuestBasketToUser(user.getId(), guestId)
	            : basketService.getOrCreateBasketForUser(user.getId());

	    List<ProductLineItem> plis = lineItemService.getLineItemsByBasketId(basket.getId());

	    if (plis.isEmpty())
	        return ResponseEntity.badRequest().build();

	    basket.setShippingMethodId(shippingMethodId);
	    basketService.save(basket);
	    return ResponseEntity.ok().build();
	}
}
