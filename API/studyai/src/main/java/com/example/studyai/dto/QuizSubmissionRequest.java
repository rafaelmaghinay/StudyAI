package com.example.studyai.dto;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for quiz submission with user answers
 */
public class QuizSubmissionRequest {
    private List<AnswerSubmission> answers;

    public QuizSubmissionRequest() {
    }

    public QuizSubmissionRequest(List<AnswerSubmission> answers) {
        this.answers = answers;
    }

    public List<AnswerSubmission> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerSubmission> answers) {
        this.answers = answers;
    }

    /**
     * Individual answer submission for a question
     */
    public static class AnswerSubmission {
        private UUID questionId;
        private String userAnswer; // A, B, C, or D
        private Integer timeSpentSeconds; // Optional: how long user spent on this question

        public AnswerSubmission() {
        }

        public AnswerSubmission(UUID questionId, String userAnswer, Integer timeSpentSeconds) {
            this.questionId = questionId;
            this.userAnswer = userAnswer;
            this.timeSpentSeconds = timeSpentSeconds;
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

        public Integer getTimeSpentSeconds() {
            return timeSpentSeconds;
        }

        public void setTimeSpentSeconds(Integer timeSpentSeconds) {
            this.timeSpentSeconds = timeSpentSeconds;
        }
    }
}
