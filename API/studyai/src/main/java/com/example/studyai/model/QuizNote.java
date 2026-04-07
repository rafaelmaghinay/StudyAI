package com.example.studyai.model;

import jakarta.persistence.*;
import java.util.UUID;
import java.util.Objects;

/**
 * QuizNote - Junction table entity for Many-to-Many relationship between Quiz
 * and Note with explicit UUID primary key
 * Allows quizzes to be created from multiple notes
 */
@Entity
@Table(name = "quiz_notes")
public class QuizNote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "quiznote_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    @Column(name = "order_index")
    private int orderIndex = 0;

    public QuizNote() {
    }

    public QuizNote(Quiz quiz, Note note) {
        this.quiz = quiz;
        this.note = note;
        this.orderIndex = 0;
    }

    public QuizNote(Quiz quiz, Note note, int orderIndex) {
        this.quiz = quiz;
        this.note = note;
        this.orderIndex = orderIndex;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        QuizNote quizNote = (QuizNote) o;
        return id != null && id.equals(quizNote.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
