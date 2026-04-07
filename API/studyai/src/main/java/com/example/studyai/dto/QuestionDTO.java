package com.example.studyai.dto;

import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for Questions
 *
 * All questions are multiple choice with 4 options (A, B, C, D)
 * correctAnswer must be one of: A, B, C, or D
 * options must always contain exactly 4 strings
 */
public class QuestionDTO {
    private UUID id;
    private UUID quizId;
    private String questionText;
    private String questionType;  // Always "multiple_choice"
    private List<String> options;  // Always exactly 4 options
    private String correctAnswer;  // One of: A, B, C, or D
    private String explanation;
    private int orderIndex;

    public QuestionDTO() {}

    public QuestionDTO(UUID id, UUID quizId, String questionText, String questionType,
                       List<String> options, String correctAnswer, String explanation, int orderIndex) {
        this.id = id;
        this.quizId = quizId;
        this.questionText = questionText;
        this.questionType = questionType;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
        this.orderIndex = orderIndex;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getQuizId() { return quizId; }
    public void setQuizId(UUID quizId) { this.quizId = quizId; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
}
