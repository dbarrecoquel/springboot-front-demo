package com.example.shippingmethod.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.shippingmethod.model.Carrier;

@Repository
public interface CarrierRepository extends JpaRepository<Carrier, Long>{
	
	List<Carrier> findByEnabledTrue();
    Optional<Carrier> findByCode(String code);

}
