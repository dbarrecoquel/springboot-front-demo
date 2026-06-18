package com.example.shippingmethod.service;

import com.example.shippingmethod.dto.CarrierDto;
import com.example.shippingmethod.mapper.CarrierMapper;
import com.example.shippingmethod.model.Carrier;
import com.example.shippingmethod.repository.CarrierRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class CarrierService {
    
    private final CarrierRepository carrierRepository;
    private final CarrierMapper carrierMapper;
    
    public CarrierService(CarrierRepository carrierRepository, CarrierMapper carrierMapper) {
        this.carrierRepository = carrierRepository;
        this.carrierMapper = carrierMapper;
    }
    
    /**
     * Récupérer tous les transporteurs (DTO)
     */
    @Transactional(readOnly = true)
    public List<CarrierDto> getAllCarriersDto() {
        return carrierMapper.toDtoList(carrierRepository.findAll());
    }
    
    /**
     * Récupérer tous les transporteurs (Entité)
     */
    @Transactional(readOnly = true)
    public List<Carrier> getAllCarriers() {
        return carrierRepository.findAll();
    }
    
    /**
     * Récupérer les transporteurs actifs (DTO)
     */
    @Transactional(readOnly = true)
    public List<CarrierDto> getActiveCarriersDto() {
        return carrierMapper.toDtoList(carrierRepository.findByEnabledTrue());
    }
    
    /**
     * Récupérer les transporteurs actifs (Entité)
     */
    @Transactional(readOnly = true)
    public List<Carrier> getActiveCarriers() {
        return carrierRepository.findByEnabledTrue();
    }
    
    /**
     * Récupérer un transporteur par ID (DTO)
     */
    @Transactional(readOnly = true)
    public Optional<CarrierDto> getCarrierDtoById(Long id) {
        return carrierRepository.findById(id).map(carrierMapper::toDto);
    }
    
    /**
     * Récupérer un transporteur par ID (Entité)
     */
    @Transactional(readOnly = true)
    public Optional<Carrier> getCarrierById(Long id) {
        return carrierRepository.findById(id);
    }
    
    /**
     * Récupérer un transporteur par code (DTO)
     */
    @Transactional(readOnly = true)
    public Optional<CarrierDto> getCarrierDtoByCode(String code) {
        return carrierRepository.findByCode(code).map(carrierMapper::toDto);
    }
    
    /**
     * Récupérer un transporteur par code (Entité)
     */
    @Transactional(readOnly = true)
    public Optional<Carrier> getCarrierByCode(String code) {
        return carrierRepository.findByCode(code);
    }
    
    /**
     * Créer un nouveau transporteur
     */
    public CarrierDto createCarrier(CarrierDto dto) {
        // Vérifier l'unicité du code
        if (carrierRepository.findByCode(dto.getCode()).isPresent()) {
            throw new IllegalArgumentException("Un transporteur avec le code '" + dto.getCode() + "' existe déjà");
        }
        
        // Mapper DTO vers entité
        Carrier carrier = carrierMapper.toEntity(dto);
        carrier.setCreatedAt(LocalDateTime.now());
        carrier.setUpdatedAt(LocalDateTime.now());
        
        // Sauvegarder
        Carrier saved = carrierRepository.save(carrier);
        
        log.info("✅ Transporteur créé: {} (code: {})", saved.getName(), saved.getCode());
        
        return carrierMapper.toDto(saved);
    }
    
    /**
     * Mettre à jour un transporteur
     */
    public CarrierDto updateCarrier(Long id, CarrierDto dto) {
        Carrier existing = carrierRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Transporteur non trouvé avec l'ID: " + id));
        
        // Vérifier l'unicité du code (si modifié)
        if (!existing.getCode().equals(dto.getCode()) && 
            carrierRepository.findByCode(dto.getCode()).isPresent()) {
            throw new IllegalArgumentException("Un transporteur avec le code '" + dto.getCode() + "' existe déjà");
        }
        
        // Mettre à jour les champs
        existing.setName(dto.getName());
        existing.setCode(dto.getCode());
        existing.setDescription(dto.getDescription());
        existing.setEnabled(dto.getEnabled());
        existing.setUpdatedAt(LocalDateTime.now());
        
        // Sauvegarder
        Carrier updated = carrierRepository.save(existing);
        
        log.info("📝 Transporteur mis à jour: {} (code: {})", updated.getName(), updated.getCode());
        
        return carrierMapper.toDto(updated);
    }
    
    /**
     * Supprimer un transporteur
     */
    public void deleteCarrier(Long id) {
        Carrier carrier = carrierRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Transporteur non trouvé avec l'ID: " + id));
        
        carrierRepository.deleteById(id);
        
        log.info("🗑️ Transporteur supprimé: {} (code: {})", carrier.getName(), carrier.getCode());
    }
    
    /**
     * Activer/désactiver un transporteur
     */
    public CarrierDto toggleCarrierStatus(Long id) {
        Carrier carrier = carrierRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Transporteur non trouvé avec l'ID: " + id));
        
        carrier.setEnabled(!carrier.getEnabled());
        carrier.setUpdatedAt(LocalDateTime.now());
        
        Carrier updated = carrierRepository.save(carrier);
        
        log.info("🔄 Statut du transporteur modifié: {} (enabled: {})", updated.getName(), updated.getEnabled());
        
        return carrierMapper.toDto(updated);
    }
    
    /**
     * Vérifier si un code existe
     */
    @Transactional(readOnly = true)
    public boolean codeExists(String code) {
        return carrierRepository.findByCode(code).isPresent();
    }
    
    /**
     * Vérifier si un code existe (hors de l'ID fourni)
     */
    @Transactional(readOnly = true)
    public boolean codeExistsExcludingId(String code, Long id) {
        return carrierRepository.findByCode(code)
            .map(c -> !c.getId().equals(id))
            .orElse(false);
    }
}