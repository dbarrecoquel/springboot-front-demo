package com.example.backoffice.config;

import com.example.user.model.User;
import com.example.user.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final UserRepository userRepository;
    
    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
                .anyRequest().hasRole("ADMIN")
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/products", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        
        return http.build();
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            System.out.println("=== TENTATIVE DE LOGIN BACKOFFICE ===");
            System.out.println("Email saisi : [" + email + "]");

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        System.out.println("❌ ÉCHEC : Aucun utilisateur trouvé avec cet email.");
                        return new UsernameNotFoundException("User not found: " + email);
                    });

            String roleBDD = user.getRole() != null ? user.getRole().trim() : "";
            System.out.println("Utilisateur trouvé ! Rôle exact en BDD : [" + roleBDD + "]");
            System.out.println("Statut Enabled en BDD : " + user.getEnabled());

            // Comparaison insensible à la casse et nettoyée des espaces
            if (!"ROLE_ADMIN".equalsIgnoreCase(roleBDD)) {
                System.out.println("❌ ÉCHEC : Le rôle '" + roleBDD + "' n'est pas égal à ROLE_ADMIN.");
                throw new UsernameNotFoundException("User is not an admin");
            }

            System.out.println("✅ SUCCÈS : Rôle validé, envoi à Spring Security.");
            
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPassword())
                    .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority(roleBDD))
                    .disabled(!user.getEnabled())
                    .build();
        };
    }
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        
        // On lui associe TON encodeur de mot de passe
        authProvider.setPasswordEncoder(passwordEncoder());
        
        return authProvider;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}