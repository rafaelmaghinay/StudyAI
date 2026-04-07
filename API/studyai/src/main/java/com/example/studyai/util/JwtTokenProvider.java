package com.example.studyai.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * JWT Token provider for custom token generation and validation
 * Uses Base64-encoded custom tokens in format: userId:email:timestamp
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwtExpirationMs:86400000}")
    private long jwtExpirationMs;

    /**
     * Generate custom JWT token for user
     * Creates a Base64-encoded token in format: userId:email:timestamp
     *
     * @param userId The user's UUID
     * @param email  The user's email
     * @return Custom JWT token string
     */
    public String generateToken(UUID userId, String email) {
        try {
            // Create custom token with userId, email, and current timestamp
            String tokenData = userId.toString() + ":" + email + ":" + System.currentTimeMillis();
            String encodedToken = java.util.Base64.getEncoder().encodeToString(tokenData.getBytes());
            logger.debug("Token generated successfully for user: {}", userId);
            return encodedToken;
        } catch (Exception e) {
            logger.error("Failed to generate token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Validate custom JWT token (Base64 encoded format: userId:email:timestamp)
     * This system uses custom tokens, not Firebase tokens
     *
     * @param token The JWT token string
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            byte[] decoded = java.util.Base64.getDecoder().decode(token);
            String[] parts = new String(decoded).split(":");
            // Valid token must have userId, email, and timestamp
            if (parts.length < 3) {
                logger.debug("Invalid token format: expected 3 parts, got {}", parts.length);
                return false;
            }
            // Verify userId is a valid UUID
            UUID.fromString(parts[0]);
            return true;
        } catch (IllegalArgumentException e) {
            logger.debug("Invalid token: invalid UUID format - {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract userId from custom JWT token (format: userId:email:timestamp)
     *
     * @param token The custom JWT token string
     * @return User ID as UUID, or null if invalid
     */
    public UUID getUserIdFromToken(String token) {
        try {
            byte[] decoded = java.util.Base64.getDecoder().decode(token);
            String[] parts = new String(decoded).split(":");
            if (parts.length > 0) {
                UUID userId = UUID.fromString(parts[0]);
                logger.debug("Successfully extracted userId from token");
                return userId;
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid UUID in token: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to extract userId from custom token: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Extract email from custom JWT token (format: userId:email:timestamp)
     *
     * @param token The custom JWT token string
     * @return Email, or null if invalid
     */
    public String getEmailFromToken(String token) {
        try {
            byte[] decoded = java.util.Base64.getDecoder().decode(token);
            String[] parts = new String(decoded).split(":");
            if (parts.length > 1) {
                logger.debug("Successfully extracted email from token");
                return parts[1];
            }
        } catch (Exception e) {
            logger.error("Failed to extract email from custom token: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Get token expiration time in milliseconds
     *
     * @return Expiration time
     */
    public long getExpirationTime() {
        return jwtExpirationMs;
    }
}
