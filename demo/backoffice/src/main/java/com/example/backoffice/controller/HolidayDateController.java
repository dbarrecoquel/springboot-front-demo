package com.example.backoffice.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.shippingmethod.dto.HolidayDatesDto;
import com.example.shippingmethod.dto.HolidayDatesListDto;
import com.example.shippingmethod.dto.WarehouseDto;
import com.example.shippingmethod.service.HolidayDatesService;
import com.example.shippingmethod.service.WarehouseService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/holiday-dates")
@Slf4j
@AllArgsConstructor
public class HolidayDateController {

	private final HolidayDatesService holidayDatesService;
	private final WarehouseService warehouseService;
	@GetMapping
	public String list(Model model) {
		 try {
	            List<HolidayDatesListDto> dates = holidayDatesService.getAllHolidayDatesListDto();
	            model.addAttribute("holidays", dates);
	            return "holiday-dates/list";
	        } catch (Exception e) {
	            log.error("Erreur lors du chargement des jours fériés", e);
	            model.addAttribute("error", "Erreur lors du chargement des jours fériés");
	            return "holiday-dates/list";
	        }
	}
	 @GetMapping("/create")
    public String create(Model model) {
        try {
            List<WarehouseDto> warehouses = warehouseService.findByEnabledTrue();
            
            // Créer un nouveau DTO avec les valeurs par défaut
            HolidayDatesDto newdate = HolidayDatesDto.builder()
                .recurring(false)
                .warehouseId(null)
                .build();
            
            model.addAttribute("warehouses", warehouses);
            model.addAttribute("holiday", newdate);
            model.addAttribute("isEdit", false);
            
            log.info("Formulaire de création affichée");
            
            return "holiday-dates/form";
        } catch (Exception e) {
            log.error("Erreur lors de l'affichage du formulaire de création", e);
            return "redirect:/carrier-services";
        }
	 }
	 @PostMapping
     public String save(@ModelAttribute HolidayDatesDto holidayDto, RedirectAttributes redirectAttributes) {
        try {
            log.info("Création d'un nouveau jour férié: {}", holidayDto.getName());
           
            if (holidayDto.getRecurring() == null) {
            	holidayDto.setRecurring(true);
            }
            
            holidayDatesService.createHolidayDate(holidayDto);
            redirectAttributes.addFlashAttribute("success", " jour férié créé avec succès");
            return "redirect:/holiday-dates";
        } catch (Exception e) {
            log.error("Erreur lors de la sauvegarde du jours fériés", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/holiday-dates/create";
        }
     }
	 @GetMapping("/{id}/edit")
     public String edit(@PathVariable Long id, Model model) {
        try {
            HolidayDatesDto date = holidayDatesService.getHolidayDateById(id);
            List<WarehouseDto> warehouses = warehouseService.findByEnabledTrue();
            
            // S'assurer que enabled n'est pas null
            if (date.getRecurring() == null) {
                date.setRecurring(true);
            }
            
            model.addAttribute("holiday", date);
            model.addAttribute("warehouses", warehouses);
            model.addAttribute("isEdit", true);
            
            log.info("Formulaire d'édition affichée pour le jour férié: {}", id);
            
            return "holiday-dates/form";
        } catch (Exception e) {
            log.error("Erreur lors du chargement du jour férié {}", id, e);
            return "redirect:/holiday-dates";
        }
     }
	 @PostMapping("/{id}")
     public String update(@PathVariable Long id, @ModelAttribute HolidayDatesDto date, RedirectAttributes redirectAttributes) {
        try {
            log.info("Mise à jour du service: {}", id);
            date.setId(id);
            // S'assurer que enabled n'est pas null
            if (date.getRecurring() == null) {
                date.setRecurring(true);
            }
            
            holidayDatesService.updateHolidayDate(date);
            redirectAttributes.addFlashAttribute("success", "Jour férié mis à jour avec succès");
            return "redirect:/holiday-dates";
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du jour férié {}", id, e);
            redirectAttributes.addFlashAttribute("error", "" + e.getMessage());
            return "redirect:/holiday-dates/{id}/edit";
        }
	 }
   @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            log.info("Suppression du jour férié: {}", id);
            holidayDatesService.deleteHolidayDate(id);
            redirectAttributes.addFlashAttribute("success", "jour férié supprimé avec succès");
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du jour férié {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression");
        }
        return "redirect:/holiday-dates";
    }
	    
}
