package com.example.studyai.repository;

import com.example.studyai.model.QuizNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * QuizNoteRepository - Repository for QuizNote junction table
 * Enables querying many-to-many relationships between Quiz and Note
 */
@Repository
public interface QuizNoteRepository extends JpaRepository<QuizNote, UUID> {

    /**
     * Find all quiz-note associations for a specific quiz
     */
    List<QuizNote> findByQuizId(UUID quizId);

    /**
     * Find all quiz-note associations for a specific note
     */
    List<QuizNote> findByNoteId(UUID noteId);

    /**
     * Delete all quiz-note associations for a specific quiz
     */
    void deleteByQuizId(UUID quizId);

    /**
     * Delete all quiz-note associations for a specific note
     */
    void deleteByNoteId(UUID noteId);
}
