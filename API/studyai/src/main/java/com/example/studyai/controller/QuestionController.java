package com.example.studyai.controller;

import com.example.studyai.dto.QuestionDTO;
import com.example.studyai.model.Question;
import com.example.studyai.model.Quiz;
import com.example.studyai.service.QuestionService;
import com.example.studyai.service.QuizService;
import com.example.studyai.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuizService quizService;

    /**
     * Create a multiple choice question with 4 options (A, B, C, D)
     *
     * Request body:
     * {
     *   "quizId": "uuid",
     *   "questionText": "What is 2+2?",
     *   "options": ["3", "4", "5", "6"],           // Exactly 4 options
     *   "correctAnswer": "B",                       // One of: A, B, C, or D
     *   "explanation": "Basic arithmetic",
     *   "orderIndex": 1
     * }
     */
    @PostMapping
    public ResponseEntity<?> createQuestion(@RequestBody Map<String, Object> request) {
        UUID quizId = UUID.fromString((String) request.get("quizId"));
        String questionText = (String) request.get("questionText");
        @SuppressWarnings("unchecked")
        List<String> options = (List<String>) request.get("options");
        String correctAnswer = (String) request.get("correctAnswer");
        String explanation = (String) request.get("explanation");
        int orderIndex = ((Number) request.get("orderIndex")).intValue();

        Optional<Quiz> quiz = quizService.getQuizById(quizId);
        if (quiz.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Question question = questionService.createQuestion(quiz.get(), questionText, options, correctAnswer, explanation, orderIndex);
            return ResponseEntity.ok(new ApiResponse<>(true, "Question created successfully", questionService.convertToDTO(question)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // IMPORTANT: Put /quiz/{quizId} BEFORE /{id} to avoid path variable conflict
    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<?> getQuizQuestions(@PathVariable String quizId) {
        try {
            UUID quizUuid = UUID.fromString(quizId);
            List<Question> questions = questionService.getQuestionsByQuizId(quizUuid);
            return ResponseEntity.ok(new ApiResponse<>(true, "Quiz questions retrieved", questionService.convertToDTOList(questions)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid quiz ID format", null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQuestion(@PathVariable String id) {
        try {
            UUID questionId = UUID.fromString(id);
            Optional<Question> question = questionService.getQuestionById(questionId);
            if (question.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "Question retrieved", questionService.convertToDTO(question.get())));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid question ID format", null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable UUID id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}
