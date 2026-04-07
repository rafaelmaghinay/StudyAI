package com.example.studyai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for signup endpoint
 * Contains email, password, and optional displayName for user registration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    private String email; // User email address
    private String password; // User password (will be hashed)
    private String displayName; // Optional user display name
}
