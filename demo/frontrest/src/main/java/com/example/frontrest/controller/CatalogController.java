package com.example.frontrest.controller;

import com.example.events.model.ProductViewEvent;
import com.example.events.producer.EventProducer;
import com.example.product.model.Product;
import com.example.product.service.ProductService;
import com.example.user.model.User;
import com.example.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@CrossOrigin(origins = "*")
public class CatalogController {
    
    private final ProductService productService;
    private final EventProducer productEventProducer;
    private final UserService userService;
    
    public CatalogController(ProductService productService,
    							EventProducer productEventProducer,
                                UserService userService) {
        this.productService = productService;
        this.productEventProducer = productEventProducer;
        this.userService = userService;
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("FrontRest API is running!");
    }
    
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id,
                                                  Authentication authentication,
                                                  HttpServletRequest request,
                                                  HttpSession session) {
        return productService.getProductById(id)
                .map(product -> {
                    // Créer et envoyer l'événement ProductView
                    sendProductViewEvent(product, authentication, request, session);
                    return ResponseEntity.ok(product);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    private void sendProductViewEvent(Product product, 
                                     Authentication authentication,
                                     HttpServletRequest request,
                                     HttpSession session) {
        try {
            ProductViewEvent event = new ProductViewEvent(
                product.getId(),
                product.getName(),
                product.getSku(),
                getUserId(authentication),
                session.getId(),
                getUserEmail(authentication)
            );
            
            // Ajouter les informations de la requête
            event.setIpAddress(getClientIp(request));
            event.setUserAgent(request.getHeader("User-Agent"));
            
            // Envoyer l'événement à Kafka
            productEventProducer.sendProductViewEvent(event);
            
        } catch (Exception e) {
            // Log l'erreur mais ne pas bloquer la requête
            System.err.println("Error sending ProductViewEvent: " + e.getMessage());
        }
    }
    
    private Long getUserId(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return userService.findByEmail(authentication.getName())
                    .map(User::getId)
                    .orElse(null);
        }
        return null;
    }
    
    private String getUserEmail(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}