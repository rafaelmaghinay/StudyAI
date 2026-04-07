package com.example.studyai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class QuizGenerationResponse {
    private List<GeneratedQuestion> questions;
    
    @JsonProperty("quiz_title")
    private String quizTitle;
    
    @JsonProperty("total_questions")
    private int totalQuestions;
    
    @JsonProperty("generation_status")
    private String generationStatus;
    
    private String message;

    public QuizGenerationResponse() {}

    public QuizGenerationResponse(List<GeneratedQuestion> questions, String quizTitle, 
                                 int totalQuestions, String generationStatus, String message) {
        this.questions = questions;
        this.quizTitle = quizTitle;
        this.totalQuestions = totalQuestions;
        this.generationStatus = generationStatus;
        this.message = message;
    }

    public List<GeneratedQuestion> getQuestions() { return questions; }
    public void setQuestions(List<GeneratedQuestion> questions) { this.questions = questions; }

    public String getQuizTitle() { return quizTitle; }
    public void setQuizTitle(String quizTitle) { this.quizTitle = quizTitle; }
    
    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    
    public String getGenerationStatus() { return generationStatus; }
    public void setGenerationStatus(String generationStatus) { this.generationStatus = generationStatus; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public static class GeneratedQuestion {
        @JsonProperty("question_text")
        private String questionText;
        
        @JsonProperty("question_type")
        private String questionType;
        
        private List<String> options;
        
        @JsonProperty("correct_answer")
        private String correctAnswer;
        
        private String explanation;
        
        @JsonProperty("order_index")
        private int orderIndex;

        public GeneratedQuestion() {}

        public GeneratedQuestion(String questionText, String questionType, List<String> options,
                                String correctAnswer, String explanation, int orderIndex) {
            this.questionText = questionText;
            this.questionType = questionType;
            this.options = options;
            this.correctAnswer = correctAnswer;
            this.explanation = explanation;
            this.orderIndex = orderIndex;
        }

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
}
