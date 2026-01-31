package com.example.frontoffice.controller;

import com.example.address.model.Address;
import com.example.address.service.AddressService;
import com.example.user.model.User;
import com.example.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    
    private final UserService userService;
    private final AddressService addressService;
    
    public ProfileController(UserService userService, AddressService addressService) {
        this.userService = userService;
        this.addressService = addressService;
    }
    
    @GetMapping
    public String showProfile(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Address> addresses = addressService.getAddressesByUserId(user.getId());
        
        model.addAttribute("user", user);
        model.addAttribute("addresses", addresses);
        return "profile";
    }
    
    @GetMapping("/addresses/new")
    public String showAddressForm(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
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
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        address.setUserId(user.getId());
        addressService.saveAddress(address);
        
        redirectAttributes.addFlashAttribute("message", "Adresse enregistrée avec succès !");
        return "redirect:/profile";
    }
    
    @GetMapping("/addresses/edit/{id}")
    public String showEditAddressForm(@PathVariable Long id, Model model) {
        Address address = addressService.getAddressById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        
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
        
        redirectAttributes.addFlashAttribute("message", "Adresse mise à jour avec succès !");
        return "redirect:/profile";
    }
    
    @GetMapping("/addresses/delete/{id}")
    public String deleteAddress(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        addressService.deleteAddress(id);
        redirectAttributes.addFlashAttribute("message", "Adresse supprimée avec succès !");
        return "redirect:/profile";
    }
}