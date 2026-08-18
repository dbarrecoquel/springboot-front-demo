package com.example.shippingmethod.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shippingmethod.model.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

	List<Warehouse> findByEnabledTrue();
	Optional<Warehouse> findByCode(String code);
	List<Warehouse> findByRegion(String region);
    List<Warehouse> findByCountry(String country);
    List<Warehouse> findByCountryAndEnabledTrue(String country);
}
