package com.example.shippingmethod.service;

import com.example.shippingmethod.dto.CarrierServiceDto;
import com.example.shippingmethod.dto.CarrierServiceListDto;
import com.example.shippingmethod.mapper.CarrierServiceMapper;
import com.example.shippingmethod.model.Carrier;
import com.example.shippingmethod.model.CarrierService;
import com.example.shippingmethod.repository.CarrierRepository;
import com.example.shippingmethod.repository.CarrierServiceRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class CarrierServiceService {
    
    private final CarrierServiceRepository serviceRepository;
    private final CarrierRepository carrierRepository;
    private final CarrierServiceMapper mapper;
   
    
    /**
     * Récupérer tous les services (DTO de liste)
     */
    @Transactional(readOnly = true)
    public List<CarrierServiceListDto> getAllServicesListDto() {
        return mapper.toListDtoList(serviceRepository.findAll());
    }
    
    /**
     * Récupérer tous les services (DTO complet)
     */
    @Transactional(readOnly = true)
    public List<CarrierServiceDto> getAllServicesDto() {
        return mapper.toDtoList(serviceRepository.findAll());
    }
    
    /**
     * Récupérer tous les services (Entité)
     */
    @Transactional(readOnly = true)
    public List<CarrierService> getAllServices() {
        return serviceRepository.findAll();
    }
    
    /**
     * Récupérer les services actifs (DTO de liste)
     */
    @Transactional(readOnly = true)
    public List<CarrierServiceListDto> getActiveServicesListDto() {
        return mapper.toListDtoList(serviceRepository.findByEnabledTrue());
    }
    
    /**
     * Récupérer les services actifs (DTO complet)
     */
    @Transactional(readOnly = true)
    public List<CarrierServiceDto> getActiveServicesDto() {
        return mapper.toDtoList(serviceRepository.findByEnabledTrue());
    }
    
    /**
     * Récupérer les services d'un transporteur (DTO de liste)
     */
    @Transactional(readOnly = true)
    public List<CarrierServiceListDto> getServicesByCarrierIdListDto(Long carrierId) {
        return mapper.toListDtoList(serviceRepository.findByCarrierId(carrierId));
    }
    
    /**
     * Récupérer les services d'un transporteur (DTO complet)
     */
    @Transactional(readOnly = true)
    public List<CarrierServiceDto> getServicesByCarrierIdDto(Long carrierId) {
        return mapper.toDtoList(serviceRepository.findByCarrierId(carrierId));
    }
    
    /**
     * Récupérer un service par ID (DTO complet)
     */
    @Transactional(readOnly = true)
    public Optional<CarrierServiceDto> getServiceDtoById(Long id) {
        return serviceRepository.findById(id).map(mapper::toDto);
    }
    
    /**
     * Récupérer un service par ID (DTO de liste)
     */
    @Transactional(readOnly = true)
    public Optional<CarrierServiceListDto> getServiceListDtoById(Long id) {
        return serviceRepository.findById(id).map(mapper::toListDto);
    }
    
    /**
     * Récupérer un service par ID (Entité)
     */
    @Transactional(readOnly = true)
    public Optional<CarrierService> getServiceById(Long id) {
        return serviceRepository.findById(id);
    }
    
    /**
     * Créer un nouveau service
     */
    public CarrierServiceDto createService(CarrierServiceDto dto) {
        // Vérifier le transporteur
        Carrier carrier = carrierRepository.findById(dto.getCarrierId())
            .orElseThrow(() -> new RuntimeException("Transporteur non trouvé avec l'ID: " + dto.getCarrierId()));
        
        // S'assurer que les valeurs par défaut sont présentes
        if (dto.getEnabled() == null) {
            dto.setEnabled(true);
        }
        if (dto.getCost() == null) {
            dto.setCost(0.0);
        }
        if (dto.getFreeShippingMinAmount() == null) {
            dto.setFreeShippingMinAmount(50.0);
        }
        
        // Créer l'entité
        CarrierService service = mapper.toEntity(dto);
        service.setCarrier(carrier);
        service.setCreatedAt(LocalDateTime.now());
        service.setUpdatedAt(LocalDateTime.now());
        
        // Sauvegarder
        CarrierService saved = serviceRepository.save(service);
        
        log.info("Service créé: {} (transporteur: {})", saved.getName(), carrier.getName());
        
        return mapper.toDto(saved);
    }
    
    /**
     * Mettre à jour un service
     */
    public CarrierServiceDto updateService(Long id, CarrierServiceDto dto) {
        CarrierService existing = serviceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Service non trouvé avec l'ID: " + id));
        
        // Vérifier le transporteur si modifié
        Carrier carrier = existing.getCarrier();
        if (!existing.getCarrier().getId().equals(dto.getCarrierId())) {
            carrier = carrierRepository.findById(dto.getCarrierId())
                .orElseThrow(() -> new RuntimeException("Transporteur non trouvé avec l'ID: " + dto.getCarrierId()));
        }
        
        // S'assurer que les valeurs ne sont pas null
        if (dto.getEnabled() == null) {
            dto.setEnabled(true);
        }
        if (dto.getCost() == null) {
            dto.setCost(0.0);
        }
        if (dto.getFreeShippingMinAmount() == null) {
            dto.setFreeShippingMinAmount(50.0);
        }
        
        // Mettre à jour les champs
        existing.setName(dto.getName());
        existing.setCode(dto.getCode());
        existing.setDescription(dto.getDescription());
        existing.setDeliveryDays(dto.getDeliveryDays());
        existing.setProcessingDays(dto.getProcessingDays());
        existing.setCutoffTime(dto.getCutoffTime());
        existing.setCost(dto.getCost());
        existing.setFreeShippingMinAmount(dto.getFreeShippingMinAmount());
        existing.setEnabled(dto.getEnabled());
        existing.setCarrier(carrier);
        existing.setUpdatedAt(LocalDateTime.now());
        
        // Sauvegarder
        CarrierService updated = serviceRepository.save(existing);
        
        log.info("Service mis à jour: {} (transporteur: {})", updated.getName(), carrier.getName());
        
        return mapper.toDto(updated);
    }
    
    /**
     * Supprimer un service
     */
    public void deleteService(Long id) {
        CarrierService service = serviceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Service non trouvé avec l'ID: " + id));
        
        serviceRepository.deleteById(id);
        
        log.info("Service supprimé: {} (transporteur: {})", service.getName(), service.getCarrier().getName());
    }
    
    /**
     * Activer/désactiver un service
     */
    public CarrierServiceDto toggleServiceStatus(Long id) {
        CarrierService service = serviceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Service non trouvé avec l'ID: " + id));
        
        service.setEnabled(!service.getEnabled());
        service.setUpdatedAt(LocalDateTime.now());
        
        CarrierService updated = serviceRepository.save(service);
        
        log.info("Statut du service modifié: {} (enabled: {})", updated.getName(), updated.getEnabled());
        
        return mapper.toDto(updated);
    }
    
    /**
     * Récupérer le coût de livraison
     */
    @Transactional(readOnly = true)
    public Double getServiceCost(Long id, Double orderAmount) {
        return serviceRepository.findById(id)
            .map(service -> {
                if (orderAmount >= service.getFreeShippingMinAmount()) {
                    return 0.0;
                }
                return service.getCost();
            })
            .orElse(0.0);
    }
    
    /**
     * Compter les services d'un transporteur
     */
    @Transactional(readOnly = true)
    public long countServicesByCarrierId(Long carrierId) {
        return serviceRepository.findByCarrierId(carrierId).size();
    }
}