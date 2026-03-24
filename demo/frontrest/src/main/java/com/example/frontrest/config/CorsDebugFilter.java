package com.example.frontrest.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsDebugFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        String origin = request.getHeader("Origin");
        String method = request.getMethod();
        
        System.out.println("🌐 CORS Request:");
        System.out.println("  Method: " + method);
        System.out.println("  Origin: " + origin);
        System.out.println("  URI: " + request.getRequestURI());
        
        // Laisser Spring Security gérer CORS
        chain.doFilter(req, res);
        
        System.out.println("📤 CORS Response:");
        System.out.println("  Status: " + response.getStatus());
        System.out.println("  Access-Control-Allow-Origin: " + response.getHeader("Access-Control-Allow-Origin"));
        System.out.println("  Access-Control-Allow-Methods: " + response.getHeader("Access-Control-Allow-Methods"));
    }
}