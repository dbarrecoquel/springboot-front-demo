package com.example.user.mapper;

import com.example.user.dto.UserDto;
import com.example.user.dto.UserRegistrationDto;
import com.example.user.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    
    /**
     * Convertit une entité User en UserDto
     * ⚠️ N'inclut PAS le mot de passe (sécurité)
     */
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setEnabled(user.getEnabled());
        dto.setRole(user.getRole());
        dto.setFullName(user.getFullName()); // Méthode calculée
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        return dto;
    }
    
    /**
     * Convertit un UserDto en entité User
     * ⚠️ N'inclut PAS le mot de passe (ne doit pas être mis à jour via DTO)
     */
    public User toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }
        
        User user = new User();
        user.setId(dto.getId());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhone(dto.getPhone());
        user.setEnabled(dto.getEnabled());
        user.setRole(dto.getRole());
        // password n'est pas mappé ici pour la sécurité
        
        return user;
    }
    
    /**
     * Convertit un UserRegistrationDto en entité User
     * Utilisé lors de l'inscription
     */
    public User registrationDtoToEntity(UserRegistrationDto registrationDto) {
        if (registrationDto == null) {
            return null;
        }
        
        User user = new User();
        user.setEmail(registrationDto.getEmail());
        user.setPassword(registrationDto.getPassword()); // Sera hashé par le service
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setPhone(registrationDto.getPhone());
        user.setEnabled(true);
        user.setRole("ROLE_USER");
        
        return user;
    }
    
    /**
     * Met à jour une entité User existante avec les données d'un UserDto
     * ⚠️ Préserve l'ID, le mot de passe, et les dates
     */
    public void updateEntityFromDto(UserDto dto, User user) {
        if (dto == null || user == null) {
            return;
        }
        
        // Ne pas modifier l'ID
        // Ne pas modifier le mot de passe ici
        
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getFirstName() != null) {
            user.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            user.setLastName(dto.getLastName());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getEnabled() != null) {
            user.setEnabled(dto.getEnabled());
        }
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }
        
        // Les dates sont gérées automatiquement par @PreUpdate
    }
    
    /**
     * Convertit une liste d'entités User en liste de UserDto
     */
    public List<UserDto> toDtoList(List<User> users) {
        if (users == null) {
            return null;
        }
        
        return users.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Convertit une liste de UserDto en liste d'entités User
     */
    public List<User> toEntityList(List<UserDto> dtos) {
        if (dtos == null) {
            return null;
        }
        
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}