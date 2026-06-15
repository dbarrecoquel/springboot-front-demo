package com.example.shippingmethod.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.shippingmethod.model.CarrierService;
@Repository
public interface CarrierServiceRepository extends JpaRepository<CarrierService, Long> {
	
	List<CarrierService> findByEnabledTrue();
	List<CarrierService> findByCarrierId(Long carrierId);
	
}
