package com.example.frontrest.controller;

import com.example.frontrest.models.LoginRequest;
import com.example.frontrest.models.LoginResponse;
import com.example.frontrest.security.JWTService;
import com.example.user.dto.UserDto;
import com.example.user.dto.UserRegistrationDto;
import com.example.user.mapper.UserMapper;
import com.example.user.model.User;
import com.example.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserService userService;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    
    public AuthController(UserService userService,
                         UserMapper userMapper,
                         AuthenticationManager authenticationManager,
                         JWTService jwtService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }
    
    /* ===================== LOGIN ===================== */
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Authentifier l'utilisateur
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            
            // Récupérer l'utilisateur
            User user = userService.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Générer le token JWT
            String token = jwtService.generateToken(user.getEmail(), user.getId());
            
            // Convertir en DTO (sans le mot de passe)
            UserDto userDto = userMapper.toDto(user);
            
            
            return ResponseEntity.ok().header("Authorization", "Bearer " + token).body(userDto);
            
        } catch (AuthenticationException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Email ou mot de passe incorrect");
        }
    }
    
    /* ===================== REGISTER ===================== */
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
        try {
            User user = userService.registerUser(registrationDto);
            
            // Générer un token pour l'utilisateur nouvellement créé
            String token = jwtService.generateToken(user.getEmail(), user.getId());
            UserDto userDto = userMapper.toDto(user);
            
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .header("Authorization", "Bearer " + token)
                    .body(userDto);
                    
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
    
    /* ===================== VERIFY TOKEN ===================== */
    
    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            UserDto userDto = userMapper.toDto(user);
            return ResponseEntity.ok(userDto);
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide");
    }
}