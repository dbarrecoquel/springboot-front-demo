package com.example.backoffice.controller;

import com.example.address.model.Address;
import com.example.address.service.AddressService;
import com.example.user.model.User;
import com.example.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserManagementController {
    
    private final UserService userService;
    private final AddressService addressService;
    private final PasswordEncoder passwordEncoder;
    
    public UserManagementController(UserService userService, 
                                   AddressService addressService,
                                   PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.addressService = addressService;
        this.passwordEncoder = passwordEncoder;
    }
    
    // Liste des utilisateurs
    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "users";
    }
    
    // Afficher le formulaire de création
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        return "user-form";
    }
    
    // Créer un utilisateur
    @PostMapping
    public String createUser(@Valid @ModelAttribute("user") User user,
                           @RequestParam(required = false) String password,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "user-form";
        }
        
        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ROLE_USER");
        }
        
        userService.save(user);
        redirectAttributes.addFlashAttribute("message", "Utilisateur créé avec succès !");
        return "redirect:/users";
    }
    
    // Afficher le formulaire d'édition
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID utilisateur invalide : " + id));
        model.addAttribute("user", user);
        return "user-form";
    }
    
    // Mettre à jour un utilisateur
    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable Long id,
                           @Valid @ModelAttribute("user") User user,
                           @RequestParam(required = false) String password,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            user.setId(id);
            return "user-form";
        }
        
        User existingUser = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID utilisateur invalide : " + id));
        
        existingUser.setEmail(user.getEmail());
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setPhone(user.getPhone());
        existingUser.setRole(user.getRole());
        existingUser.setEnabled(user.getEnabled());
        
        // Mettre à jour le mot de passe uniquement s'il est fourni
        if (password != null && !password.isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(password));
        }
        
        userService.save(existingUser);
        redirectAttributes.addFlashAttribute("message", "Utilisateur mis à jour avec succès !");
        return "redirect:/users";
    }
    
    // Voir les détails d'un utilisateur
    @GetMapping("/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID utilisateur invalide : " + id));
        List<Address> addresses = addressService.getAddressesByUserId(id);
        
        model.addAttribute("user", user);
        model.addAttribute("addresses", addresses);
        return "user-detail";
    }
    
    // Supprimer un utilisateur
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Supprimer d'abord toutes les adresses de l'utilisateur
        List<Address> addresses = addressService.getAddressesByUserId(id);
        for (Address address : addresses) {
            addressService.deleteAddress(address.getId());
        }
        
        userService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Utilisateur supprimé avec succès !");
        return "redirect:/users";
    }
    
    // Activer/Désactiver un utilisateur
    @GetMapping("/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID utilisateur invalide : " + id));
        
        user.setEnabled(!user.getEnabled());
        userService.save(user);
        
        String status = user.getEnabled() ? "activé" : "désactivé";
        redirectAttributes.addFlashAttribute("message", "Utilisateur " + status + " avec succès !");
        return "redirect:/users";
    }
}