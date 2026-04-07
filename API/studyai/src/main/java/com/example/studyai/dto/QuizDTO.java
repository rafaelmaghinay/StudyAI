package com.example.studyai.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonFormat;

public class QuizDTO {
    private UUID id;
    private UUID userId;
    private String title;
    private String description;
    private int totalQuestions;
    private String difficultyLevel;
    private List<UUID> noteIds;
    private List<QuestionDTO> questions;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public QuizDTO() {}

    public QuizDTO(UUID id, UUID userId, String title, List<UUID> noteIds, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.noteIds = noteIds;
        this.createdAt = createdAt;
    }

    public QuizDTO(UUID id, UUID userId, String title, String description, int totalQuestions, 
                   String difficultyLevel, List<UUID> noteIds, List<QuestionDTO> questions,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.totalQuestions = totalQuestions;
        this.difficultyLevel = difficultyLevel;
        this.noteIds = noteIds;
        this.questions = questions;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public List<UUID> getNoteIds() { return noteIds; }
    public void setNoteIds(List<UUID> noteIds) { this.noteIds = noteIds; }

    public List<QuestionDTO> getQuestions() { return questions; }
    public void setQuestions(List<QuestionDTO> questions) { this.questions = questions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
