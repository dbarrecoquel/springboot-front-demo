package com.example.frontrest.controller;

import com.example.address.dto.AddressDto;
import com.example.address.mapper.AddressMapper;
import com.example.address.model.Address;
import com.example.address.service.AddressService;
import com.example.user.dto.UserDto;
import com.example.user.mapper.UserMapper;
import com.example.user.model.User;
import com.example.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    
    private final UserService userService;
    private final AddressService addressService;
    private final AddressMapper addressMapper;
    private final UserMapper userMapper;
    
    public ProfileController(UserService userService, 
                           AddressService addressService,
                           AddressMapper addressMapper,
                           UserMapper userMapper) {
        this.userService = userService;
        this.addressService = addressService;
        this.addressMapper = addressMapper;
        this.userMapper = userMapper;
    }
    
    /* ===================== USER PROFILE ===================== */
    
    @GetMapping
    public ResponseEntity<UserDto> getProfile(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserDto userDto = userMapper.toDto(user);
        return ResponseEntity.ok(userDto);
    }
    
    @PutMapping
    public ResponseEntity<UserDto> updateProfile(
            @Valid @RequestBody UserDto userDto,
            Authentication authentication) {
        
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Mettre à jour l'utilisateur avec les données du DTO
        userMapper.updateEntityFromDto(userDto, user);
        
        // Sauvegarder
        User updated = userService.save(user);
        
        // Retourner le DTO mis à jour
        UserDto updatedDto = userMapper.toDto(updated);
        return ResponseEntity.ok(updatedDto);
    }
    
    /* ===================== ADDRESSES ===================== */
    
    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDto>> getAddresses(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Address> addresses = addressService.getAddressesByUserId(user.getId());
        List<AddressDto> addressDtos = addressMapper.toDtoList(addresses);
        
        return ResponseEntity.ok(addressDtos);
    }
    
    @PostMapping("/addresses")
    public ResponseEntity<AddressDto> createAddress(
            @Valid @RequestBody AddressDto addressDto,
            Authentication authentication) {
        
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Address address = addressMapper.toEntity(addressDto);
        address.setUserId(user.getId());
        
        Address saved = addressService.saveAddress(address);
        AddressDto savedDto = addressMapper.toDto(saved);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }
    
    @GetMapping("/addresses/{id}")
    public ResponseEntity<AddressDto> getAddressById(@PathVariable Long id,
                                                     Authentication authentication) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Address address = addressService.getAddressById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        
        if (!address.getUserId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        AddressDto addressDto = addressMapper.toDto(address);
        return ResponseEntity.ok(addressDto);
    }
    
    @PutMapping("/addresses/{id}")
    public ResponseEntity<AddressDto> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressDto addressDto,
            Authentication authentication) {
        
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Address existingAddress = addressService.getAddressById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        
        if (!existingAddress.getUserId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Address address = addressMapper.toEntity(addressDto);
        address.setId(id);
        address.setUserId(user.getId());
        
        Address updated = addressService.saveAddress(address);
        AddressDto updatedDto = addressMapper.toDto(updated);
        
        return ResponseEntity.ok(updatedDto);
    }
    
    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id,
                                              Authentication authentication) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Address address = addressService.getAddressById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        
        if (!address.getUserId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }
}