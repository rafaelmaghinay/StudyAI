package com.example.studyai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Simple CORS filter that adds CORS headers to ALL responses (including errors)
 * This filter runs at the servlet level, before Spring Security
 * Ensures that CORS headers are present even when a 403 or other error is returned
 */
// @Component  // DISABLED - Using SecurityConfig's built-in CORS configuration instead
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SimpleCorsFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(SimpleCorsFilter.class);

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://localhost:8080}")
    private String allowedOrigins;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String origin = request.getHeader("Origin");
        
        if (origin != null && isOriginAllowed(origin)) {
            // Add CORS response headers
            response.addHeader("Access-Control-Allow-Origin", origin);
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD");
            response.addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Accept");
            response.addHeader("Access-Control-Expose-Headers", "Authorization, Content-Type, X-Total-Count");
            response.addHeader("Access-Control-Max-Age", "3600");
            response.addHeader("Access-Control-Allow-Credentials", "true");
            
            logger.debug("✅ CORS headers added for origin: {}", origin);
        }

        // For preflight OPTIONS requests, return 200 immediately
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            logger.info("✅ CORS preflight OPTIONS request to {} - responding with 200", request.getRequestURI());
            response.setStatus(200);
            return;
        }

        // Continue with the filter chain for other requests
        filterChain.doFilter(request, response);
    }

    /**
     * Check if the given origin is in the allowed origins list
     */
    private boolean isOriginAllowed(String origin) {
        String[] originArray = allowedOrigins.split(",");
        for (String allowedOrigin : originArray) {
            if (allowedOrigin.trim().equals(origin)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Apply this filter to all requests
        return false;
    }
}
