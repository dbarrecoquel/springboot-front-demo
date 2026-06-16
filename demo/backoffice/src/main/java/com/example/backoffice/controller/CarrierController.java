package com.example.backoffice.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.shippingmethod.dto.CarrierDto;
import com.example.shippingmethod.dto.WarehouseDto;
import com.example.shippingmethod.model.Carrier;
import com.example.shippingmethod.model.Warehouse;
import com.example.shippingmethod.service.CarrierService;
import com.example.shippingmethod.service.WarehouseService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/carriers")
@AllArgsConstructor
@Slf4j
public class CarrierController {
	private final CarrierService carrierService;
	
	@GetMapping
	public String getAllCarrier(Model model){
		List<CarrierDto> carriers = carrierService.getAllCarrier();
		model.addAttribute("carriers", carriers);
		return "carriers/list";
	}
	@GetMapping("/create")
	public String create(Model model) {
		model.addAttribute("carrier", new Carrier());
		model.addAttribute("isEdit",false);
		return "carriers/form";
	}
	@PostMapping
	public String save(@ModelAttribute Carrier carrier, RedirectAttributes redirectAttributes) {
		 try {
	            carrier.setUpdatedAt(LocalDateTime.now());
	            carrierService.saveCarrier(carrier);
	            redirectAttributes.addFlashAttribute("success", "Transporteur créé avec succès");
	            return "redirect:/carriers";
	        } catch (Exception e) {
	            log.error("Error saving warehouse", e);
	            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création");
	            return "redirect:/carriers/create";
	        }
	}
	 @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        CarrierDto carrier = carrierService.getCarrierById(id);
        model.addAttribute("isEdit", true);
        model.addAttribute("carrier", carrier);
        return "carriers/form";
    }
    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Carrier carrier, RedirectAttributes redirectAttributes) {
        try {
            carrier.setId(id);
            carrier.setUpdatedAt(LocalDateTime.now());
            carrierService.updateCarrier(id,carrier);
            redirectAttributes.addFlashAttribute("success", "Transporteur mis à jour avec succès");
            return "redirect:/carriers";
        } catch (Exception e) {
            log.error("Error updating warehouse", e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour");
            return "redirect:/carriers/{id}/edit";
        }
    }
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            carrierService.deleteCarrierById(id);
            redirectAttributes.addFlashAttribute("success", "Transporteur supprimé avec succès");
        } catch (Exception e) {
            log.error("Error deleting warehouse", e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression");
        }
        return "redirect:/carriers";
    }
    @PostMapping("/{id}/toggle")
    @ResponseBody
    public Carrier toggleStatus(@PathVariable Long id) {
        return carrierService.toggleCarrierStatus(id);
    }
}
