package com.example.payment.dto;

import java.time.LocalDateTime;

import com.example.payment.enums.TransactionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDto {
	
	private Long id;
	private Long orderId;
	private String transactionNumber;
	private PaymentMethodDto paymentMethod;
	private Double amount;
	private TransactionStatus status;
	private String externalReference;
	private String errorMessage;
	private String notes;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
