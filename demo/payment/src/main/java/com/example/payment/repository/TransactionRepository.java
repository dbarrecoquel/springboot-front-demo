package com.example.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.payment.enums.TransactionStatus;
import com.example.payment.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
	
	Optional<Transaction> findByTransactionNumber(String transactionNumber);
	Optional<Transaction> findByOrderId(Long orderId);
	List<Transaction> findByStatus(TransactionStatus status);
	Page<Transaction> findByStatusOrderByCreatedAtDesc(TransactionStatus status, Pageable pageable);
	Page<Transaction> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
