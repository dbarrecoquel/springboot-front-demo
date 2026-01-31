package com.example.backoffice.config;

import com.example.user.model.User;
import com.example.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    
    @Bean
    public CommandLineRunner initAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Créer un admin par défaut s'il n'existe pas
            if (!userRepository.existsByEmail("admin@example.com")) {
                User admin = new User();
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setFirstName("Admin");
                admin.setLastName("System");
                admin.setRole("ROLE_ADMIN");
                admin.setEnabled(true);
                
                userRepository.save(admin);
                System.out.println("✅ Admin user created:");
                System.out.println("   Email: admin@example.com");
                System.out.println("   Password: admin123");
            }
        };
    }
}