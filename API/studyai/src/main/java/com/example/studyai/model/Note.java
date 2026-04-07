package com.example.studyai.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "notes")
public class Note {

    public enum FileType {
        pdf, docx
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "s3_key", length = 512)
    private String s3Key;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type")
    private FileType fileType;

    @Column(name = "file_size_kb")
    private Integer fileSizeKb;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<QuizNote> quizNotes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
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

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public Integer getFileSizeKb() {
        return fileSizeKb;
    }

    public void setFileSizeKb(Integer fileSizeKb) {
        this.fileSizeKb = fileSizeKb;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<QuizNote> getQuizNotes() {
        return quizNotes;
    }

    public void setQuizNotes(List<QuizNote> quizNotes) {
        this.quizNotes = quizNotes;
    }

    // Helper method for backward compatibility - extracts Quiz objects from
    // QuizNotes
    public List<Quiz> getQuizzes() {
        if (quizNotes == null || quizNotes.isEmpty()) {
            return new ArrayList<>();
        }
        return quizNotes.stream()
                .map(QuizNote::getQuiz)
                .collect(java.util.stream.Collectors.toList());
    }

    // Helper method to set quizzes - creates QuizNote wrappers
    public void setQuizzes(List<Quiz> quizzes) {
        this.quizNotes = new ArrayList<>();
        if (quizzes != null) {
            int index = 0;
            for (Quiz quiz : quizzes) {
                QuizNote qn = new QuizNote(quiz, this, index++);
                this.quizNotes.add(qn);
            }
        }
    }
}
