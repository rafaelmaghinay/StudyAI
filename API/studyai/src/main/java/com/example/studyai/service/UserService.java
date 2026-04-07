package com.example.studyai.service;

import com.example.studyai.dto.UserDTO;
import com.example.studyai.model.User;
import com.example.studyai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new user with email and password
     */
    public User createUser(String email, String displayName, String passwordHash) {
        User user = new User();
        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setPasswordHash(passwordHash);
        return userRepository.save(user);
    }

    /**
     * Find user by ID, throws exception if not found
     */
    public User getUserById(UUID id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found with id: " + id);
        }
        return user.get();
    }

    /**
     * Find user by email, returns Optional
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Save or update user
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Find user by email, returns Optional
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find or create user by email
     * Used for email/password authentication
     *
     * @param email       User email
     * @param displayName Display name (optional)
     * @param passwordHash Hashed password
     * @return User object (existing or newly created)
     */
    public User findOrCreateByEmail(String email, String displayName, String passwordHash) {
        logger.debug("Looking up user by email: {}", email);

        // Try to find existing user
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            logger.info("User found: {}", email);
            return existingUser.get();
        }

        // Create new user if doesn't exist
        logger.info("Creating new user: {}", email);
        User newUser = createUser(email, displayName, passwordHash);
        logger.info("New user created: {} ({})", email, newUser.getId());

        return newUser;
    }

    /**
     * Update user display name
     */
    public User updateUser(UUID id, String displayName) {
        User user = getUserById(id);
        user.setDisplayName(displayName);
        return userRepository.save(user);
    }

    /**
     * Convert User entity to DTO
     */
    public UserDTO convertToDTO(User user) {
        return new UserDTO(user.getId(), user.getEmail(), user.getDisplayName(), user.getCreatedAt());
    }

    /**
     * Find or create user by email
     * Used for email/password authentication
     *
     * @param email User email
     * @return User object (existing or newly created)
     */
    public User findOrCreateByEmail(String email) {
        logger.debug("Looking up user by email: {}", email);

        // Try to find existing user
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            logger.info("User found: {}", email);
            return existingUser.get();
        }

        // Create new user if doesn't exist
        logger.info("Creating new user with email: {}", email);
        String displayName = email.split("@")[0]; // Use part before @ as display name
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setDisplayName(displayName);
        // Generate synthetic firebaseUid for email/password users (prefix with "local:"
        // to distinguish from Firebase UIDs)

        User savedUser = userRepository.save(newUser);
        logger.info("New user created: {} ({})", email, savedUser.getId());

        return savedUser;
    }

    /**
     * Find or create user by email with explicit display name
     * Used for signup with email/password
     *
     * @param email       User email
     * @param displayName User display name
     * @return User object (existing or newly created)
     */
    public User findOrCreateByEmailWithName(String email, String displayName) {
        logger.debug("Looking up user by email: {}", email);

        // Try to find existing user
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            logger.info("User found: {}", email);
            User user = existingUser.get();
            // Update display name if provided
            if (displayName != null && !displayName.isEmpty() &&
                    (user.getDisplayName() == null || user.getDisplayName().isEmpty())) {
                user.setDisplayName(displayName);
                userRepository.save(user);
            }
            return user;
        }

        // Create new user if doesn't exist
        logger.info("Creating new user with email: {} and name: {}", email, displayName);
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setDisplayName(displayName != null ? displayName : email.split("@")[0]);
        // Generate synthetic firebaseUid for email/password users (prefix with "local:"
        // to distinguish from Firebase UIDs)
        User savedUser = userRepository.save(newUser);
        logger.info("New user created: {} ({})", email, savedUser.getId());

        return savedUser;
    }
}
