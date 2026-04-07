package com.example.studyai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for authentication endpoints
 * Sent after successful login/signup with JWT token and user info
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String jwtToken;
    private UUID userId;
    private String email;
    private String displayName;
    private long expiresIn; // Token expiration time in milliseconds
    private String tokenType; // Usually "Bearer"

    public static AuthResponse of(String jwtToken, UUID userId, String email, String displayName, long expiresIn) {
        return AuthResponse.builder()
                .jwtToken(jwtToken)
                .userId(userId)
                .email(email)
                .displayName(displayName)
                .expiresIn(expiresIn)
                .tokenType("Bearer")
                .build();
    }
}
