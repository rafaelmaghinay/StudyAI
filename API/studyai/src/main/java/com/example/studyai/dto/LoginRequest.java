package com.example.studyai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for login endpoint
 * Contains only email and password for authentication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    private String email; // User email
    private String password; // User password
}
