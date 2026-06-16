package com.example.shippingmethod.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.shippingmethod.dto.CarrierDto;
import com.example.shippingmethod.mapper.CarrierMapper;
import com.example.shippingmethod.model.Carrier;
import com.example.shippingmethod.model.ShippingMethod;
import com.example.shippingmethod.repository.CarrierRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class CarrierService {
	
	private final CarrierRepository carrierRepository;
	private final CarrierMapper carrierMapper;
	
	
	public List<CarrierDto> getAllCarrier() {
    	return carrierRepository.findAll().stream().map(carrierMapper::toDto).collect(Collectors.toList());
    }
	
	public CarrierDto getCarrierById(Long id) {
		Carrier carrier = carrierRepository.findById(id).orElseThrow(() -> new RuntimeException("Carrier not found"));
		return carrierMapper.toDto(carrier);
	}
	public List<CarrierDto> findByEnabledTrue() {
		return carrierRepository.findByEnabledTrue().stream().map(carrierMapper::toDto).collect(Collectors.toList());
	}
    public CarrierDto findByCode(String code) {
    	Carrier carrier = carrierRepository.findByCode(code).orElseThrow(() -> new RuntimeException("Carrier not found"));
    	return carrierMapper.toDto(carrier);
    }
    @Transactional
    public Carrier saveCarrier(Carrier carrier) {
    	if (carrierRepository.findByCode(carrier.getCode()).isPresent()) {
            throw new RuntimeException("Un transporteur avec ce code existe déjà");
        }
        
        carrier.setCreatedAt(LocalDateTime.now());
        carrier.setUpdatedAt(LocalDateTime.now());
        
        Carrier saved = carrierRepository.save(carrier);
        log.info("Carrier created: {} ({})", carrier.getName(), carrier.getCode());
        
        return saved;
    }
    @Transactional
    public void deleteCarrierById(Long id) {
    	carrierRepository.deleteById(id);
    }
    public Carrier updateCarrier(Long id, Carrier carrier) {
        Carrier existing = carrierRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Carrier not found"));
        
        // Vérifier que le code est unique (si modifié)
        if (!existing.getCode().equals(carrier.getCode()) && 
            carrierRepository.findByCode(carrier.getCode()).isPresent()) {
            throw new RuntimeException("Un transporteur avec ce code existe déjà");
        }
        
        existing.setName(carrier.getName());
        existing.setCode(carrier.getCode());
        existing.setDescription(carrier.getDescription());
        existing.setEnabled(carrier.getEnabled());
        existing.setUpdatedAt(LocalDateTime.now());
        
        Carrier updated = carrierRepository.save(existing);
        log.info("Carrier updated: {} ({})", carrier.getName(), carrier.getCode());
        
        return updated;
    }
    public Carrier toggleCarrierStatus(Long id) {
        Carrier carrier = carrierRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Carrier not found"));
        
        carrier.setEnabled(!carrier.getEnabled());
        carrier.setUpdatedAt(LocalDateTime.now());
        
        Carrier updated = carrierRepository.save(carrier);
        log.info("Carrier status toggled: {} (enabled: {})", carrier.getName(), carrier.getEnabled());
        
        return updated;
    }
}
