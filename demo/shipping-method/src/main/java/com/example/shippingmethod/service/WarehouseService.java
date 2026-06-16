package com.example.shippingmethod.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.shippingmethod.dto.WarehouseDto;
import com.example.shippingmethod.mapper.WarehouseMapper;
import com.example.shippingmethod.model.Carrier;
import com.example.shippingmethod.model.ShippingMethod;
import com.example.shippingmethod.model.Warehouse;
import com.example.shippingmethod.repository.WarehouseRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class WarehouseService {
	private final WarehouseRepository warehouseRepository;
	private final WarehouseMapper warehouseMapper;
	
	public List<WarehouseDto> getAllWarehouse() {
    	return warehouseRepository.findAll().stream().map(warehouseMapper::toDto).collect(Collectors.toList());
    }
	
	public WarehouseDto getWarehouseById(Long id) {
		
		Warehouse warehouse = warehouseRepository.findById(id).orElseThrow(()->new RuntimeException("warehouse not found"));
		return warehouseMapper.toDto(warehouse);
		
	}
	public List<WarehouseDto> findByEnabledTrue() {
    	return warehouseRepository.findByEnabledTrue().stream().map(warehouseMapper::toDto).collect(Collectors.toList());
    }
	public WarehouseDto findByCode(String code){
		Warehouse warehouse = warehouseRepository.findByCode(code).orElseThrow(()->new RuntimeException("warehouse not found"));
		return warehouseMapper.toDto(warehouse);
	}
	public List<WarehouseDto> findByRegion(String region){
		return warehouseRepository.findByRegion(region).stream().map(warehouseMapper::toDto).collect(Collectors.toList());
	}
	@Transactional
    public Warehouse saveWarehouse(Warehouse warehouse) {
		if (warehouseRepository.findByCode(warehouse.getCode()).isPresent()) {
            throw new RuntimeException("Un transporteur avec ce code existe déjà");
        }
        
		warehouse.setCreatedAt(LocalDateTime.now());
		warehouse.setUpdatedAt(LocalDateTime.now());
        
        Warehouse saved = warehouseRepository.save(warehouse);
        log.info("Warehouse created: {} ({})", warehouse.getName(), warehouse.getCode());
        
        return saved;
    }
	@Transactional
    public void deleteWarehouse(Long id) {
        warehouseRepository.deleteById(id);
    }
	@Transactional
	 public Warehouse updateWarehouse(Long id, Warehouse warehouse) {
	        Warehouse existing = warehouseRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("Carrier not found"));
	        
	        // Vérifier que le code est unique (si modifié)
	        if (!existing.getCode().equals(warehouse.getCode()) && 
	            warehouseRepository.findByCode(warehouse.getCode()).isPresent()) {
	            throw new RuntimeException("Un entrepot avec ce code existe déjà");
	        }
	        
	        existing.setName(warehouse.getName());
	        existing.setCode(warehouse.getCode());
	        existing.setCity(warehouse.getCity());
	        existing.setCountry(warehouse.getCountry());
	        existing.setPostalCode(warehouse.getPostalCode());
	        existing.setRegion(warehouse.getRegion());
	        existing.setStreet(warehouse.getStreet());
	       
	        existing.setEnabled(warehouse.getEnabled());
	        existing.setUpdatedAt(LocalDateTime.now());
	        
	        Warehouse updated = warehouseRepository.save(existing);
	        log.info("Carrier updated: {} ({})", warehouse.getName(), warehouse.getCode());
	        
	        return updated;
	    }
}
