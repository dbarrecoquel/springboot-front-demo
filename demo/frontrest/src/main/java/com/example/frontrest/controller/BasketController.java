package com.example.frontrest.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.events.model.AddToBasketEvent;
import com.example.events.model.BasketViewEvent;
import com.example.events.producer.AddToBasketEventProducer;
import com.example.events.producer.BasketEventProducer;
import com.example.frontrest.models.AddToBasketRequest;
import com.example.frontrest.models.BasketResponse;
import com.example.frontrest.models.MessageResponse;
import com.example.frontrest.models.UpdateQuantityRequest;
import com.example.product.model.Product;
import com.example.product.service.ProductService;
import com.example.shopping.model.Basket;
import com.example.shopping.model.ProductLineItem;
import com.example.shopping.service.BasketService;
import com.example.shopping.service.ProductLineItemService;
import com.example.user.model.User;
import com.example.user.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/basket")
public class BasketController {
    
    private final BasketService basketService;
    private final ProductLineItemService lineItemService;
    private final ProductService productService;
    private final UserService userService;

    private final BasketEventProducer basketEventProducer;
    private final AddToBasketEventProducer addToBasketEventProducer;
    public BasketController(BasketService basketService,
                          ProductLineItemService lineItemService,
                          ProductService productService,
                          UserService userService,
                          Optional<BasketEventProducer> basketEventProducer,
                          Optional<AddToBasketEventProducer> addToBasketEventProducer) {
        this.basketService = basketService;
        this.lineItemService = lineItemService;
        this.productService = productService;
        this.userService = userService;
        this.basketEventProducer = basketEventProducer.orElse(null);
        this.addToBasketEventProducer = addToBasketEventProducer.orElse(null);
    }
    
    /* ===================== VIEW BASKET ===================== */
    
    @GetMapping
    public ResponseEntity<BasketResponse> viewBasket(Authentication authentication, HttpSession session) {
        Basket basket = getOrCreateBasket(authentication, session);
        List<ProductLineItem> items = lineItemService.getLineItemsByBasketId(basket.getId());
        Double total = lineItemService.calculateBasketTotal(basket.getId());
        
        if (basketEventProducer != null)
        	this.sendBasketViewEvent(basket,session);
        BasketResponse response = new BasketResponse(basket, items, total);
        return ResponseEntity.ok(response);
    }
    
    /* ===================== ADD TO BASKET ===================== */
    
    @PostMapping("/add")
    public ResponseEntity<MessageResponse> addToBasket(@Valid @RequestBody AddToBasketRequest request,
                                                      Authentication authentication,
                                                      HttpSession session) {
        Product product = productService.getProductById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + request.getProductId()));
        
        Basket basket = getOrCreateBasket(authentication, session);
        lineItemService.addOrUpdateLineItem(
            basket.getId(), 
            request.getProductId(), 
            request.getQuantity(), 
            product.getPrice()
        );
        if (addToBasketEventProducer != null)
        	this.sendAddToBasketEvent(basket,request);
        return ResponseEntity.ok(new MessageResponse("Product added to basket successfully"));
    }
    
    /* ===================== UPDATE QUANTITY ===================== */
    
    @PostMapping("/update/{lineItemId}")
    public ResponseEntity<MessageResponse> updateQuantity(@PathVariable Long lineItemId,
                                                         @Valid @RequestBody UpdateQuantityRequest request) {
        if (request.getQuantity() <= 0) {
            lineItemService.deleteLineItem(lineItemId);
            return ResponseEntity.ok(new MessageResponse("Product removed from basket"));
        } else {
            lineItemService.updateQuantity(lineItemId, request.getQuantity());
            return ResponseEntity.ok(new MessageResponse("Quantity updated successfully"));
        }
    }
    
    /* ===================== REMOVE FROM BASKET ===================== */
    
    @DeleteMapping("/remove/{lineItemId}")
    public ResponseEntity<MessageResponse> removeFromBasket(@PathVariable Long lineItemId) {
        lineItemService.deleteLineItem(lineItemId);
        return ResponseEntity.ok(new MessageResponse("Product removed from basket"));
    }
    
    /* ===================== CLEAR BASKET ===================== */
    
    @DeleteMapping("/clear")
    public ResponseEntity<MessageResponse> clearBasket(Authentication authentication, HttpSession session) {
        Basket basket = getOrCreateBasket(authentication, session);
        lineItemService.clearBasket(basket.getId());
        return ResponseEntity.ok(new MessageResponse("Basket cleared successfully"));
    }
    
    /* ===================== GET BASKET COUNT ===================== */
    
    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> getBasketCount(Authentication authentication, HttpSession session) {
        Basket basket = getOrCreateBasket(authentication, session);
        int count = lineItemService.getBasketItemCount(basket.getId());
        
        Map<String, Integer> response = new HashMap<>();
        response.put("count", count);
        
        return ResponseEntity.ok(response);
    }
    
    /* ===================== UTILITY METHOD ===================== */
    
    private Basket getOrCreateBasket(Authentication authentication, HttpSession session) {
        Long userId = null;
        String sessionId = session.getId();
        
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userService.findByEmail(authentication.getName()).orElse(null);
            if (user != null) {
                userId = user.getId();
            }
        }
        
        return basketService.getOrCreateBasket(userId, sessionId);
    }
    
    private void sendBasketViewEvent(Basket basket,
            HttpSession session) {
    	
		try {
			BasketViewEvent event = new BasketViewEvent();
			event.setBasketId(basket.getId());
			event.setUserId(basket.getUserId());
			event.setSessionId(session.getId());
			event.setCreatedAt(basket.getCreatedAt());
			event.setUpdatedAt(basket.getUpdatedAt());
			// Envoyer l'événement à Kafka
			basketEventProducer.sendBasketViewEvent(event);
			
		} 
		catch (Exception e) {
			// Log l'erreur mais ne pas bloquer la requête
			System.err.println("Error sending ProductViewEvent: " + e.getMessage());
		}
    }
    private void sendAddToBasketEvent(Basket basket,
    		AddToBasketRequest request) {
    	
		try {
			AddToBasketEvent event = new AddToBasketEvent(request.getProductId(),request.getQuantity(),basket.getId());
			// Envoyer l'événement à Kafka
			addToBasketEventProducer.sendAddToBasketEvent(event);
			
		} 
		catch (Exception e) {
			// Log l'erreur mais ne pas bloquer la requête
			System.err.println("Error sending ProductViewEvent: " + e.getMessage());
		}
    }
}