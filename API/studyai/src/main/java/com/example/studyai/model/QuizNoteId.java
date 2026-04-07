package com.example.studyai.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * QuizNoteId - Composite primary key for the QuizNote junction table
 */
@Embeddable
public class QuizNoteId implements Serializable {

    @Column(name = "quiz_id")
    private UUID quizId;

    @Column(name = "note_id")
    private UUID noteId;

    public QuizNoteId() {
    }

    public QuizNoteId(UUID quizId, UUID noteId) {
        this.quizId = quizId;
        this.noteId = noteId;
    }

    public UUID getQuizId() {
        return quizId;
    }

    public void setQuizId(UUID quizId) {
        this.quizId = quizId;
    }

    public UUID getNoteId() {
        return noteId;
    }

    public void setNoteId(UUID noteId) {
        this.noteId = noteId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        QuizNoteId that = (QuizNoteId) o;
        return Objects.equals(quizId, that.quizId) && Objects.equals(noteId, that.noteId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quizId, noteId);
    }
}
