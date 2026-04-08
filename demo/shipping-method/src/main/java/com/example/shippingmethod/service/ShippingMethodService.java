package com.example.shippingmethod.service;

import org.springframework.stereotype.Service;
import com.example.shippingmethod.model.ShippingMethod;
import com.example.shippingmethod.repository.ShippingMethodRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
@Service
public class ShippingMethodService {
	
	private static final Logger logger = LoggerFactory.getLogger(ShippingMethodService.class);
    
    private final ShippingMethodRepository shippingMethodRepository;
    
    public ShippingMethodService(ShippingMethodRepository shippingMethodRepository) {
        this.shippingMethodRepository = shippingMethodRepository;
    }
    
    public List<ShippingMethod> getAllShippingMethods() {
    	return shippingMethodRepository.findAll();
    }
    
    public List<ShippingMethod> getEnabledShippingMethods(){
    	return shippingMethodRepository.findByEnabledTrue();
    }
    
    public List<ShippingMethod> getAvailaShippingMethods(String country) {
    	return shippingMethodRepository.findAvailableForCountry(country);
    	
    }
    
    public Optional<ShippingMethod> getShippingMethodById(Long id){
    	return shippingMethodRepository.findById(id);
    }
    @Transactional
    public ShippingMethod saveShippingMethod(ShippingMethod shippingMethod) {
    	return shippingMethodRepository.save(shippingMethod);
    }
    @Transactional
    public void deleteShippingMethod(Long id) {
        shippingMethodRepository.deleteById(id);
    }
    
    @Transactional
    public ShippingMethod toggleEnabled(Long id) {
        ShippingMethod shippingMethod = shippingMethodRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Shipping method not found: " + id));
        
        shippingMethod.setEnabled(!shippingMethod.getEnabled());
        
        logger.info("Toggled shipping method {} to {}", id, shippingMethod.getEnabled() ? "enabled" : "disabled");
        
        return shippingMethodRepository.save(shippingMethod);
    }
    
    public List<String> getAllDestinationCountries() {
        return shippingMethodRepository.findAllDestinationCountries();
    }
}
