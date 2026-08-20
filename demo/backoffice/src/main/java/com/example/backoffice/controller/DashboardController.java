package com.example.backoffice.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    
    /**
     * Accueil du backoffice
     */
    @GetMapping("/")
    public String index(Authentication authentication) {
        
        // Si pas connecté => rediriger vers login
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        // Si connecté => afficher le dashboard
        return "dashboard";
    }
    
    /**
     * Accueil alternatif
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        
        // Si pas connecté => rediriger vers login
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        return "dashboard";
    }
}