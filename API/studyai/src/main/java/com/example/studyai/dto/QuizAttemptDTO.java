package com.example.studyai.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonFormat;

public class QuizAttemptDTO {
    private UUID id;
    private UUID quizId;
    private UUID userId;
    private Integer score;
    private int totalQuestions;
    private int correctAnswers;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startedAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;
    private Integer timeSpentSeconds;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime takenAt; // Backward compatibility
    private List<UserAnswerDTO> userAnswers;

    public QuizAttemptDTO() {
    }

    public QuizAttemptDTO(UUID id, UUID quizId, UUID userId, Integer score, int totalQuestions,
            int correctAnswers, LocalDateTime startedAt, LocalDateTime completedAt,
            Integer timeSpentSeconds, List<UserAnswerDTO> userAnswers) {
        this.id = id;
        this.quizId = quizId;
        this.userId = userId;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.timeSpentSeconds = timeSpentSeconds;
        this.takenAt = completedAt; // For backward compatibility
        this.userAnswers = userAnswers;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getQuizId() {
        return quizId;
    }

    public void setQuizId(UUID quizId) {
        this.quizId = quizId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getTimeSpentSeconds() {
        return timeSpentSeconds;
    }

    public void setTimeSpentSeconds(Integer timeSpentSeconds) {
        this.timeSpentSeconds = timeSpentSeconds;
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public LocalDateTime getTakenAt() {
        return takenAt != null ? takenAt : completedAt;
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public void setTakenAt(LocalDateTime takenAt) {
        this.takenAt = takenAt;
    }

    public List<UserAnswerDTO> getUserAnswers() {
        return userAnswers;
    }

    public void setUserAnswers(List<UserAnswerDTO> userAnswers) {
        this.userAnswers = userAnswers;
    }
}
