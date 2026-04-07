package com.example.studyai.dto;

import java.util.List;

/**
 * Request DTO for quiz generation from notes stored in S3
 * 
 * Instead of sending the full text content, we send note IDs which allows FastAPI
 * to fetch files from S3 and extract text itself.
 */
public class QuizGenerationRequestWithNotes {
    
    private List<String> note_ids;
    private int num_questions;
    private String quiz_title;
    
    // Constructors
    public QuizGenerationRequestWithNotes() {
    }
    
    public QuizGenerationRequestWithNotes(List<String> note_ids, int num_questions, String quiz_title) {
        this.note_ids = note_ids;
        this.num_questions = num_questions;
        this.quiz_title = quiz_title;
    }
    
    // Getters and Setters
    public List<String> getNote_ids() {
        return note_ids;
    }
    
    public void setNote_ids(List<String> note_ids) {
        this.note_ids = note_ids;
    }
    
    public int getNum_questions() {
        return num_questions;
    }
    
    public void setNum_questions(int num_questions) {
        this.num_questions = num_questions;
    }
    
    public String getQuiz_title() {
        return quiz_title;
    }
    
    public void setQuiz_title(String quiz_title) {
        this.quiz_title = quiz_title;
    }
    
    @Override
    public String toString() {
        return "QuizGenerationRequestWithNotes{" +
                "note_ids=" + note_ids +
                ", num_questions=" + num_questions +
                ", quiz_title='" + quiz_title + '\'' +
                '}';
    }
}
