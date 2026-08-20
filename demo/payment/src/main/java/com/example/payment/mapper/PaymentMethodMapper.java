package com.example.payment.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.example.payment.dto.PaymentMethodDto;
import com.example.payment.model.PaymentMethod;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMethodMapper {
	
	PaymentMethodDto toDto(PaymentMethod paymentMethod);
	PaymentMethod toEntity(PaymentMethodDto dto);
	List<PaymentMethodDto> toDtoList(List<PaymentMethod> paymentMethods);
	List<PaymentMethod> toEntityList(List<PaymentMethodDto> dtos);

}
