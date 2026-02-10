package com.example.frontrest.controller;

import com.example.address.model.Address;
import com.example.address.service.AddressService;
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

    public ProfileController(UserService userService, AddressService addressService) {
        this.userService = userService;
        this.addressService = addressService;
    }

    /* ===================== USER PROFILE ===================== */

    @GetMapping
    public ResponseEntity<User> getProfile(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(user);
    }

    /* ===================== ADDRESSES ===================== */

    @GetMapping("/addresses")
    public ResponseEntity<List<Address>> getAddresses(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                addressService.getAddressesByUserId(user.getId())
        );
    }

    @PostMapping("/addresses")
    public ResponseEntity<Address> createAddress(
            @Valid @RequestBody Address address,
            Authentication authentication) {

        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        address.setUserId(user.getId());
        Address saved = addressService.saveAddress(address);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/addresses/{id}")
    public ResponseEntity<Address> getAddressById(@PathVariable Long id) {
        Address address = addressService.getAddressById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        return ResponseEntity.ok(address);
    }

    @PutMapping("/addresses/{id}")
    public ResponseEntity<Address> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody Address address) {

        address.setId(id);
        Address updated = addressService.saveAddress(address);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }
}
