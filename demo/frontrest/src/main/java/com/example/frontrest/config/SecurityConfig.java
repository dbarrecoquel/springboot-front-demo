package com.example.frontrest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Désactiver CSRF pour l'API REST
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
            )
            
            // CORS - IMPORTANT : doit être avant authorizeHttpRequests
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configuration des autorisations
            .authorizeHttpRequests(auth -> auth
                // Endpoints publics
                .requestMatchers(
                    "/",
                    "/login",
                    "/register",
                    "/api/**",  // Simplifier : tout /api/** est public par défaut
                    "/catalog/**",
                    "/products/**",
                    "/basket/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/error"
                ).permitAll()
                
                // Endpoints privés
                .requestMatchers("/api/profile/**", "/profile/**").authenticated()
                .requestMatchers("/api/checkout/**", "/checkout/**").authenticated()
                
                // Tout le reste
                .anyRequest().permitAll()
            )
            
            // Sessions IF_REQUIRED
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            
            // Formulaire de login
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            );
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // IMPORTANT : Utiliser setAllowedOriginPatterns au lieu de setAllowedOrigins
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
        ));
        
        configuration.setAllowedHeaders(Arrays.asList(
            "*"
        ));
        
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Content-Disposition"
        ));
        
        // Autoriser les credentials (cookies)
        configuration.setAllowCredentials(true);
        
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // Appliquer à TOUS les endpoints
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("http://localhost:4200")
                    .allowedMethods("*")
                    .allowCredentials(true);
            }
        };
    }
}