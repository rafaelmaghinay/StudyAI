package com.example.studyai.service;

import com.example.studyai.dto.AuthResponse;
import com.example.studyai.model.User;
import com.example.studyai.util.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for handling authentication operations
 * Manages email/password authentication and JWT token generation
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Register a new user with email and password
     *
     * @param email       Email address
     * @param password    Plain text password (will be hashed)
     * @param displayName Display name (optional)
     * @return AuthResponse with JWT token and user info
     */
    @Transactional
    public AuthResponse registerUser(String email, String password, String displayName) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        logger.info("Attempting to register user: {}", email);

        // Check if user already exists
        Optional<User> existingUser = userService.findByEmail(email);
        if (existingUser.isPresent()) {
            logger.warn("Registration failed - user already exists: {}", email);
            throw new IllegalArgumentException("Email already registered");
        }

        // Hash password
        String passwordHash = passwordEncoder.encode(password);

        // Create new user
        User newUser = userService.createUser(email, displayName != null ? displayName : "", passwordHash);

        logger.info("User registered successfully: {} ({})", email, newUser.getId());

        // Generate JWT token
        String jwtToken = jwtTokenProvider.generateToken(newUser.getId(), newUser.getEmail());
        long expirationTime = jwtTokenProvider.getExpirationTime();

        return AuthResponse.of(
                jwtToken,
                newUser.getId(),
                newUser.getEmail(),
                newUser.getDisplayName(),
                expirationTime);
    }

    /**
     * Authenticate user with email and password
     *
     * @param email    Email address
     * @param password Plain text password
     * @return AuthResponse with JWT token and user info
     */
    @Transactional(readOnly = true)
    public AuthResponse authenticateUser(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        logger.info("Attempting to authenticate user: {}", email);

        // Find user by email
        Optional<User> userOptional = userService.findByEmail(email);
        if (userOptional.isEmpty()) {
            logger.warn("Authentication failed - user not found: {}", email);
            throw new IllegalArgumentException("Invalid email or password");
        }

        User user = userOptional.get();

        // Verify password
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            logger.warn("Authentication failed - invalid password for user: {}", email);
            throw new IllegalArgumentException("Invalid email or password");
        }

        logger.info("User authenticated successfully: {} ({})", email, user.getId());

        // Generate JWT token
        String jwtToken = jwtTokenProvider.generateToken(user.getId(), user.getEmail());
        long expirationTime = jwtTokenProvider.getExpirationTime();

        return AuthResponse.of(
                jwtToken,
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                expirationTime);
    }

    /**
     * Verify JWT token validity
     *
     * @param jwtToken JWT token to verify
     * @return true if valid, false otherwise
     */
    public boolean verifyJwtToken(String jwtToken) {
        return jwtTokenProvider.validateToken(jwtToken);
    }

    /**
     * Extract userId from JWT token
     *
     * @param jwtToken JWT token
     * @return User ID as UUID
     */
    public UUID getUserIdFromToken(String jwtToken) {
        return jwtTokenProvider.getUserIdFromToken(jwtToken);
    }

    /**
     * Extract email from JWT token
     *
     * @param jwtToken JWT token
     * @return User email
     */
    public String getEmailFromToken(String jwtToken) {
        return jwtTokenProvider.getEmailFromToken(jwtToken);
    }

    /**
     * Login with email and password
     * Validates existing user's password, returns JWT token
     * 
     * @param email    User email
     * @param password User password
     * @return AuthResponse with JWT token and user info
     * @throws IllegalArgumentException if user not found or password is invalid
     */
    public AuthResponse authenticateWithEmailPassword(String email, String password) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Email and password cannot be empty");
        }

        logger.info("Authenticating user with email: {}", email);

        // Find user by email
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            logger.warn("Authentication failed: user not found with email: {}", email);
            throw new IllegalArgumentException("Invalid email or password");
        }

        User user = userOpt.get();

        // Verify password
        if (user.getPasswordHash() == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            logger.warn("Authentication failed: invalid password for user: {}", email);
            throw new IllegalArgumentException("Invalid email or password");
        }

        logger.info("Password verified for user: {}", email);

        // Generate JWT token
        String jwtToken = jwtTokenProvider.generateToken(user.getId(), user.getEmail());
        long expirationTime = jwtTokenProvider.getExpirationTime();

        logger.info("User authenticated successfully: {} ({})", user.getEmail(), user.getId());

        // Create response
        return AuthResponse.of(
                jwtToken,
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                expirationTime);
    }

    /**
     * Register new user with email, password and name
     * Validates that email is not already taken, hashes password, creates user
     * 
     * @param email    User email
     * @param password User password (will be hashed with bcrypt)
     * @param name     User display name
     * @return AuthResponse with JWT token and user info
     * @throws IllegalArgumentException if email already taken or invalid input
     */
    public AuthResponse registerWithEmailPassword(String email, String password, String name) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty() || name == null
                || name.isEmpty()) {
            throw new IllegalArgumentException("Email, password, and name cannot be empty");
        }

        logger.info("Registering new user with email: {} and name: {}", email, name);

        // Check if email is already taken
        Optional<User> existingUser = userService.findByEmail(email);
        if (existingUser.isPresent()) {
            logger.warn("Signup failed: email already registered: {}", email);
            throw new IllegalArgumentException("Email is already registered");
        }

        // Hash password
        String passwordHash = passwordEncoder.encode(password);
        logger.debug("Password hashed for new user: {}", email);

        // Create new user with hashed password
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setDisplayName(name);
        newUser.setPasswordHash(passwordHash);

        User savedUser = userService.saveUser(newUser);

        logger.info("User registered successfully: {} ({})", savedUser.getEmail(), savedUser.getId());

        // Generate JWT token
        String jwtToken = jwtTokenProvider.generateToken(savedUser.getId(), savedUser.getEmail());
        long expirationTime = jwtTokenProvider.getExpirationTime();

        // Create response
        return AuthResponse.of(
                jwtToken,
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getDisplayName(),
                expirationTime);
    }
}
