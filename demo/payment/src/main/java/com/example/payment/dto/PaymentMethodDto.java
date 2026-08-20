package com.example.payment.dto;

import com.example.payment.enums.PaymentMethodType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodDto {
	
	private Long id;
	private PaymentMethodType type;
	private String name;
	private String description;
	private String icon;
	private Boolean enabled;
	private Integer displayOrder;
}
