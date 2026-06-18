package com.example.backoffice.controller;

import com.example.shippingmethod.dto.CarrierDto;
import com.example.shippingmethod.dto.CarrierServiceDto;
import com.example.shippingmethod.dto.CarrierServiceListDto;
import com.example.shippingmethod.service.CarrierService;
import com.example.shippingmethod.service.CarrierServiceService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/carrier-services")
@Slf4j
@AllArgsConstructor
public class CarrierServicesController {
    
    private final CarrierServiceService carrierServiceService;
    private final CarrierService carrierService;
    
   
    
    /**
     * Afficher la liste des services
     */
    @GetMapping
    public String list(Model model) {
        try {
            List<CarrierServiceListDto> services = carrierServiceService.getAllServicesListDto();
            model.addAttribute("services", services);
            return "carrier-services/list";
        } catch (Exception e) {
            log.error("Erreur lors du chargement des services", e);
            model.addAttribute("error", "Erreur lors du chargement des services");
            return "carrier-services/list";
        }
    }
    
    /**
     * Afficher le formulaire de création
     */
    @GetMapping("/create")
    public String create(Model model) {
        try {
            List<CarrierDto> carriers = carrierService.getActiveCarriersDto();
            
            // Créer un nouveau DTO avec les valeurs par défaut
            CarrierServiceDto newService = CarrierServiceDto.builder()
                .enabled(true)
                .cost(0.0)
                .freeShippingMinAmount(50.0)
                .processingDays(1)
                .deliveryDays(2)
                .cutoffTime(java.time.LocalTime.of(14, 0))
                .build();
            
            model.addAttribute("service", newService);
            model.addAttribute("carriers", carriers);
            model.addAttribute("isEdit", false);
            
            log.info("Formulaire de création affichée");
            
            return "carrier-services/form";
        } catch (Exception e) {
            log.error("Erreur lors de l'affichage du formulaire de création", e);
            return "redirect:/carrier-services";
        }
    }
    
    /**
     * Enregistrer un nouveau service
     */
    @PostMapping
    public String save(@ModelAttribute CarrierServiceDto serviceDto, RedirectAttributes redirectAttributes) {
        try {
            log.info("Création d'un nouveau service: {}", serviceDto.getName());
            
            // S'assurer que enabled n'est pas null
            if (serviceDto.getEnabled() == null) {
                serviceDto.setEnabled(true);
            }
            
            carrierServiceService.createService(serviceDto);
            redirectAttributes.addFlashAttribute("success", "Service créé avec succès");
            return "redirect:/carrier-services";
        } catch (Exception e) {
            log.error("Erreur lors de la sauvegarde du service", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/carrier-services/create";
        }
    }
    
    /**
     * Afficher le formulaire d'édition
     */
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        try {
            CarrierServiceDto service = carrierServiceService.getServiceDtoById(id)
                .orElseThrow(() -> new RuntimeException("Service non trouvé"));
            
            List<CarrierDto> carriers = carrierService.getActiveCarriersDto();
            
            // S'assurer que enabled n'est pas null
            if (service.getEnabled() == null) {
                service.setEnabled(true);
            }
            
            model.addAttribute("service", service);
            model.addAttribute("carriers", carriers);
            model.addAttribute("isEdit", true);
            
            log.info("Formulaire d'édition affichée pour le service: {}", id);
            
            return "carrier-services/form";
        } catch (Exception e) {
            log.error("Erreur lors du chargement du service {}", id, e);
            return "redirect:/carrier-services";
        }
    }
    
    /**
     * Mettre à jour un service
     */
    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute CarrierServiceDto serviceDto, RedirectAttributes redirectAttributes) {
        try {
            log.info("Mise à jour du service: {}", id);
            
            // S'assurer que enabled n'est pas null
            if (serviceDto.getEnabled() == null) {
                serviceDto.setEnabled(true);
            }
            
            carrierServiceService.updateService(id, serviceDto);
            redirectAttributes.addFlashAttribute("success", "Service mis à jour avec succès");
            return "redirect:/carrier-services";
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du service {}", id, e);
            redirectAttributes.addFlashAttribute("error", "" + e.getMessage());
            return "redirect:/carrier-services/{id}/edit";
        }
    }
    
    /**
     * Supprimer un service
     */
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            log.info("Suppression du service: {}", id);
            carrierServiceService.deleteService(id);
            redirectAttributes.addFlashAttribute("success", "Service supprimé avec succès");
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du service {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression");
        }
        return "redirect:/carrier-services";
    }
    
    /**
     * Activer/désactiver un service (AJAX)
     */
    @PostMapping("/{id}/toggle")
    @ResponseBody
    public CarrierServiceDto toggleStatus(@PathVariable Long id) {
        return carrierServiceService.toggleServiceStatus(id);
    }
}