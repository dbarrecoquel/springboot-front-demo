package com.example.shopping.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.shippingmethod.model.ShippingMethod;
import com.example.shippingmethod.service.ShippingMethodService;
import com.example.shopping.model.Basket;

@Service
public class BasketCalculationService {

	private final ProductLineItemService lineItemService;
	private final ShippingMethodService shippingMethodService;
	
	public BasketCalculationService(ProductLineItemService pliService, ShippingMethodService shippingMethodService) {
		
		this.lineItemService = pliService;
		this.shippingMethodService = shippingMethodService;
	}
	
	public Map<String,Double> calculateBasketTotals(Basket basket){
		Map<String, Double> totals = new HashMap<>();
		
		Double subTotal = calculateLineItemsTotal(totals, basket);
		Double shippingTotal = calculateShippingTotal(totals, basket);
		Double total = subTotal + shippingTotal;
		
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
}
