package com.example.studyai.controller;

import com.example.studyai.dto.UserAnswerDTO;
import com.example.studyai.model.Question;
import com.example.studyai.model.QuizAttempt;
import com.example.studyai.model.UserAnswer;
import com.example.studyai.service.QuestionService;
import com.example.studyai.service.QuizAttemptService;
import com.example.studyai.service.UserAnswerService;
import com.example.studyai.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/user-answers")
public class UserAnswerController {

    @Autowired
    private UserAnswerService userAnswerService;

    @Autowired
    private QuizAttemptService quizAttemptService;

    @Autowired
    private QuestionService questionService;

    @PostMapping
    public ResponseEntity<?> submitAnswer(@RequestBody Map<String, Object> request) {
        UUID attemptId = UUID.fromString((String) request.get("attemptId"));
        UUID questionId = UUID.fromString((String) request.get("questionId"));
        String answerText = (String) request.get("answerText");

        Optional<QuizAttempt> attempt = quizAttemptService.getQuizAttemptById(attemptId);
        Optional<Question> question = questionService.getQuestionById(questionId);

        if (attempt.isEmpty() || question.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        boolean isCorrect = question.get().getCorrectAnswer().equalsIgnoreCase(answerText);
        UserAnswer userAnswer = userAnswerService.createUserAnswer(attempt.get(), question.get(), answerText, isCorrect);
        return ResponseEntity.ok(new ApiResponse<>(true, "Answer submitted successfully", userAnswerService.convertToDTO(userAnswer)));
    }

    // IMPORTANT: Put /attempt/{attemptId} BEFORE /{id} to avoid path variable conflict
    @GetMapping("/attempt/{attemptId}")
    public ResponseEntity<?> getAttemptAnswers(@PathVariable String attemptId) {
        try {
            UUID attemptUuid = UUID.fromString(attemptId);
            List<UserAnswer> answers = userAnswerService.getAttemptAnswers(attemptUuid);
            return ResponseEntity.ok(new ApiResponse<>(true, "Attempt answers retrieved", userAnswerService.convertToDTOList(answers)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid attempt ID format", null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserAnswer(@PathVariable String id) {
        try {
            UUID answerId = UUID.fromString(id);
            Optional<UserAnswer> answer = userAnswerService.getUserAnswerById(answerId);
            if (answer.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "Answer retrieved", userAnswerService.convertToDTO(answer.get())));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid answer ID format", null));
        }
    }
}
