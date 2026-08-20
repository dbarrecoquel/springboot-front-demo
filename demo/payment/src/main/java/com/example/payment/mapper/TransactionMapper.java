package com.example.payment.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.example.payment.dto.TransactionDto;
import com.example.payment.model.Transaction;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = PaymentMethodMapper.class)
public interface TransactionMapper {
	
	TransactionDto toDto(Transaction transaction);
	Transaction toEntity(TransactionDto dto);
	List<TransactionDto> toDtoList(List<Transaction> transactions);
}
