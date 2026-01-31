package com.example.address.service;

import com.example.address.model.Address;
import com.example.address.repository.AddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AddressService {
    
    private final AddressRepository addressRepository;
    
    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }
    
    public List<Address> getAddressesByUserId(Long userId) {
        return addressRepository.findByUserId(userId);
    }
    
    public List<Address> getAddressesByUserIdAndType(Long userId, String addressType) {
        return addressRepository.findByUserIdAndAddressType(userId, addressType);
    }
    
    public Optional<Address> getAddressById(Long id) {
        return addressRepository.findById(id);
    }
    
    public Address saveAddress(Address address) {
        // Si c'est l'adresse par défaut, retirer le flag des autres
        if (address.getIsDefault()) {
            List<Address> userAddresses = addressRepository.findByUserId(address.getUserId());
            for (Address addr : userAddresses) {
                if (!addr.getId().equals(address.getId())) {
                    addr.setIsDefault(false);
                    addressRepository.save(addr);
                }
            }
        }
        return addressRepository.save(address);
    }
    
    public void deleteAddress(Long id) {
        addressRepository.deleteById(id);
    }
    
    public Address getDefaultAddress(Long userId) {
        return addressRepository.findByUserIdAndIsDefaultTrue(userId);
    }
}