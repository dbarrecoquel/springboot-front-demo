package com.example.backoffice.controller;

import com.example.payment.dto.PaymentMethodDto;
import com.example.payment.enums.PaymentMethodType;
import com.example.payment.service.PaymentMethodService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/payment-methods")
@Slf4j
public class PaymentMethodController {
    
    private final PaymentMethodService paymentMethodService;
    
    public PaymentMethodController(PaymentMethodService paymentMethodService) {
        this.paymentMethodService = paymentMethodService;
    }
    
    /**
     * Afficher la liste des méthodes de paiement
     */
    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction sortDir,
            Model model) {
        
        try {
            Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sortDir, sortBy));
            
            List<PaymentMethodDto> paymentMethods = paymentMethodService.getAllPaymentMethods();
            
            model.addAttribute("paymentMethods", paymentMethods);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", pageSize);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            
            log.info("Affichage de {} méthodes de paiement", paymentMethods.size());
            
            return "payment-methods/list";
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des méthodes", e);
            return "redirect:/";
        }
    }
    
    /**
     * Afficher le formulaire de création
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        try {
            // Ajouter tous les types d'énums disponibles
            PaymentMethodType[] types = PaymentMethodType.values();
            
            model.addAttribute("paymentMethod", new PaymentMethodDto());
            model.addAttribute("paymentTypes", types);
            model.addAttribute("isEdit", false);
            
            log.info("Affichage du formulaire de création de méthode de paiement");
            
            return "payment-methods/form";
        } catch (Exception e) {
            log.error("Erreur lors de l'affichage du formulaire de création", e);
            return "redirect:/payment-methods";
        }
    }
    
    /**
     * Créer une nouvelle méthode de paiement
     */
    @PostMapping
    public String create(
            @RequestParam String type,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false, defaultValue = "bi-credit-card") String icon,
            @RequestParam(required = false, defaultValue = "true") Boolean enabled,
            @RequestParam(required = false, defaultValue = "0") Integer displayOrder,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Convertir le string en enum
            PaymentMethodType paymentType = PaymentMethodType.valueOf(type.toUpperCase());
            
            // Vérifier si la méthode existe déjà
            if (paymentMethodService.getPaymentMethodByType(paymentType).isPresent()) {
                log.warn("⚠️ Tentative de création d'une méthode {} déjà existante", type);
                redirectAttributes.addFlashAttribute("error", 
                    "La méthode de paiement " + type + " existe déjà");
                return "redirect:/payment-methods/create";
            }
            
            // Créer la méthode
            PaymentMethodDto paymentMethod = paymentMethodService.createOrUpdatePaymentMethod(
                paymentType,
                name,
                description,
                icon,
                enabled,
                displayOrder
            );
            
            log.info("Méthode de paiement créée: {} ({})", paymentType, name);
            redirectAttributes.addFlashAttribute("success", 
                "Méthode de paiement '" + name + "' créée avec succès");
            
            return "redirect:/payment-methods";
        } catch (IllegalArgumentException e) {
            log.error("Type de paiement invalide: {}", type);
            redirectAttributes.addFlashAttribute("error", 
                "Type de paiement invalide: " + type);
            return "redirect:/payment-methods/create";
        } catch (Exception e) {
            log.error("Erreur lors de la création de la méthode", e);
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la création: " + e.getMessage());
            return "redirect:/payment-methods/create";
        }
    }
    
    /**
     * Afficher le formulaire d'édition
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            PaymentMethodDto paymentMethod = paymentMethodService.getPaymentMethodById(id)
                .orElseThrow(() -> new RuntimeException("Méthode de paiement non trouvée"));
            
            PaymentMethodType[] types = PaymentMethodType.values();
            
            model.addAttribute("paymentMethod", paymentMethod);
            model.addAttribute("paymentTypes", types);
            model.addAttribute("isEdit", true);
            
            log.info("Affichage du formulaire d'édition pour la méthode {}", id);
            
            return "payment-methods/form";
        } catch (Exception e) {
            log.error("Erreur lors du chargement du formulaire d'édition", e);
            return "redirect:/payment-methods";
        }
    }
    
    /**
     * Mettre à jour une méthode de paiement
     */
    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @RequestParam String type,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false, defaultValue = "bi-credit-card") String icon,
            @RequestParam(required = false, defaultValue = "true") Boolean enabled,
            @RequestParam(required = false, defaultValue = "0") Integer displayOrder,
            RedirectAttributes redirectAttributes) {
        
        try {
            PaymentMethodDto paymentMethod = paymentMethodService.getPaymentMethodById(id)
                .orElseThrow(() -> new RuntimeException("Méthode de paiement non trouvée"));
            
            // Convertir le string en enum
            PaymentMethodType paymentType = PaymentMethodType.valueOf(type.toUpperCase());
            
            // Mettre à jour
            paymentMethodService.createOrUpdatePaymentMethod(
                paymentType,
                name,
                description,
                icon,
                enabled,
                displayOrder
            );
            
            log.info("Méthode de paiement {} mise à jour", id);
            redirectAttributes.addFlashAttribute("success", 
                "Méthode de paiement '" + name + "' mise à jour avec succès");
            
            return "redirect:/payment-methods";
        } catch (IllegalArgumentException e) {
            log.error("Type de paiement invalide: {}", type);
            redirectAttributes.addFlashAttribute("error", 
                "Type de paiement invalide: " + type);
            return "redirect:/payment-methods/{id}/edit";
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la méthode", e);
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la mise à jour: " + e.getMessage());
            return "redirect:/payment-methods/{id}/edit";
        }
    }
    
    /**
     * Activer/désactiver une méthode de paiement
     */
    @PostMapping("/{id}/toggle")
    public String toggle(
            @PathVariable Long id,
            @RequestParam Boolean enabled,
            RedirectAttributes redirectAttributes) {
        
        try {
            paymentMethodService.togglePaymentMethod(id, enabled);
            
            redirectAttributes.addFlashAttribute("success", 
                "Méthode de paiement " + (enabled ? "activée" : "désactivée"));
            
            log.info("Méthode {} {}", id, enabled ? "activée" : "désactivée");
            
            return "redirect:/payment-methods";
        } catch (Exception e) {
            log.error("Erreur lors du changement de statut", e);
            redirectAttributes.addFlashAttribute("error", 
                "Erreur: " + e.getMessage());
            return "redirect:/payment-methods";
        }
    }
    
    /**
     * Supprimer une méthode de paiement
     */
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            paymentMethodService.getPaymentMethodById(id)
                .orElseThrow(() -> new RuntimeException("Méthode non trouvée"));
            
            paymentMethodService.deletePaymentMethod(id);
            
            log.info("Méthode de paiement {} supprimée", id);
            redirectAttributes.addFlashAttribute("success", 
                "Méthode de paiement supprimée");
            
            return "redirect:/payment-methods";
        } catch (Exception e) {
            log.error("Erreur lors de la suppression", e);
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la suppression: " + e.getMessage());
            return "redirect:/payment-methods";
        }
    }
}