package com.example.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.payment.enums.PaymentMethodType;
import com.example.payment.model.PaymentMethod;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long>{
	
	Optional<PaymentMethod> findByType(PaymentMethodType type);
	List<PaymentMethod> findByEnabledOrderByDisplayOrder(Boolean enabled);
	List<PaymentMethod> findAllByOrderByDisplayOrder();

}
