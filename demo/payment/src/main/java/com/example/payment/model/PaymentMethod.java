package com.example.payment.model;

import java.time.LocalDateTime;

import com.example.payment.enums.PaymentMethodType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_methods")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethod {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, unique = true)
	private PaymentMethodType type;
	
	@Column(nullable = false)
	private String name;
	
	@Column(columnDefinition = "TEXT")
	private String description;
	
	private String icon;
	
	@Column(nullable = false)
	private Boolean enabled = true;
	
	@Column(name = "display_order")
	private Integer displayOrder = 0;
	
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime created_at = LocalDateTime.now();
	
	@Column(name = "updated_at")
	private LocalDateTime updatedAt = LocalDateTime.now();
	
	
}
