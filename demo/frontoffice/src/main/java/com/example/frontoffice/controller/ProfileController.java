package com.example.frontoffice.controller;

import com.example.address.model.Address;
import com.example.address.service.AddressService;
import com.example.order.dto.OrderDto;
import com.example.order.model.Order;
import com.example.order.model.OrderProductLineItem;
import com.example.order.service.OrderService;
import com.example.order.service.OrderProductLineItemService;
import com.example.payment.dto.TransactionDto;
import com.example.payment.service.TransactionService;
import com.example.user.model.User;
import com.example.user.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/profile")
@Slf4j
public class ProfileController {
    
    private final UserService userService;
    private final AddressService addressService;
    private final OrderService orderService;
    private final OrderProductLineItemService orderProductLineItemService;
    private final TransactionService transactionService;
    
    public ProfileController(UserService userService, 
                            AddressService addressService,
                            OrderService orderService,
                            OrderProductLineItemService orderProductLineItemService,
                            TransactionService transactionService) {
        this.userService = userService;
        this.addressService = addressService;
        this.orderService = orderService;
        this.orderProductLineItemService = orderProductLineItemService;
        this.transactionService = transactionService;
    }
    
    /**
     * Afficher le profil utilisateur
     */
    @GetMapping
    public String showProfile(Authentication authentication, Model model) {
        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            List<Address> addresses = addressService.getAddressesByUserId(user.getId());
            
            model.addAttribute("user", user);
            model.addAttribute("addresses", addresses);
            
            log.info("Profil affiche pour l'utilisateur: {}", user.getEmail());
            
            return "profile";
        } catch (Exception e) {
            log.error("Erreur affichage profil: {}", e.getMessage());
            return "redirect:/";
        }
    }
    
    /**
     * Afficher la liste des commandes de l'utilisateur
     */
    @GetMapping("/orders")
    public String showOrders(Authentication authentication, Model model) {
        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            List<Order> orders = orderService.getOrdersByUserId(user.getId());
            
            model.addAttribute("user", user);
            model.addAttribute("orders", orders);
            
            log.info("Liste des commandes affichée pour l'utilisateur: {} | {} commandes", 
                user.getEmail(), orders.size());
            
            return "orders-list";
        } catch (Exception e) {
            log.error("Erreur affichage commandes: {}", e.getMessage());
            return "redirect:/profile";
        }
    }
    
    /**
     * Afficher le détail d'une commande
     */
    @GetMapping("/orders/{orderId}")
    public String showOrderDetail(@PathVariable Long orderId, 
                                 Authentication authentication, 
                                 Model model) {
        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            OrderDto order = orderService.getOrderById(orderId)
                    .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
            
            // Vérifier que la commande appartient à l'utilisateur
            if (!order.getUserId().equals(user.getId())) {
                log.warn("Tentative d'accès non autorisée à la commande {} par l'utilisateur {}", 
                    orderId, user.getEmail());
                return "redirect:/profile/orders";
            }
            
            // Récupérer les articles de la commande
            List<OrderProductLineItem> orderItems = orderProductLineItemService.getOrderItemsByOrderId(orderId);
            
            // Récupérer la transaction de paiement
            Optional<TransactionDto> transaction = transactionService.getTransactionByOrderId(orderId);
            
            model.addAttribute("user", user);
            model.addAttribute("order", order);
            model.addAttribute("orderItems", orderItems);
            model.addAttribute("transaction", transaction.orElse(null));
            
            log.info("Detail commande affiche | Commande: {} | Utilisateur: {}", 
                orderId, user.getEmail());
            
            return "order-detail";
        } catch (Exception e) {
            log.error("Erreur affichage detail commande: {}", e.getMessage());
            return "redirect:/profile/orders";
        }
    }
    
    // ========================================
    // GESTION DES ADRESSES
    // ========================================
    
    @GetMapping("/addresses/new")
    public String showAddressForm(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        Address address = new Address();
        address.setUserId(user.getId());
        
        model.addAttribute("address", address);
        return "address-form";
    }
    
    @PostMapping("/addresses")
    public String saveAddress(@Valid @ModelAttribute("address") Address address,
                            BindingResult result,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "address-form";
        }
        
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        address.setUserId(user.getId());
        addressService.saveAddress(address);
        
        redirectAttributes.addFlashAttribute("message", "Adresse enregistrée avec succes !");
        return "redirect:/profile";
    }
    
    @GetMapping("/addresses/edit/{id}")
    public String showEditAddressForm(@PathVariable Long id, Model model) {
        Address address = addressService.getAddressById(id)
                .orElseThrow(() -> new RuntimeException("Adresse non trouvée"));
        
        model.addAttribute("address", address);
        return "address-form";
    }
    
    @PostMapping("/addresses/update/{id}")
    public String updateAddress(@PathVariable Long id,
                              @Valid @ModelAttribute("address") Address address,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            address.setId(id);
            return "address-form";
        }
        
        address.setId(id);
        addressService.saveAddress(address);
        
        redirectAttributes.addFlashAttribute("message", "Adresse mise a jour avec succes !");
        return "redirect:/profile";
    }
    
    @GetMapping("/addresses/delete/{id}")
    public String deleteAddress(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        addressService.deleteAddress(id);
        redirectAttributes.addFlashAttribute("message", "Adresse supprimee avec succes !");
        return "redirect:/profile";
    }
}