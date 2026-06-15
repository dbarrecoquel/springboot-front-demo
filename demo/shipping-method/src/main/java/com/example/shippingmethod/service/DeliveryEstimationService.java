package com.example.shippingmethod.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.shippingmethod.dto.DeliveryEstimateDTO;
import com.example.shippingmethod.model.CarrierService;
import com.example.shippingmethod.model.HolidayDate;
import com.example.shippingmethod.model.Warehouse;
import com.example.shippingmethod.repository.CarrierServiceRepository;
import com.example.shippingmethod.repository.HolidayDateRepository;
import com.example.shippingmethod.repository.WarehouseRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DeliveryEstimationService {

	private final CarrierServiceRepository carrierServiceRepository;
	private final HolidayDateRepository holidayDateRepository;
	private final WarehouseRepository warehouseRepository;
	
	public DeliveryEstimationService(CarrierServiceRepository carrierServiceRepository,
									 HolidayDateRepository holidayDateRepository,
									 WarehouseRepository warehouseRepository) {
		
		this.carrierServiceRepository = carrierServiceRepository;
		this.holidayDateRepository = holidayDateRepository;
		this.warehouseRepository = warehouseRepository;
		
	}
	
	public DeliveryEstimateDTO estimateDelivery(Long carrierServiceId, Long warehouseId, Double orderAmount, LocalDateTime orderDateTime) {
		
		CarrierService carrierService = carrierServiceRepository.findById(carrierServiceId).orElseThrow(() -> new RuntimeException("Carrier not found"));
		Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow(()-> new RuntimeException("Warehouse not found"));
		
		//determiner la date de début du traitement
		LocalDate processingStartDate = calculateProcessingStartDate(orderDateTime, carrierService.getCutoffTime());
		
		//calculer la date d'enlevement (après le délai de traitement)	
		LocalDate pickUpDate = addBusinessDays(processingStartDate, carrierService.getProcessingDays(), warehouse.getId());
		// Calculer la date de livraison (après les jours de transport)
		LocalDate deliveryDate = addBusinessDays(pickUpDate, carrierService.getDeliveryDays(), warehouse.getId());
		// Date la plus tardive (avec marge de sécurité)
		LocalDate latestDeliveryDate = deliveryDate.plusDays(1);
		
		// determiner si la livraison est gratuite
		Boolean freeShipping = orderAmount >= carrierService.getFreeShippingMinAmount();
		
		Double cost = freeShipping ? 0.0 : carrierService.getCost();
		
		DeliveryEstimateDTO estimate = new DeliveryEstimateDTO();
		
		estimate.setCarrierServiceId(carrierServiceId);
		estimate.setCarrierName(carrierService.getCarrier().getName());
		estimate.setServiceName(carrierService.getName());
		estimate.setServiceDescription(carrierService.getDescription());
		estimate.setOrderDate(orderDateTime);
		estimate.setEarliestDeliveryDate(deliveryDate);
		estimate.setLatestDeliveryDate(latestDeliveryDate);
		estimate.setEstimatedDays(carrierService.getDeliveryDays() + carrierService.getProcessingDays());
		estimate.setCost(cost);
		estimate.setFreeShipping(freeShipping);
		estimate.setWarehouseCode(warehouse.getCode());
		estimate.setWarehouseName(warehouse.getName());
		estimate.setCutoffTime(carrierService.getCutoffTime().toString());
		estimate.setProcessingInfo(String.format("Traitement: %d jour(s) | transport: %d jours", carrierService.getProcessingDays(),carrierService.getDeliveryDays()));
		
		log.info("Delivery estimated for service {}: {} to {}", 
	            carrierService.getName(), deliveryDate, latestDeliveryDate);
	        
	        return estimate;
	
	}

	private LocalDate addBusinessDays(LocalDate startDate, Integer businessDay, Long warehouseId ) {
		LocalDate currentDate = startDate;
		int daysAdded = 0;
		
		while (daysAdded < businessDay)
		{
			currentDate.plusDays(1);
			if (isBusinessDay(currentDate, warehouseId))
				daysAdded++;
				
		}
		return currentDate;
	}
	private boolean isBusinessDay(LocalDate date , Long warehouseId) {
		//exclure les weekends (samedi et dimanche)
		DayOfWeek dayOfWeek = date.getDayOfWeek();
		if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY)
		{
			return false;
		}
		if (isHolidayDate(date,warehouseId)) {
			return false;
		}
		return true;
	}
	private boolean isHolidayDate(LocalDate date, Long warehouseId) {
		//cherche un jour férié exact
		boolean hasExactHoliday = holidayDateRepository.existsByWarehouseIdAndHolidayDate(warehouseId, date);
        if (hasExactHoliday) {
            return true;
        }
        // Chercher un jour férié récurrent (même mois/jour mais année différente)
        List<HolidayDate> recurringHolidays = holidayDateRepository
            .findByWarehouseIdAndIsRecurringTrue(warehouseId);
        
        for (HolidayDate holiday : recurringHolidays) {
            LocalDate holidayInYear = holiday.getHolidayDate()
                .withYear(date.getYear());
            if (holidayInYear.equals(date)) {
                return true;
            }
        }
		return false;
	}

	private LocalDate calculateProcessingStartDate(LocalDateTime orderDateTime, LocalTime cutoffTime) {
		LocalDate orderDate = orderDateTime.toLocalDate();
		LocalTime orderTime = orderDateTime.toLocalTime();
		
		// Si la commande est après l'heure limite, traitement commençe le jour suivant
		if (orderTime.isAfter(cutoffTime))
			orderDate.plusDays(1);
		
		return orderDate;
	}
	/**
     * Obtenir tous les services disponibles avec estimations
     */
    public List<DeliveryEstimateDTO> getAvailableDeliveryOptions(
            Long warehouseId,
            Double orderAmount,
            LocalDateTime orderDateTime) {
        
        List<CarrierService> services = carrierServiceRepository.findByEnabledTrue();
        
        return services.stream()
            .map(service -> estimateDelivery(service.getId(), warehouseId, orderAmount, orderDateTime))
            .sorted((a, b) -> a.getEstimatedDays().compareTo(b.getEstimatedDays()))
            .toList();
    }
}
