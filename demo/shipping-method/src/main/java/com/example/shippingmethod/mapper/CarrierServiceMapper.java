package com.example.shippingmethod.mapper;

import com.example.shippingmethod.dto.CarrierServiceDto;
import com.example.shippingmethod.dto.CarrierServiceListDto;
import com.example.shippingmethod.model.Carrier;
import com.example.shippingmethod.model.CarrierService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CarrierServiceMapper {
    
    CarrierServiceMapper INSTANCE = Mappers.getMapper(CarrierServiceMapper.class);
    
    /**
     * Mapper une entité CarrierService vers son DTO complet
     */
    @Mapping(source = "carrier.id", target = "carrierId")
    @Mapping(source = "carrier.name", target = "carrierName")
    @Mapping(source = "carrier.code", target = "carrierCode")
    @Mapping(expression = "java(service.getDeliveryDays() + service.getProcessingDays())", target = "totalDays")
    @Mapping(expression = "java(generateDeliveryEstimate(service))", target = "deliveryEstimate")
    CarrierServiceDto toDto(CarrierService service);
    
    /**
     * Mapper une entité CarrierService vers son DTO de liste
     */
    @Mapping(source = "name", target = "serviceName")
    @Mapping(source = "code", target = "serviceCode")
    @Mapping(source = "carrier.name", target = "carrierName")
    @Mapping(source = "carrier.code", target = "carrierCode")
    @Mapping(expression = "java(service.getDeliveryDays() + service.getProcessingDays())", target = "totalDays")
    @Mapping(expression = "java(getDeliveryIcon(service.getDeliveryDays()))", target = "deliveryIcon")
    CarrierServiceListDto toListDto(CarrierService service);
    
    /**
     * Mapper un DTO vers son entité - avec création d'un Carrier minimal
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "carrier", expression = "java(createCarrierFromDto(dto))")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CarrierService toEntity(CarrierServiceDto dto);
    
    /**
     * Mapper une liste d'entités vers une liste de DTOs complets
     */
    List<CarrierServiceDto> toDtoList(List<CarrierService> services);
    
    /**
     * Mapper une liste d'entités vers une liste de DTOs de liste
     */
    List<CarrierServiceListDto> toListDtoList(List<CarrierService> services);
    
    /**
     * Générer une estimation du délai de livraison
     */
    default String generateDeliveryEstimate(CarrierService service) {
        int totalDays = service.getDeliveryDays() + service.getProcessingDays();
        if (totalDays == 1) {
            return "Livraison en 1 jour";
        } else if (totalDays <= 3) {
            return "Livraison en " + totalDays + " jours";
        } else {
            return "Livraison en " + totalDays + " jours";
        }
    }
    
    /**
     * Déterminer l'icône du transport
     */
    default String getDeliveryIcon(Integer deliveryDays) {
        if (deliveryDays == null) {
            return "standard";
        }
        return deliveryDays <= 2 ? "fast" : "standard";
    }
    
    /**
     * Créer un Carrier minimal à partir du DTO
     */
    default Carrier createCarrierFromDto(CarrierServiceDto dto) {
        if (dto.getCarrierId() == null) {
            return null;
        }
        Carrier carrier = new Carrier();
        carrier.setId(dto.getCarrierId());
        return carrier;
    }
}