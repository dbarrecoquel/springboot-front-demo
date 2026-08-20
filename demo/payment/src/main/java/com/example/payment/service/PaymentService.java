package com.example.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.payment.dto.PaymentMethodDto;
import com.example.payment.dto.TransactionDto;
import com.example.payment.enums.PaymentMethodType;
import com.example.payment.enums.TransactionStatus;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class PaymentService {
	
	private final PaymentMethodService paymentMethodService;
	private final TransactionService transactionService;
	
	public PaymentService(PaymentMethodService paymentMethodService, TransactionService transactionService) {
		
		this.paymentMethodService = paymentMethodService;
		this.transactionService = transactionService;
	}
	/*
	 * Cash on delivery
	 */
	public TransactionDto initiateCODPayment(Long orderId, Double amount) {
		
		PaymentMethodDto codMethod = paymentMethodService.getPaymentMethodByType(PaymentMethodType.COD)
				 .orElseThrow(() -> new RuntimeException("Méthod COD non configurée"));
		
		if (!codMethod.getEnabled())
			throw new RuntimeException("Le paiement à la livraison n'est pas activée");
		
		log.info("Initiation paiement COD | Commande: {} | Montant: {}", orderId, amount);
	    
		TransactionDto transaction = transactionService.createTransaction(orderId, codMethod.getId(), amount);
		
		transaction = transactionService.updateTransactionStatus(
		            transaction.getId(),
		            TransactionStatus.PENDING,
		            "En attente de livraison"
		        );
		
		log.info("Paiement COD initialisé: {}", transaction.getTransactionNumber());
        
        return transaction;
	}
	public TransactionDto confirmCODPayment(Long transactionId) {
        TransactionDto transaction = transactionService.getTransactionById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction non trouvée"));
        
        return transactionService.completeTransaction(
            transactionId,
            null
        );
    }
	public TransactionDto cancelTransaction(Long transactionId, String reason) {
        return transactionService.updateTransactionStatus(
            transactionId,
            TransactionStatus.CANCELLED,
            reason
        );
    }
}
