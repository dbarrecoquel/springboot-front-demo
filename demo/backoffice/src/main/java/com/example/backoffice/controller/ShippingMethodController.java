package com.example.backoffice.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.shippingmethod.model.ShippingMethod;
import com.example.shippingmethod.service.ShippingMethodService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/shipping-methods")
public class ShippingMethodController {

	private final ShippingMethodService shippingMethodService;
	
	public ShippingMethodController(ShippingMethodService shippingMethodService) {
		this.shippingMethodService = shippingMethodService;
	}
	
	@GetMapping
	public String getShippingMethods(Model model) {
		
		List<ShippingMethod> shippingMethods = shippingMethodService.getAllShippingMethods();
		
		model.addAttribute("shippingMethods", shippingMethods);
		
		return "shipping-methods";
		
	}
	@GetMapping("/new")
	public String showCreateForm(Model model) {
		model.addAttribute("shippingMethod", new ShippingMethod());
		model.addAttribute("isEdit", false);
		return "shipping-method-form";
	}
	@PostMapping
	public String createShippingMethod(@Valid @ModelAttribute ShippingMethod shippingMethod,
										BindingResult result,
										RedirectAttributes redirectAttributes,
										Model model) {
		
		if (result.hasErrors()) {
			model.addAttribute("isEdit",false);
			return "shipping-method-form";
		}
		shippingMethodService.saveShippingMethod(shippingMethod);
		redirectAttributes.addFlashAttribute("success","Methode de livraison créée");
		return "redirect:/shipping-methods";
		
	}
	
	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable Long id, Model model) {
		
		ShippingMethod method = shippingMethodService.getShippingMethodById(id)
				.orElseThrow(()-> new RuntimeException("ShippingMethod Not found"));
		
		model.addAttribute("shippingMethod", method);
		model.addAttribute("isEdit",true);
		
		return "shipping-method-form";
	}
	
	@PostMapping("/update/{id}")
    public String updateShippingMethod(@PathVariable Long id,
                                      @Valid @ModelAttribute ShippingMethod shippingMethod,
                                      BindingResult result,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "shipping-method-form";
        }
        
        shippingMethod.setId(id);
        shippingMethodService.saveShippingMethod(shippingMethod);
        redirectAttributes.addFlashAttribute("success", "Méthode de livraison mise à jour avec succès");
        
        return "redirect:/shipping-methods";
    }
    
    @GetMapping("/toggle/{id}")
    public String toggleEnabled(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        shippingMethodService.toggleEnabled(id);
        redirectAttributes.addFlashAttribute("success", "Statut modifié avec succès");
        
        return "redirect:/shipping-methods";
    }
    
    @GetMapping("/delete/{id}")
    public String deleteShippingMethod(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        shippingMethodService.deleteShippingMethod(id);
        redirectAttributes.addFlashAttribute("success", "Méthode de livraison supprimée avec succès");
        
        return "redirect:/shipping-methods";
	  }
}
