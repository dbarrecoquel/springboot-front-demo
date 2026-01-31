package com.example.backoffice.controller;

import com.example.address.model.Address;
import com.example.address.service.AddressService;
import com.example.user.model.User;
import com.example.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users/{userId}/addresses")
public class UserAddressController {
    
    private final AddressService addressService;
    private final UserService userService;
    
    public UserAddressController(AddressService addressService, UserService userService) {
        this.addressService = addressService;
        this.userService = userService;
    }
    
    // Afficher le formulaire d'ajout d'adresse
    @GetMapping("/new")
    public String showAddressForm(@PathVariable Long userId, Model model) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("ID utilisateur invalide : " + userId));
        
        Address address = new Address();
        address.setUserId(userId);
        
        model.addAttribute("address", address);
        model.addAttribute("user", user);
        return "user-address-form";
    }
    
    // Créer une adresse pour l'utilisateur
    @PostMapping
    public String createAddress(@PathVariable Long userId,
                              @Valid @ModelAttribute("address") Address address,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("ID utilisateur invalide : " + userId));
            model.addAttribute("user", user);
            return "user-address-form";
        }
        
        address.setUserId(userId);
        addressService.saveAddress(address);
        
        redirectAttributes.addFlashAttribute("message", "Adresse ajoutée avec succès !");
        return "redirect:/users/" + userId;
    }
    
    // Afficher le formulaire d'édition d'adresse
    @GetMapping("/edit/{addressId}")
    public String showEditAddressForm(@PathVariable Long userId, 
                                     @PathVariable Long addressId, 
                                     Model model) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("ID utilisateur invalide : " + userId));
        Address address = addressService.getAddressById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("ID adresse invalide : " + addressId));
        
        model.addAttribute("address", address);
        model.addAttribute("user", user);
        return "user-address-form";
    }
    
    // Mettre à jour une adresse
    @PostMapping("/update/{addressId}")
    public String updateAddress(@PathVariable Long userId,
                              @PathVariable Long addressId,
                              @Valid @ModelAttribute("address") Address address,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("ID utilisateur invalide : " + userId));
            address.setId(addressId);
            model.addAttribute("user", user);
            return "user-address-form";
        }
        
        address.setId(addressId);
        address.setUserId(userId);
        addressService.saveAddress(address);
        
        redirectAttributes.addFlashAttribute("message", "Adresse mise à jour avec succès !");
        return "redirect:/users/" + userId;
    }
    
    // Supprimer une adresse
    @GetMapping("/delete/{addressId}")
    public String deleteAddress(@PathVariable Long userId,
                              @PathVariable Long addressId,
                              RedirectAttributes redirectAttributes) {
        addressService.deleteAddress(addressId);
        redirectAttributes.addFlashAttribute("message", "Adresse supprimée avec succès !");
        return "redirect:/users/" + userId;
    }
}