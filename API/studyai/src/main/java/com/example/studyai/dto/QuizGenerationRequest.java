package com.example.studyai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class QuizGenerationRequest {
    @JsonProperty("documents")
    private List<QuestionDocument> documents;

    @JsonProperty("num_questions")
    private int numQuestions;

    @JsonProperty("quiz_title")
    private String quizTitle;

    public QuizGenerationRequest() {
    }

    public QuizGenerationRequest(List<QuestionDocument> documents, int numQuestions, String quizTitle) {
        this.documents = documents;
        this.numQuestions = numQuestions;
        this.quizTitle = quizTitle;
    }

    public List<QuestionDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<QuestionDocument> documents) {
        this.documents = documents;
    }

    public int getNumQuestions() {
        return numQuestions;
    }

    public void setNumQuestions(int numQuestions) {
        this.numQuestions = numQuestions;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
    }
}
