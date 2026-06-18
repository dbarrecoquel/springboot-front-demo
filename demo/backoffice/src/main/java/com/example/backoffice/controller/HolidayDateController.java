package com.example.backoffice.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.shippingmethod.dto.CarrierDto;
import com.example.shippingmethod.dto.CarrierServiceDto;
import com.example.shippingmethod.dto.CarrierServiceListDto;
import com.example.shippingmethod.dto.HolidayDatesDto;
import com.example.shippingmethod.dto.HolidayDatesListDto;
import com.example.shippingmethod.dto.WarehouseDto;
import com.example.shippingmethod.service.CarrierService;
import com.example.shippingmethod.service.CarrierServiceService;
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
	            List<HolidayDatesListDto> services = holidayDatesService.getAllHolidayDatesListDto();
	            model.addAttribute("holidays", services);
	            return "holiday-dates/list";
	        } catch (Exception e) {
	            log.error("Erreur lors du chargement des services", e);
	            model.addAttribute("error", "Erreur lors du chargement des services");
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
	            log.error("Erreur lors de la sauvegarde du service", e);
	            redirectAttributes.addFlashAttribute("error", e.getMessage());
	            return "redirect:/holiday-dates/create";
	        }
	    }
}
