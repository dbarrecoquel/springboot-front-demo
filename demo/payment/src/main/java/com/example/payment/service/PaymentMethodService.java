package com.example.payment.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.payment.dto.PaymentMethodDto;
import com.example.payment.enums.PaymentMethodType;
import com.example.payment.mapper.PaymentMethodMapper;
import com.example.payment.model.PaymentMethod;
import com.example.payment.repository.PaymentMethodRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class PaymentMethodService {
	
	private final PaymentMethodRepository paymentMethodRepository;
	private final PaymentMethodMapper paymentMethodMapper;
	
	public PaymentMethodService(PaymentMethodRepository paymentMethodRepository,
			 					PaymentMethodMapper paymentMethodMapper) {
		
		this.paymentMethodRepository = paymentMethodRepository;
		this.paymentMethodMapper = paymentMethodMapper;
	}
	
	@Transactional(readOnly = true)
	public Optional<PaymentMethodDto> getPaymentMethodById(Long id){
		
		return paymentMethodRepository.findById(id).map(paymentMethodMapper::toDto);
		
	}
	
	@Transactional(readOnly = true)
	public Optional<PaymentMethodDto> getPaymentMethodByType(PaymentMethodType type) {
		
		return paymentMethodRepository.findByType(type).map(paymentMethodMapper::toDto);
	}
	
	@Transactional(readOnly = true)
    public List<PaymentMethodDto> getEnabledPaymentMethods() {
        return paymentMethodMapper.toDtoList(
            paymentMethodRepository.findByEnabledOrderByDisplayOrder(true)
        );
    }
	
	@Transactional(readOnly = true)
	public List<PaymentMethodDto> getAllPaymentMethods() {
		
		return paymentMethodMapper.toDtoList(paymentMethodRepository.findAllByOrderByDisplayOrder());
	}
	
	public PaymentMethodDto createOrUpdatePaymentMethod(
			 PaymentMethodType type,
			 String name,
			 String description,
			 String icon,
			 Boolean enabled,
			 Integer displayOrder
			) {
		
		PaymentMethod paymentMethod = paymentMethodRepository.findByType(type).orElse(new PaymentMethod());
		
		paymentMethod.setType(type);
		paymentMethod.setName(name);
		paymentMethod.setDescription(description);
		paymentMethod.setIcon(icon);
		paymentMethod.setEnabled(enabled);
		paymentMethod.setDisplayOrder(displayOrder);
		paymentMethod.setUpdatedAt(LocalDateTime.now());
		
		PaymentMethod saved = paymentMethodRepository.save(paymentMethod);
		
		log.info("Méthode de paiement sauvegardée: {} ({})", type, name);
 
		return paymentMethodMapper.toDto(saved);
	}
	
	public PaymentMethodDto togglePaymentMethod(Long id, Boolean enabled) {
		PaymentMethod paymentMethod = paymentMethodRepository.findById(id).orElseThrow(() -> new RuntimeException("Méthode de paiement non trouvée"));
		
		paymentMethod.setEnabled(enabled);
		paymentMethod.setUpdatedAt(LocalDateTime.now());
		
		PaymentMethod saved = paymentMethodRepository.save(paymentMethod);
		
		log.info("Méthode {} {}", paymentMethod.getType(), enabled ? "activée" : "désactivée");
        
        return paymentMethodMapper.toDto(saved);
	}
	
	public void deletePaymentMethod(Long id) {
		
		paymentMethodRepository.deleteById(id);
		log.info("Méthode de paiement supprimée");
	}
}
