package com.example.shippingmethod.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.shippingmethod.model.DeliveryRule;

@Repository
public interface DeliveryRuleRepository extends JpaRepository<DeliveryRule, UUID> {

	List<DeliveryRule> findByCarrierIdAndShippingMethodIdAndCountryCodeAndActiveTrue(UUID carrierId, UUID shippingMethodId, String countryCode);
}
