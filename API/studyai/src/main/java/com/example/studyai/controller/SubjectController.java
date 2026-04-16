package com.example.studyai.controller;

import com.example.studyai.dto.SubjectDTO;
import com.example.studyai.model.Subject;
import com.example.studyai.model.User;
import com.example.studyai.service.SubjectService;
import com.example.studyai.service.UserService;
import com.example.studyai.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private static final Logger logger = LoggerFactory.getLogger(SubjectController.class);

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> createSubject(@RequestBody Map<String, Object> request) {
        try {
            // Extract and validate required fields
            Object userIdObj = request.get("userId");
            String name = (String) request.get("name");
            String description = (String) request.get("description");

            if (userIdObj == null) {
                logger.error("Missing userId in request body");
                return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, "User ID is required", null)
                );
            }

            if (name == null || name.trim().isEmpty()) {
                logger.error("Missing or empty name in request body");
                return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, "Subject name is required", null)
                );
            }

            UUID userId;
            try {
                userId = UUID.fromString((String) userIdObj);
            } catch (IllegalArgumentException e) {
                logger.error("Invalid UUID format for userId: {}", userIdObj);
                return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, "Invalid user ID format", null)
                );
            }

            User user = userService.getUserById(userId);
            if (user == null) {
                logger.error("User not found: {}", userId);
                return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, "User not found", null)
                );
            }

            logger.debug("Creating subject - userId: {}, name: {}, description: {}", userId, name, description);
            Subject subject = subjectService.createSubject(user, name, description);
            
            logger.info("Subject created successfully - id: {}, userId: {}", subject.getId(), userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Subject created successfully", subjectService.convertToDTO(subject)));

        } catch (Exception e) {
            logger.error("Error creating subject: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                new ApiResponse<>(false, "Error creating subject: " + e.getMessage(), null)
            );
        }
    }

    // IMPORTANT: Put /user/{userId} BEFORE /{id} to avoid path variable conflict
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserSubjects(@PathVariable String userId) {
        try {
            logger.info("Attempting to fetch subjects for userId: {}", userId);

            // Validate that userId is in UUID format
            UUID userUuid;
            try {
                userUuid = UUID.fromString(userId);
            } catch (IllegalArgumentException e) {
                logger.error("Invalid UUID format for userId: '{}'. Expected format: 550e8400-e29b-41d4-a716-446655440000", userId);
                return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, "Invalid user ID format. Expected UUID format (e.g., 550e8400-e29b-41d4-a716-446655440000), received: " + userId, null)
                );
            }

            List<SubjectDTO> subjects = subjectService.getUserSubjectsDTO(userUuid);
            logger.info("Successfully retrieved {} subjects for user: {}", subjects.size(), userUuid);
            return ResponseEntity.ok(new ApiResponse<>(true, "Subjects retrieved successfully", subjects));
        } catch (Exception e) {
            logger.error("Error retrieving user subjects: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ApiResponse<>(false, "Error retrieving subjects: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSubject(@PathVariable String id) {
        try {
            UUID subjectId = UUID.fromString(id);
            SubjectDTO subjectDTO = subjectService.getSubjectByIdDTO(subjectId);
            if (subjectDTO == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "Subject retrieved successfully", subjectDTO));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format for subject id: {}", id);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid subject ID format", null));
        } catch (Exception e) {
            logger.error("Error retrieving subject: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ApiResponse<>(false, "Error retrieving subject", null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubject(@PathVariable String id, @RequestBody Map<String, String> request) {
        try {
            UUID subjectId = UUID.fromString(id);
            String name = request.get("name");
            String description = request.get("description");
            Subject subject = subjectService.updateSubject(subjectId, name, description);
            return ResponseEntity.ok(new ApiResponse<>(true, "Subject updated successfully", subjectService.convertToDTO(subject)));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format for subject id: {}", id);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid subject ID format", null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubject(@PathVariable String id) {
        try {
            UUID subjectId = UUID.fromString(id);
            subjectService.deleteSubject(subjectId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format for subject id: {}", id);
            return ResponseEntity.badRequest().body(null);
        }
    }
}
