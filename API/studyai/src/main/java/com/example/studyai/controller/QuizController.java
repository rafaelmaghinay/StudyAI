package com.example.studyai.controller;

import com.example.studyai.dto.QuizDTO;
import com.example.studyai.exception.QuizGenerationException;
import com.example.studyai.model.Note;
import com.example.studyai.model.Quiz;
import com.example.studyai.model.User;
import com.example.studyai.repository.NoteRepository;
import com.example.studyai.service.QuizService;
import com.example.studyai.service.QuestionService;
import com.example.studyai.service.UserService;
import com.example.studyai.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for quiz management endpoints.
 * <p>
 * Exposes operations to create quizzes from user notes (via FastAPI),
 * retrieve quizzes and their questions, and update or delete existing
 * quizzes for a given user.
 */
@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private static final Logger logger = LoggerFactory.getLogger(QuizController.class);

    @Autowired
    private QuizService quizService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private UserService userService;

    @Autowired
    private NoteRepository noteRepository;

    /**
     * Create a new quiz and (optionally) generate questions from notes.
     *
     * @param request JSON body containing userId, title, noteIds, difficulty,
     *                questionCount and optional subjectId/description
     * @return API response wrapping the created quiz DTO or an error message
     */
    @PostMapping
    public ResponseEntity<?> createQuiz(@RequestBody Map<String, Object> request) {
        try {
            logger.debug("Create quiz request received: {}", request);

            // Get userId from request or from Spring Security context
            String userIdStr = (String) request.get("userId");
            if (userIdStr == null || userIdStr.isEmpty()) {
                // Try to extract from Security context (JWT token)
                var authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()) {
                    userIdStr = (String) authentication.getPrincipal();
                    logger.debug("Extracted userId from Security context: {}", userIdStr);
                } else {
                    logger.error("Missing userId parameter and no authentication found");
                    return ResponseEntity.badRequest()
                            .body(new ApiResponse<>(false, "User ID is required", null));
                }
            }

            if (request.get("title") == null || ((String) request.get("title")).isEmpty()) {
                logger.error("Missing title parameter");
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Quiz title is required", null));
            }

            if (request.get("noteIds") == null) {
                logger.error("Missing noteIds parameter");
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Note IDs are required", null));
            }

            UUID userId = UUID.fromString(userIdStr);
            String title = (String) request.get("title");
            String description = (String) request.getOrDefault("description", "");
            String difficulty = (String) request.getOrDefault("difficulty", "medium");
            Integer questionCount = request.get("questionCount") != null
                    ? ((Number) request.get("questionCount")).intValue()
                    : 0;

            // Extract optional subjectId
            UUID subjectId = null;
            if (request.get("subjectId") != null && !((String) request.get("subjectId")).isEmpty()) {
                try {
                    subjectId = UUID.fromString((String) request.get("subjectId"));
                    logger.debug("Subject ID extracted: {}", subjectId);
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid subject ID format: {}", request.get("subjectId"));
                    // Continue without subject ID - it's optional
                }
            }

            @SuppressWarnings("unchecked")
            List<String> noteIdStrings = (List<String>) request.get("noteIds");

            User user = userService.getUserById(userId);
            if (user == null) {
                logger.error("User not found: {}", userId);
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "User not found", null));
            }

            logger.debug("Looking for {} notes: {}", noteIdStrings.size(), noteIdStrings);
            List<Note> notes = noteIdStrings.stream()
                    .peek(noteId -> logger.debug("Processing note ID: {}", noteId))
                    .map(UUID::fromString)
                    .map(noteId -> {
                        var foundNote = noteRepository.findById(noteId);
                        if (foundNote.isPresent()) {
                            logger.debug("Found note: {}", noteId);
                        } else {
                            logger.warn("Note not found: {}", noteId);
                        }
                        return foundNote;
                    })
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            logger.info("Found {} notes out of {} requested", notes.size(), noteIdStrings.size());

            // Create quiz with questions (with transaction rollback on FastAPI failure)
            Quiz quiz = quizService.createQuizWithQuestions(user, title, description, difficulty, questionCount, notes,
                    subjectId);
            logger.info("Quiz created successfully: {}", quiz.getId());
            return ResponseEntity
                    .ok(new ApiResponse<>(true, "Quiz created successfully", quizService.convertToDTO(quiz)));

        } catch (QuizGenerationException e) {
            // FastAPI quiz generation failed - transaction rolled back
            logger.warn("Quiz creation rolled back due to FastAPI generation failure: {}", e.getMessage());
            return ResponseEntity.status(424) // 424 Failed Dependency
                    .body(new ApiResponse<>(false,
                            "Quiz generation failed - no quiz was created. " + e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Invalid UUID format: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error creating quiz: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(false, "Error creating quiz: " + e.getMessage(), null));
        }
    }

    // IMPORTANT: Put /user/{userId} BEFORE /{id} to avoid path variable conflict

    /**
     * Fetch all quizzes that belong to a specific user.
     *
     * @param userId user identifier as a UUID string
     * @return list of quiz DTOs owned by the user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserQuizzes(@PathVariable String userId) {
        try {
            UUID userUuid = UUID.fromString(userId);
            List<QuizDTO> quizzes = quizService.getUserQuizzesDTO(userUuid);
            return ResponseEntity.ok(new ApiResponse<>(true, "Quizzes retrieved successfully", quizzes));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format for userId: {}", userId);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid user ID format", null));
        }
    }

    /**
     * Retrieve all questions for a given quiz.
     *
     * @param id quiz identifier as a UUID string
     * @return API response containing question DTOs
     */
    @GetMapping("/{id}/questions")
    public ResponseEntity<?> getQuizQuestions(@PathVariable String id) {
        try {
            UUID quizId = UUID.fromString(id);
            List<?> questions = questionService.convertToDTOList(questionService.getQuestionsByQuizId(quizId));
            return ResponseEntity.ok(new ApiResponse<>(true, "Quiz questions retrieved successfully", questions));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format for quiz id: {}", id);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid quiz ID format", null));
        } catch (Exception e) {
            logger.error("Error retrieving quiz questions for quiz id: {}", id, e);
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(false, "Error retrieving quiz questions: " + e.getMessage(), null));
        }
    }

    /**
     * Get a single quiz by its identifier.
     *
     * @param id quiz identifier as a UUID string
     * @return API response with the quiz DTO or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getQuiz(@PathVariable String id) {
        try {
            UUID quizId = UUID.fromString(id);
            QuizDTO quiz = quizService.getQuizByIdDTO(quizId);
            if (quiz == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "Quiz retrieved successfully", quiz));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format for quiz id: {}", id);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid quiz ID format", null));
        }
    }

    /**
     * Update quiz metadata such as the title.
     *
     * @param id      quiz identifier as a UUID string
     * @param request request body containing the new title
     * @return API response with the updated quiz DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuiz(@PathVariable String id, @RequestBody Map<String, String> request) {
        try {
            UUID quizId = UUID.fromString(id);
            String title = request.get("title");
            Quiz quiz = quizService.updateQuiz(quizId, title);
            return ResponseEntity
                    .ok(new ApiResponse<>(true, "Quiz updated successfully", quizService.convertToDTO(quiz)));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format for quiz id: {}", id);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid quiz ID format", null));
        }
    }

    /**
     * Delete a quiz permanently.
     *
     * @param id quiz identifier as a UUID string
     * @return 204 No Content on success or 400 on invalid ID format
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable String id) {
        try {
            UUID quizId = UUID.fromString(id);
            quizService.deleteQuiz(quizId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format for quiz id: {}", id);
            return ResponseEntity.badRequest().body(null);
        }
    }
}
