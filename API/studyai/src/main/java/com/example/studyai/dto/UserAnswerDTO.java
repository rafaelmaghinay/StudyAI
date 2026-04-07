package com.example.studyai.dto;

import java.util.UUID;

public class UserAnswerDTO {
    private UUID id;
    private UUID quizAttemptId;
    private UUID questionId;
    private String userAnswer;
    private boolean isCorrect;

    public UserAnswerDTO() {
    }

    public UserAnswerDTO(UUID id, UUID quizAttemptId, UUID questionId, String userAnswer, boolean isCorrect) {
        this.id = id;
        this.quizAttemptId = quizAttemptId;
        this.questionId = questionId;
        this.userAnswer = userAnswer;
        this.isCorrect = isCorrect;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getQuizAttemptId() {
        return quizAttemptId;
    }

    public void setQuizAttemptId(UUID quizAttemptId) {
        this.quizAttemptId = quizAttemptId;
    }

    public UUID getQuestionId() {
        return questionId;
    }

    public void setQuestionId(UUID questionId) {
        this.questionId = questionId;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }
}
