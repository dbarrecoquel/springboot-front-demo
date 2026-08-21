package com.example.frontoffice.config;

import com.example.shopping.model.Basket;
import com.example.shopping.service.BasketService;
import com.example.user.model.User;
import com.example.user.repository.UserRepository;
import com.example.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {
    
    private final UserRepository userRepository;
    private final BasketService basketService;
    private final UserService userService;
    
    public SecurityConfig(UserRepository userRepository,
                         BasketService basketService,
                         @Lazy UserService userService) {
        this.userRepository = userRepository;
        this.basketService = basketService;
        this.userService = userService;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/category/**", "/catalog", "/catalog/**", 
                               "/register", "/login", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/basket/**").permitAll()
                .requestMatchers("/profile/**", "/addresses/**").authenticated()
                .requestMatchers("/checkout/**").authenticated()
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("username") // Lie explicitement le name="username" du HTML
                .passwordParameter("password")
                .successHandler(authenticationSuccessHandler())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionFixation().none() // Ne pas changer le session ID
            )
            .csrf(csrf -> csrf
                    .csrfTokenRepository(
                        new org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository()
                    )
                );
        
        return http.build();
    }
    
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            log.info("AuthenticationSuccessHandler déclenché");

            String email = authentication.getName();
            log.info("Email utilisateur: {}", email);

            try {
                User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
                
                log.info("Utilisateur trouvé: {} (ID: {})", user.getFullName(), user.getId());

                HttpSession session = request.getSession();
                String sessionId = session.getId();
                log.info("Session ID: {}", sessionId);

                // ✅ FUSIONNER LES PANIERS
                Basket mergedBasket = basketService.mergeBaskets(user.getId(), sessionId);
                
                session.setAttribute("message", "Bienvenue " + user.getFirstName() + " !");
                log.info("Panier fusionné pour l'utilisateur: {} | Panier ID: {}", user.getEmail(), mergedBasket.getId());

            } catch (Exception e) {
                log.error("Erreur lors de la fusion du panier: {}", e.getMessage());
                e.printStackTrace();
            }

            response.sendRedirect("/");
        };
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
        	System.out.println("Tentative de chargement de l'utilisateur avec l'email : " + email);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
            System.out.println("Utilisateur trouvé en BDD ! Nom: " + user.getLastName());
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPassword())
                    .roles(user.getRole().replace("ROLE_", ""))
                    .disabled(!user.getEnabled())
                    .build();
        };
    }
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        
        authProvider.setPasswordEncoder(passwordEncoder());
        
        return authProvider;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}