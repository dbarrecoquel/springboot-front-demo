package com.example.payment.model;

import java.time.LocalDateTime;

import com.example.payment.enums.TransactionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "order_id", nullable = false)
	private Long orderId;
	
	@Column(nullable = false, unique = true)
	private String transactionNumber;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "payment_method_id", nullable = false)
	private PaymentMethod paymentMethod;
	
	@Column(nullable = false)
	private Double amount;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TransactionStatus status = TransactionStatus.PENDING;
	
	@Column(name = "external_reference")
	private String externalReference;
	
	@Column(columnDefinition = "TEXT")
	private String errorMessage;
	
	@Column(columnDefinition = "TEXT")
	private String notes;
	
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();
	
	@Column(name = "updated_at")
	private LocalDateTime updatedAt = LocalDateTime.now();
	
	@Column(name = "completed_at")
	private LocalDateTime completedAt;
}
