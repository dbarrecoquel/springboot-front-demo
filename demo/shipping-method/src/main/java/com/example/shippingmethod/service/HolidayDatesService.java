package com.example.shippingmethod.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.shippingmethod.dto.HolidayDatesDto;
import com.example.shippingmethod.dto.HolidayDatesListDto;
import com.example.shippingmethod.mapper.HolidayDatesMapper;
import com.example.shippingmethod.model.HolidayDate;
import com.example.shippingmethod.model.Warehouse;
import com.example.shippingmethod.repository.HolidayDateRepository;
import com.example.shippingmethod.repository.WarehouseRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class HolidayDatesService {
	
	 private final HolidayDateRepository holidayRepository;
	 private final WarehouseRepository warehouseRepository;
	 private final HolidayDatesMapper mapper;
	   
	    
	    /**
	     * Récupérer tous les holiday (DTO de liste)
	     */
	    public List<HolidayDatesListDto> getAllHolidayDatesListDto() {
	        return mapper.toListDtoList(holidayRepository.findAll());
	    }
	    
	    /**
	     * Récupérer tous les holiday (DTO complet)
	     */
	    public List<HolidayDatesDto> getAllHolidayDatesDto() {
	        return mapper.toDtoList(holidayRepository.findAll());
	    }
	    
	    /**
	     * Récupérer tous les holiday (Entité)
	     */
	    public List<HolidayDate> getAllHolidayDates() {
	        return holidayRepository.findAll();
	    }
	    /*
	     * recuperer tous les holiday by warehouse list
	     */
	    public List<HolidayDatesListDto> getByWarehouseListDto(Long warehouseId){
	    	return mapper.toListDtoList(holidayRepository.findAll());
	    }
	    
	    /*
	     * recuperer tous les holiday by warehouse
	     */
	    public List<HolidayDatesDto> getByWarehouseIdDto(Long warehouseId){
	    	return mapper.toDtoList(holidayRepository.findByWarehouseId(warehouseId));
	    }
	    
	    public List<HolidayDatesListDto> getByWarehouseIdAndIsRecurringTrueListDto(Long warehouseId){
	    	return mapper.toListDtoList(holidayRepository.findByWarehouseIdAndIsRecurringTrue(warehouseId));
	    }

	    public List<HolidayDatesDto> getByWarehouseIdAndIsRecurringTrueDto(Long warehouseId){
	    	return mapper.toDtoList(holidayRepository.findByWarehouseIdAndIsRecurringTrue(warehouseId));
	    }

		public boolean existsByWarehouseIdAndHolidayDate(Long warehouseId, LocalDate hollidayDate) {
			return holidayRepository.existsByWarehouseIdAndHolidayDate(warehouseId, hollidayDate);
		}
		
		@Transactional
		public HolidayDatesDto createHolidayDate(HolidayDatesDto date) {
			Warehouse warehouse = warehouseRepository.findById(date.getWarehouseId()).orElseThrow(() -> new RuntimeException("Warehouse not found"));
			
			if (date.getRecurring() == null)
				date.setRecurring(false);
			
			HolidayDate holiday = mapper.toEntity(date);
			holiday.setWarehouse(warehouse);
	        holiday.setCreatedAt(LocalDateTime.now());
	        holiday.setUpdatedAt(LocalDateTime.now());
	        
	        HolidayDate saved = holidayRepository.save(holiday);
	        
	        return mapper.toDto(saved);
		}
		@Transactional
		public HolidayDatesDto updateHolidayDate(HolidayDatesDto date) {
			
			HolidayDate existing = holidayRepository.findById(date.getId()).orElseThrow(() -> new RuntimeException("holiday date non trouvée"));
			
	        // Vérifier le warehouse si modifié
	        Warehouse warehouse = existing.getWarehouse();
	        if (!existing.getWarehouse().getId().equals(date.getWarehouseId())) {
	            warehouse = warehouseRepository.findById(date.getWarehouseId())
	                .orElseThrow(() -> new RuntimeException("Warehouse non trouvé avec l'ID: " + date.getWarehouseId()));
	        }
			
			
			if (date.getRecurring() == null)
				date.setRecurring(true);
			
			existing.setCountry(date.getCountry());
			existing.setDescription(date.getDescription());
			existing.setHolidayDate(date.getHolidayDate());
			existing.setIsRecurring(date.getRecurring());
			existing.setName(date.getName());
			existing.setWarehouse(warehouse);
			
	        
	        HolidayDate saved = holidayRepository.save(existing);
	        
	        return mapper.toDto(saved);
		}
		public void deleteService(Long id) {
	        HolidayDate date = holidayRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("Holiday non trouvé avec l'ID: " + id));
	        
	        holidayRepository.deleteById(id);
	        
	        log.info("Holiday supprimé: {}, warehouse {} ", date.getName(), date.getWarehouse().getName());
	    }
}
