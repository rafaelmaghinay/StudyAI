package com.example.studyai.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_answers")
public class UserAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_attempt_id", nullable = false)
    private QuizAttempt quizAttempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "user_answer", nullable = false, columnDefinition = "TEXT")
    private String userAnswer;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;

    @PrePersist
    protected void onCreate() {
        if (this.answeredAt == null) {
            this.answeredAt = LocalDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public QuizAttempt getQuizAttempt() {
        return quizAttempt;
    }

    public void setQuizAttempt(QuizAttempt quizAttempt) {
        this.quizAttempt = quizAttempt;
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public QuizAttempt getAttempt() {
        return quizAttempt;
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public void setAttempt(QuizAttempt attempt) {
        this.quizAttempt = attempt;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public String getAnswerText() {
        return userAnswer;
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public void setAnswerText(String answerText) {
        this.userAnswer = answerText;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public Integer getTimeSpentSeconds() {
        return timeSpentSeconds;
    }

    public void setTimeSpentSeconds(Integer timeSpentSeconds) {
        this.timeSpentSeconds = timeSpentSeconds;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }
}