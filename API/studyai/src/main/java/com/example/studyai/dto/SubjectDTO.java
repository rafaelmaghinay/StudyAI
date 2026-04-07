package com.example.studyai.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonFormat;

public class SubjectDTO {
    private UUID id;
    private UUID userId;
    private String name;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    private Integer notesCount;
    private Integer quizzesCount;

    public SubjectDTO() {
    }

    public SubjectDTO(UUID id, UUID userId, String name, String description,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.notesCount = 0;
        this.quizzesCount = 0;
    }

    public SubjectDTO(UUID id, UUID userId, String name, String description,
            LocalDateTime createdAt, LocalDateTime updatedAt,
            Integer notesCount, Integer quizzesCount) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.notesCount = notesCount;
        this.quizzesCount = quizzesCount;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getNotesCount() {
        return notesCount;
    }

    public void setNotesCount(Integer notesCount) {
        this.notesCount = notesCount;
    }

    public Integer getQuizzesCount() {
        return quizzesCount;
    }

    public void setQuizzesCount(Integer quizzesCount) {
        this.quizzesCount = quizzesCount;
    }
}
