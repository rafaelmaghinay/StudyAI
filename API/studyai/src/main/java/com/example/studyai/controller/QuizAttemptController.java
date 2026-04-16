package com.example.studyai.controller;

import com.example.studyai.dto.QuizAttemptDTO;
import com.example.studyai.dto.QuizSubmissionRequest;
import com.example.studyai.dto.UserAnswerDTO;
import com.example.studyai.model.Question;
import com.example.studyai.model.Quiz;
import com.example.studyai.model.QuizAttempt;
import com.example.studyai.model.User;
import com.example.studyai.service.QuestionService;
import com.example.studyai.service.QuizAttemptService;
import com.example.studyai.service.QuizService;
import com.example.studyai.service.UserAnswerService;
import com.example.studyai.service.UserService;
import com.example.studyai.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/quiz-attempts")
public class QuizAttemptController {

    private static final Logger logger = LoggerFactory.getLogger(QuizAttemptController.class);

    @Autowired
    private QuizAttemptService quizAttemptService;

    @Autowired
    private QuizService quizService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserAnswerService userAnswerService;

    @PostMapping
    public ResponseEntity<?> startQuizAttempt(@RequestBody Map<String, Object> request) {
        try {
            // Extract quiz ID from request
            UUID quizId = UUID.fromString((String) request.get("quizId"));

            // Extract user ID from JWT authentication context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                        .body(new ApiResponse<>(false, "User not authenticated", null));
            }

            UUID userId = UUID.fromString(authentication.getPrincipal().toString());

            // Get quiz
            Optional<Quiz> quiz = quizService.getQuizById(quizId);
            if (quiz.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Quiz not found", null));
            }

            // Validate quiz has questions
            int totalQuestions = quiz.get().getTotalQuestions();
            if (totalQuestions <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false,
                                "This quiz does not have any questions yet. Please generate questions before attempting.",
                                null));
            }

            // Get user
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "User not found", null));
            }

            QuizAttempt attempt = quizAttemptService.createQuizAttempt(quiz.get(), user, totalQuestions);
            return ResponseEntity
                    .ok(new ApiResponse<>(true, "Quiz attempt started", quizAttemptService.convertToDTO(attempt)));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Invalid request format: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to start quiz attempt: " + e.getMessage(), null));
        }
    }

    // IMPORTANT: Put /user/{userId} and /quiz/{quizId} BEFORE /{id} to avoid path
    // variable conflict
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserAttempts(@PathVariable String userId) {
        try {
            logger.debug("Fetching quiz attempts for user: {}", userId);
            UUID userUuid = UUID.fromString(userId);
            List<QuizAttempt> attempts = quizAttemptService.getUserAttempts(userUuid);
            logger.info("Retrieved {} quiz attempts for user: {}", attempts.size(), userId);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "User attempts retrieved", quizAttemptService.convertToDTOList(attempts)));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid user ID format: {}", userId);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid user ID format", null));
        } catch (Exception e) {
            logger.error("Error retrieving attempts for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(false, "Error retrieving attempts: " + e.getMessage(), null));
        }
    }

    @GetMapping("/quizzes/{quizId}")
    public ResponseEntity<?> getQuizAttempts(@PathVariable String quizId) {
        try {
            UUID quizUuid = UUID.fromString(quizId);
            List<QuizAttempt> attempts = quizAttemptService.getQuizAttempts(quizUuid);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Quiz attempts retrieved", quizAttemptService.convertToDTOList(attempts)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid quiz ID format", null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQuizAttempt(@PathVariable String id) {
        try {
            UUID attemptId = UUID.fromString(id);
            QuizAttemptDTO attempt = quizAttemptService.getQuizAttemptByIdDTO(attemptId);
            if (attempt == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Quiz attempt retrieved", attempt));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid attempt ID format: {}", id);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid attempt ID format", null));
        } catch (Exception e) {
            logger.error("Error retrieving quiz attempt {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(false, "Error retrieving attempt: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{id}/answers")
    public ResponseEntity<?> getAttemptAnswers(@PathVariable String id) {
        try {
            UUID attemptId = UUID.fromString(id);
            Optional<QuizAttempt> attempt = quizAttemptService.getQuizAttemptById(attemptId);
            if (attempt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            List<UserAnswerDTO> answers = userAnswerService.convertToDTOList(
                    userAnswerService.getAttemptAnswers(attemptId));
            return ResponseEntity.ok(new ApiResponse<>(true, "Attempt answers retrieved", answers));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid attempt ID format: {}", id);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid attempt ID format", null));
        } catch (Exception e) {
            logger.error("Error retrieving attempt answers for {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(false, "Error retrieving answers: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}/submit")
    public ResponseEntity<?> submitQuizAttempt(@PathVariable String id, @RequestBody QuizSubmissionRequest request) {
        try {
            UUID attemptId = UUID.fromString(id);

            // Validate answers list
            if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Answers list is required and cannot be empty", null));
            }

            // Get the quiz attempt
            Optional<QuizAttempt> attemptOptional = quizAttemptService.getQuizAttemptById(attemptId);
            if (attemptOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Quiz attempt not found", null));
            }

            QuizAttempt attempt = attemptOptional.get();
            int correctCount = 0;

            // Process each answer
            for (QuizSubmissionRequest.AnswerSubmission answerSubmission : request.getAnswers()) {
                UUID questionId = answerSubmission.getQuestionId();
                String rawAnswer = answerSubmission.getUserAnswer();

                // Extract just the letter from the answer (e.g., "B" from "B. FOR NONCOMMERCIAL
                // CLASSROOM USE.")
                String userAnswer = extractAnswerLetter(rawAnswer).toUpperCase();
                Integer timeSpent = answerSubmission.getTimeSpentSeconds();

                // Get the question
                Optional<Question> questionOptional = questionService.getQuestionById(questionId);
                if (questionOptional.isEmpty()) {
                    // Log and skip invalid question
                    logger.warn("Question not found: {}", questionId);
                    continue;
                }

                Question question = questionOptional.get();
                String correctAnswer = question.getCorrectAnswer().toUpperCase();

                // Determine if answer is correct
                boolean isCorrect = userAnswer.equals(correctAnswer);
                if (isCorrect) {
                    correctCount++;
                }

                // Save the user answer (just the letter)
                userAnswerService.createUserAnswer(
                        attempt,
                        question,
                        userAnswer,
                        isCorrect,
                        timeSpent);
            }

            // Calculate final score as percentage
            int totalQuestions = request.getAnswers().size();
            int score = (totalQuestions > 0) ? (correctCount * 100) / totalQuestions : 0;

            // Submit the quiz attempt with calculated score and correct answers count
            QuizAttempt submittedAttempt = quizAttemptService.submitQuizAttempt(attemptId, score, correctCount);

            return ResponseEntity.ok(new ApiResponse<>(true, "Quiz attempt submitted successfully",
                    quizAttemptService.convertToDTO(submittedAttempt)));

        } catch (IllegalArgumentException e) {
            logger.error("Invalid attempt ID format: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Invalid attempt ID format: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error submitting quiz attempt {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(false, "Failed to submit quiz attempt: " + e.getMessage(), null));
        }
    }

    /**
     * Extracts the letter from an answer choice.
     * Frontend now sends just the letter (A, B, C, D)
     * This function handles both direct letters and legacy full-text answers.
     * Examples: "A" -> "A", "B. FOR NONCOMMERCIAL CLASSROOM USE." -> "B"
     * 
     * @param answer The answer choice (letter or full text)
     * @return Just the letter (A, B, C, or D)
     */
    private String extractAnswerLetter(String answer) {
        if (answer == null || answer.isEmpty()) {
            return "";
        }

        answer = answer.trim();

        // If the answer is just a single letter, return it (frontend now does this)
        if (answer.length() == 1 && Character.isLetter(answer.charAt(0))) {
            return answer;
        }

        // Look for the first letter followed by a dot (e.g., "B. ")
        if (answer.length() >= 2 && Character.isLetter(answer.charAt(0)) && answer.charAt(1) == '.') {
            return String.valueOf(answer.charAt(0));
        }

        // If no dot pattern found, try to extract the first letter
        for (int i = 0; i < answer.length(); i++) {
            if (Character.isLetter(answer.charAt(i))) {
                return String.valueOf(answer.charAt(i));
            }
        }

        // Fallback: return the first character if all else fails
        return String.valueOf(answer.charAt(0));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuizAttempt(@PathVariable String id) {
        try {
            UUID attemptId = UUID.fromString(id);
            quizAttemptService.deleteQuizAttempt(attemptId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Quiz attempt deleted successfully", null));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid attempt ID format: {}", id);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Invalid attempt ID format", null));
        } catch (RuntimeException e) {
            logger.error("Error deleting quiz attempt {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Quiz attempt not found", null));
        } catch (Exception e) {
            logger.error("Error deleting quiz attempt {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(false, "Error deleting attempt: " + e.getMessage(), null));
        }
    }
}
