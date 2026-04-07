package com.example.studyai.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonFormat;

public class NoteDTO {
    private UUID id;
    private UUID userId;
    private UUID subjectId;
    private String title;
    private String s3Key;
    private String fileType;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    private String downloadUrl;

    public NoteDTO() {
    }

    public NoteDTO(UUID id, UUID userId, UUID subjectId, String title, String s3Key,
            String fileType, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.subjectId = subjectId;
        this.title = title;
        this.s3Key = s3Key;
        this.fileType = fileType;
        this.createdAt = createdAt;
        this.downloadUrl = null;
    }

    public NoteDTO(UUID id, UUID userId, UUID subjectId, String title, String s3Key,
            String fileType, LocalDateTime createdAt, String downloadUrl) {
        this.id = id;
        this.userId = userId;
        this.subjectId = subjectId;
        this.title = title;
        this.s3Key = s3Key;
        this.fileType = fileType;
        this.createdAt = createdAt;
        this.downloadUrl = downloadUrl;
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

    public UUID getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(UUID subjectId) {
        this.subjectId = subjectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
