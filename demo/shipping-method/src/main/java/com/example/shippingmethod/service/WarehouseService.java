package com.example.shippingmethod.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.shippingmethod.dto.WarehouseDto;
import com.example.shippingmethod.mapper.WarehouseMapper;
import com.example.shippingmethod.model.ShippingMethod;
import com.example.shippingmethod.model.Warehouse;
import com.example.shippingmethod.repository.WarehouseRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
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
	public List<WarehouseDto> findByCode(String code){
		return warehouseRepository.findByCode(code).stream().map(warehouseMapper::toDto).collect(Collectors.toList());
	}
	public List<WarehouseDto> findByRegion(String region){
		return warehouseRepository.findByRegion(region).stream().map(warehouseMapper::toDto).collect(Collectors.toList());
	}
	@Transactional
    public Warehouse saveWarehouse(Warehouse warehouse) {
    	return warehouseRepository.save(warehouse);
    }
	@Transactional
    public void deleteWarehouse(Long id) {
        warehouseRepository.deleteById(id);
    }
}
