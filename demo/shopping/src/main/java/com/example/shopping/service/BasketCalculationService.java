package com.example.shopping.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.shippingmethod.model.ShippingMethod;
import com.example.shippingmethod.service.CarrierServiceService;
import com.example.shippingmethod.service.ShippingMethodService;
import com.example.shopping.model.Basket;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BasketCalculationService {

	private final ProductLineItemService lineItemService;
	private final ShippingMethodService shippingMethodService;
	private final CarrierServiceService carrierServiceService;
	
	public BasketCalculationService(ProductLineItemService pliService, 
									ShippingMethodService shippingMethodService,
									CarrierServiceService carrierServiceService) {
		
		this.lineItemService = pliService;
		this.shippingMethodService = shippingMethodService;
		this.carrierServiceService = carrierServiceService;
	}
	
	public Map<String,Double> calculateBasketTotals(Basket basket){
		Map<String, Double> totals = new HashMap<>();
		
		Double subTotal = calculateLineItemsTotal(totals, basket);
		Double shippingTotal = calculateShippingTotal(totals, basket);
		Double carrierTotal = calculateCarrierServiceTotal(totals, basket);
		Double total = subTotal + shippingTotal + carrierTotal;
		
		totals.put("total", total);
		
		return totals;
		
	}
	public Double calculateLineItemsTotal(Map<String,Double> totals, Basket basket) {
		
		Double subtotal = lineItemService.calculateBasketTotal(basket.getId());
		totals.put("subtotal", subtotal);
		return subtotal;
	}
	
	public Double calculateShippingTotal(Map<String,Double> totals, Basket basket) {
		
		Double shippingCost = 0.0;
		
		if (basket.getShippingMethodId() != null) {
			
			ShippingMethod method = shippingMethodService.getShippingMethodById(basket.getShippingMethodId()).orElse(null);
			
			if (method != null)
				shippingCost = method.getCost();
		}
		totals.put("shipping", shippingCost);
		
		return shippingCost;
		
	}
	public Double calculateCarrierServiceTotal(Map<String, Double> totals, Basket basket) {
		
		Double carrierCost = 0.0;
		if (basket.getCarrierServiceId() != null) {
            try {
                var carrierServiceDto = carrierServiceService.getServiceDtoById(basket.getCarrierServiceId());
                if (carrierServiceDto.isPresent()) {
                	carrierCost = carrierServiceDto.get().getCost();
                   
                }
            } catch (Exception e) {
                log.warn("Impossible de récupérer le coût de livraison: {}", e.getMessage());
            }
        }
		totals.put("carrierCost", carrierCost);
		
		return carrierCost;
	}
}
