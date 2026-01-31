package com.example.address.repository;

import com.example.address.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
    List<Address> findByUserIdAndAddressType(Long userId, String addressType);
    Address findByUserIdAndIsDefaultTrue(Long userId);
}