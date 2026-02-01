package com.example.frontoffice.controller;

import com.example.product.model.Product;
import com.example.product.service.ProductService;
import com.example.shopping.model.Basket;
import com.example.shopping.model.ProductLineItem;
import com.example.shopping.service.BasketService;
import com.example.shopping.service.ProductLineItemService;
import com.example.user.model.User;
import com.example.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/basket")
public class BasketController {
    
    private final BasketService basketService;
    private final ProductLineItemService lineItemService;
    private final ProductService productService;
    private final UserService userService;
    
    public BasketController(BasketService basketService,
                          ProductLineItemService lineItemService,
                          ProductService productService,
                          UserService userService) {
        this.basketService = basketService;
        this.lineItemService = lineItemService;
        this.productService = productService;
        this.userService = userService;
    }
    
    // Afficher le panier
    @GetMapping
    public String viewBasket(Authentication authentication, HttpSession session, Model model) {
        Basket basket = getOrCreateBasket(authentication, session);
        List<ProductLineItem> items = lineItemService.getLineItemsByBasketId(basket.getId());
        Double total = lineItemService.calculateBasketTotal(basket.getId());
        
        model.addAttribute("basket", basket);
        model.addAttribute("items", items);
        model.addAttribute("total", total);
        
        return "basket";
    }
    
    // Ajouter au panier
    @PostMapping("/add")
    public String addToBasket(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") Integer quantity,
                            Authentication authentication,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        Basket basket = getOrCreateBasket(authentication, session);
        lineItemService.addOrUpdateLineItem(basket.getId(), productId, quantity, product.getPrice());
        
        redirectAttributes.addFlashAttribute("message", "Produit ajouté au panier !");
        return "redirect:/basket";
    }
    
    // Mettre à jour la quantité
    @PostMapping("/update/{lineItemId}")
    public String updateQuantity(@PathVariable Long lineItemId,
                                @RequestParam Integer quantity,
                                RedirectAttributes redirectAttributes) {
        if (quantity <= 0) {
            lineItemService.deleteLineItem(lineItemId);
            redirectAttributes.addFlashAttribute("message", "Produit retiré du panier !");
        } else {
            lineItemService.updateQuantity(lineItemId, quantity);
            redirectAttributes.addFlashAttribute("message", "Quantité mise à jour !");
        }
        return "redirect:/basket";
    }
    
    // Supprimer un produit
    @GetMapping("/remove/{lineItemId}")
    public String removeFromBasket(@PathVariable Long lineItemId,
                                  RedirectAttributes redirectAttributes) {
        lineItemService.deleteLineItem(lineItemId);
        redirectAttributes.addFlashAttribute("message", "Produit retiré du panier !");
        return "redirect:/basket";
    }
    
    // Vider le panier
    @GetMapping("/clear")
    public String clearBasket(Authentication authentication,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Basket basket = getOrCreateBasket(authentication, session);
        lineItemService.clearBasket(basket.getId());
        redirectAttributes.addFlashAttribute("message", "Panier vidé !");
        return "redirect:/basket";
    }
    
    // Méthode utilitaire pour obtenir ou créer un panier
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
    
    // API pour obtenir le nombre d'articles dans le panier (pour la navbar)
    @GetMapping("/count")
    @ResponseBody
    public int getBasketCount(Authentication authentication, HttpSession session) {
        Basket basket = getOrCreateBasket(authentication, session);
        return lineItemService.getBasketItemCount(basket.getId());
    }
}