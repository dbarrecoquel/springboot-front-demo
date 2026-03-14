package com.example.frontoffice.config;

import com.example.catalog.model.Category;
import com.example.catalog.service.CategoryService;
import com.example.shopping.model.Basket;
import com.example.shopping.service.BasketService;
import com.example.shopping.service.ProductLineItemService;
import com.example.user.model.User;
import com.example.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final CategoryService categoryService;
    private final BasketService basketService;
    private final ProductLineItemService productLineItemService;
    private final UserService userService;

    public GlobalControllerAdvice(CategoryService categoryService,
                                 BasketService basketService,
                                 ProductLineItemService productLineItemService,
                                 UserService userService) {
        this.categoryService = categoryService;
        this.basketService = basketService;
        this.productLineItemService = productLineItemService;
        this.userService = userService;
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model, Authentication authentication, HttpSession session) {
        // Ajouter les catégories racines
        List<Category> rootCategories = categoryService.getRootCategories();
        model.addAttribute("rootCategories", rootCategories);

        // Ajouter le nombre d'articles dans le panier
        int basketItemCount = getBasketItemCount(authentication, session);
        model.addAttribute("basketItemCount", basketItemCount);
    }

    private int getBasketItemCount(Authentication authentication, HttpSession session) {
        try {
            Long userId = null;
            String sessionId = session.getId();

            // Récupérer l'userId si l'utilisateur est authentifié
            if (authentication != null && authentication.isAuthenticated()) {
                User user = userService.findByEmail(authentication.getName()).orElse(null);
                if (user != null) {
                    userId = user.getId();
                }
            }

            // Obtenir ou créer le panier
            Basket basket = basketService.getOrCreateBasket(userId, sessionId);

            // Retourner le nombre d'articles
            return productLineItemService.getBasketItemCount(basket.getId());
        } catch (Exception e) {
            // En cas d'erreur, retourner 0
            return 0;
        }
    }
}