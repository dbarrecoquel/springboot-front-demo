package com.example.payment.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.payment.dto.TransactionDto;
import com.example.payment.enums.TransactionStatus;
import com.example.payment.mapper.TransactionMapper;
import com.example.payment.model.PaymentMethod;
import com.example.payment.model.Transaction;
import com.example.payment.repository.PaymentMethodRepository;
import com.example.payment.repository.TransactionRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class TransactionService {
	
	private final TransactionRepository transactionRepository;
	private final PaymentMethodRepository paymentMethodRepository;
	private final TransactionMapper transactionMapper;
	
	public TransactionService(TransactionRepository transactionRepository,
							  PaymentMethodRepository paymentMethodRepository,
							  TransactionMapper transactionMapper) {
		
		this.transactionRepository = transactionRepository;
		this.paymentMethodRepository = paymentMethodRepository;
		this.transactionMapper = transactionMapper;
	}
	
	public TransactionDto createTransaction(Long orderId,
			 								Long paymentMethodId,
			 								Double amount) {
		
		PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId).orElseThrow(()-> new RuntimeException("Méthode de paiement non trouvée"));
		
		String transactionNumber = "TXN-" + System.currentTimeMillis()  + "-" + UUID.randomUUID();
		
		Transaction transaction = Transaction.builder().orderId(orderId)
													   .transactionNumber(transactionNumber)
													   .paymentMethod(paymentMethod)
													   .amount(amount)
													   .status(TransactionStatus.PENDING)
													   .createdAt(LocalDateTime.now())
													   .updatedAt(LocalDateTime.now())
													   .build();
		
		Transaction saved = transactionRepository.save(transaction);
		
		log.info("Transaction créée: {} | Commande: {} | Montant: {} | Méthode: {}", 
	            transactionNumber, orderId, amount, paymentMethod.getType());
	        
	    return transactionMapper.toDto(saved);
	}
	
	@Transactional(readOnly = true)
	public Optional<TransactionDto> getTransactionById(Long id) {
		
		return transactionRepository.findById(id).map(transactionMapper::toDto);
	}
	
	@Transactional(readOnly = true)
	public Optional<TransactionDto> getTransactionByNumber(String transactionNumber) {
		
		return transactionRepository.findByTransactionNumber(transactionNumber).map(transactionMapper::toDto);
	}
	
	@Transactional(readOnly = true)
	public Optional<TransactionDto> getTransactionByOrderId(Long orderId) {
		
		return transactionRepository.findByOrderId(orderId).map(transactionMapper::toDto);
	}
	
	public TransactionDto updateTransactionStatus(Long transactionId,
			 									TransactionStatus status,
			 									String noted) {
		
		Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new RuntimeException("Transaction non trouvée"));
		
		transaction.setStatus(status);
		transaction.setNotes(noted);
		transaction.setUpdatedAt(LocalDateTime.now());
		
		if (status == TransactionStatus.COMPLETED) {
            transaction.setCompletedAt(LocalDateTime.now());
        }
		Transaction saved = transactionRepository.save(transaction);
		
		log.info("Transaction {} mise à jour: {}", transaction.getTransactionNumber(), status);
        
        return transactionMapper.toDto(saved);
		
	}
	
	public TransactionDto completeTransaction(Long transactionId, String externalReference) {
		
		Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new RuntimeException("Transaction non trouvée"));
		
		transaction.setStatus(TransactionStatus.COMPLETED);
		transaction.setExternalReference(externalReference);
		transaction.setCompletedAt(LocalDateTime.now());
		transaction.setUpdatedAt(LocalDateTime.now());
		
		Transaction saved = transactionRepository.save(transaction);
		
		log.info("Transaction {} complétée", transaction.getTransactionNumber());
	        
	     return transactionMapper.toDto(saved);
	}
	
	public TransactionDto failTransaction(Long transactionId, String errorMessage) {
		Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new RuntimeException("Transaction non trouvée"));
		
		transaction.setStatus(TransactionStatus.FAILED);
		transaction.setErrorMessage(errorMessage);
		transaction.setUpdatedAt(LocalDateTime.now());
		
		Transaction saved = transactionRepository.save(transaction);
		
		log.warn("Transaction {} échouée: {}", transaction.getTransactionNumber(), errorMessage);
        
        return transactionMapper.toDto(saved);
	}
	
	@Transactional(readOnly = true)
	public Page<TransactionDto> getTransactionsByStatus(TransactionStatus status, Pageable pageable) {
		
		return transactionRepository.findByStatusOrderByCreatedAtDesc(status, pageable).map(transactionMapper::toDto);
	}
	
	@Transactional(readOnly = true)
	public Page<TransactionDto> getAllTransactions(Pageable pageable) {
		
		return transactionRepository.findAllByOrderByCreatedAtDesc(pageable).map(transactionMapper::toDto);
	}
}
