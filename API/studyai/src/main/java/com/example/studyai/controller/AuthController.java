package com.example.studyai.controller;

import com.example.studyai.dto.LoginRequest;
import com.example.studyai.dto.SignupRequest;
import com.example.studyai.dto.AuthResponse;
import com.example.studyai.service.AuthService;
import com.example.studyai.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for authentication operations
 * Handles user login and signup with email/password
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    /**
     * Login with email and password
     * Authenticates user and returns JWT token for API access
     *
     * @param loginRequest Request containing email and password
     * @return Response with JWT token and user information
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Login attempt with email: {}", loginRequest.getEmail());

            if (loginRequest.getEmail() == null || loginRequest.getEmail().isEmpty() ||
                    loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
                logger.warn("Login attempt with empty email or password");
                return ResponseEntity
                        .badRequest()
                        .body(new ApiResponse(false, "Email and password are required"));
            }

            // Authenticate with email and password
            AuthResponse authResponse = authService.authenticateUser(loginRequest.getEmail(),
                    loginRequest.getPassword());

            logger.info("User logged in successfully: {}", authResponse.getEmail());

            return ResponseEntity.ok(new ApiResponse(true, "Login successful", authResponse));

        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument in login: {}", e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(new ApiResponse(false, e.getMessage()));

        } catch (Exception e) {
            logger.error("Unexpected error during login", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Login failed: " + e.getMessage()));
        }
    }

    /**
     * Signup with email and password
     * Creates new user or returns error if email already registered
     *
     * @param signupRequest Request containing email, password, and optional displayName
     * @return Response with JWT token and new user information
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {
        try {
            logger.info("Signup attempt with email: {}", signupRequest.getEmail());

            if (signupRequest.getEmail() == null || signupRequest.getEmail().isEmpty() ||
                    signupRequest.getPassword() == null || signupRequest.getPassword().isEmpty()) {
                logger.warn("Signup attempt with empty email or password");
                return ResponseEntity
                        .badRequest()
                        .body(new ApiResponse(false, "Email and password are required"));
            }

            // Register new user with email and password
            AuthResponse authResponse = authService.registerUser(
                    signupRequest.getEmail(),
                    signupRequest.getPassword(),
                    signupRequest.getDisplayName());

            logger.info("User registered successfully: {}", authResponse.getEmail());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Signup successful", authResponse));

        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument in signup: {}", e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(new ApiResponse(false, e.getMessage()));

        } catch (Exception e) {
            logger.error("Unexpected error during signup", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Signup failed: " + e.getMessage()));
        }
    }

    /**
     * Verify JWT token validity
     * Used by frontend to check if current JWT token is still valid
     *
     * @param authHeader Authorization header with "Bearer <token>"
     * @return Response indicating token validity
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity
                        .badRequest()
                        .body(new ApiResponse(false, "Invalid Authorization header"));
            }

            String token = authHeader.substring(7);
            boolean isValid = authService.verifyJwtToken(token);

            if (isValid) {
                return ResponseEntity.ok(new ApiResponse(true, "Token is valid"));
            } else {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "Token is invalid or expired"));
            }

        } catch (Exception e) {
            logger.error("Error verifying token", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error verifying token: " + e.getMessage()));
        }
    }
}
