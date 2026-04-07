package com.example.studyai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for authentication endpoints
 * Used for login and signup with email/password authentication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    private String email; // User email
    private String password; // User password
    private String displayName; // For signup with email/password
}
