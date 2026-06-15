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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.shippingmethod.dto.WarehouseDto;
import com.example.shippingmethod.model.Warehouse;
import com.example.shippingmethod.service.WarehouseService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/warehouses")
@AllArgsConstructor
@Slf4j
public class WarehouseController {

	private final WarehouseService warehouseService;
	
	@GetMapping
	public String list(Model model) {
		List<WarehouseDto> list = warehouseService.getAllWarehouse();
		model.addAttribute("warehouses", list);
		return "warehouses/list";
	}
	@GetMapping("/create")
	public String create(Model model) {
		 model.addAttribute("warehouse", new Warehouse());
	     return "warehouses/form";
	}
	@PostMapping
	public String save(@ModelAttribute Warehouse warehouse, RedirectAttributes redirectAttributes) {
        try {
            warehouse.setUpdatedAt(LocalDateTime.now());
            warehouseService.saveWarehouse(warehouse);
            redirectAttributes.addFlashAttribute("success", "Entrepôt créé avec succès");
            return "redirect:/warehouses";
        } catch (Exception e) {
            log.error("Error saving warehouse", e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création");
            return "redirect:/warehouses/create";
        }
    }
    
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        WarehouseDto warehouse = warehouseService.getWarehouseById(id);
            
        model.addAttribute("warehouse", warehouse);
        return "warehouses/form";
    }
    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Warehouse warehouse, RedirectAttributes redirectAttributes) {
        try {
            warehouse.setId(id);
            warehouse.setUpdatedAt(LocalDateTime.now());
            warehouseService.saveWarehouse(warehouse);
            redirectAttributes.addFlashAttribute("success", "Entrepôt mis à jour avec succès");
            return "redirect:/warehouses";
        } catch (Exception e) {
            log.error("Error updating warehouse", e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour");
            return "redirect:/warehouses/{id}/edit";
        }
    }
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            warehouseService.deleteWarehouse(id);
            redirectAttributes.addFlashAttribute("success", "Entrepôt supprimé avec succès");
        } catch (Exception e) {
            log.error("Error deleting warehouse", e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression");
        }
        return "redirect:/warehouses";
    }
}
