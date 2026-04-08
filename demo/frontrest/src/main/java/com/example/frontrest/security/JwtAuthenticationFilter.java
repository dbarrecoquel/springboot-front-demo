package com.example.frontrest.security;

import com.example.user.model.User;
import com.example.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.context.annotation.Lazy;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JWTService jwtService;
    private final UserService userService;
    
    public JwtAuthenticationFilter(JWTService jwtService,@Lazy UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        
        final String requestURI = request.getRequestURI();
        final String authHeader = request.getHeader("Authorization");
        
        logger.debug("🔍 JWT Filter processing: {} {}", request.getMethod(), requestURI);
        logger.debug("   Authorization header: {}", authHeader != null ? "Bearer ..." : "null");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("No Bearer token found, continuing without authentication");
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            final String jwt = authHeader.substring(7);
            logger.debug("Token extracted: {}...", jwt.substring(0, Math.min(jwt.length(), 20)));
            
            final String email = jwtService.extractEmail(jwt);
            logger.debug("Email extracted from token: {}", email);
            
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                logger.debug("Looking up user: {}", email);
                
                Optional<User> userOptional = userService.findByEmail(email);
                
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    logger.debug("User found: {} (role: {})", email, user.getRole());
                    
                    if (jwtService.validateToken(jwt, email)) {
                        logger.debug("Token is valid");
                        
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        email,
                                        null,
                                        Collections.singletonList(
                                                new SimpleGrantedAuthority(user.getRole())
                                        )
                                );
                        
                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );
                        
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        
                        logger.info("User authenticated: {} with role: {}", email, user.getRole());
                    } else {
                        logger.warn("Token validation failed for: {}", email);
                    }
                } else {
                    logger.warn("User not found: {}", email);
                }
            } else {
                if (email == null) {
                    logger.warn("Could not extract email from token");
                } else {
                    logger.debug("User already authenticated");
                }
            }
            
        } catch (Exception e) {
            logger.error("JWT authentication error: {}", e.getMessage(), e);
        }
        
        filterChain.doFilter(request, response);
    }
}