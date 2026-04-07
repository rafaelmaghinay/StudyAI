package com.example.studyai.config;

import com.example.studyai.util.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Authentication filter for JWT tokens
 * Extracts JWT from Authorization header and validates it
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Paths that should skip authentication
     */
    private static final String[] ALLOWED_PATHS = {
            "/api/auth/signup",
            "/api/auth/login",
            "/health",
            "/status",
            "/api/docs",
            "/swagger-ui",
            "/v3/api-docs"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Handle CORS preflight requests (OPTIONS) - bypass all authentication
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                logger.info("✅ CORS preflight OPTIONS request: {} - allowing through", request.getRequestURI());
                response.setStatus(200); // OK
                filterChain.doFilter(request, response);
                return;
            }

            // Check if this is an allowed path
            String requestPath = request.getRequestURI();
            if (isAllowedPath(requestPath)) {
                logger.debug("Skipping authentication for allowed path: {}", requestPath);
                filterChain.doFilter(request, response);
                return;
            }

            // Extract JWT from Authorization header
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                logger.warn("Missing or invalid Authorization header for path: {}", requestPath);
                filterChain.doFilter(request, response);
                return;
            }

            // Extract token from "Bearer <token>"
            String token = authorizationHeader.substring(7);

            // Validate JWT token
            if (jwtTokenProvider.validateToken(token)) {
                UUID userId = jwtTokenProvider.getUserIdFromToken(token);

                if (userId != null) {
                    // Create authentication token
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId.toString(),
                            null,
                            new ArrayList<>());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set authentication in Spring Security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("JWT authentication successful for user: {}", userId);
                } else {
                    logger.warn("Failed to extract userId from JWT token");
                }
            } else {
                logger.warn("Invalid JWT token for path: {}", requestPath);
            }

        } catch (Exception ex) {
            logger.error("Error processing JWT authentication: {}", ex.getMessage(), ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Check if the request path is allowed without authentication
     */
    private boolean isAllowedPath(String path) {
        for (String allowedPath : ALLOWED_PATHS) {
            if (path.startsWith(allowedPath)) {
                return true;
            }
        }
        return false;
    }
}
