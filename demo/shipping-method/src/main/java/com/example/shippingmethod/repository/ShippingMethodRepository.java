package com.example.shippingmethod.repository;

import com.example.shippingmethod.model.ShippingMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingMethodRepository extends JpaRepository<ShippingMethod, Long> {
    
	Optional<ShippingMethod> findByName(String name);
	
    List<ShippingMethod> findByEnabledTrue();
    
    List<ShippingMethod> findByDestinationCountry(String destinationCountry);
    
    @Query("SELECT sm FROM ShippingMethod sm WHERE sm.enabled = true AND " +
           "(sm.destinationCountry = :country OR sm.destinationCountry = 'ALL' OR " +
           "(sm.destinationCountry = 'EU' AND :country IN " +
           "('France', 'Allemagne', 'Belgique', 'Pays-Bas', 'Luxembourg', 'Italie', 'Espagne', 'Portugal', 'Autriche', 'Irlande')))")
    List<ShippingMethod> findAvailableForCountry(@Param("country") String country);
    
    @Query("SELECT DISTINCT sm.destinationCountry FROM ShippingMethod sm ORDER BY sm.destinationCountry")
    List<String> findAllDestinationCountries();
}