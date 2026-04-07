package com.example.studyai.repository;

import com.example.studyai.model.Note;
import com.example.studyai.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {
    List<Note> findBySubject(Subject subject);
    List<Note> findBySubjectId(UUID subjectId);
    List<Note> findByUserId(UUID userId);

    /**
     * Find all notes for a user with eager loading of User and Subject relationships.
     * Uses JOIN FETCH to avoid lazy loading and N+1 queries, which cause
     * prepared statement conflicts with PgBouncer connection pooling.
     *
     * @param userId The user ID
     * @return List of notes with User and Subject relationships eagerly loaded
     */
    @Query("SELECT DISTINCT n FROM Note n " +
            "JOIN FETCH n.user " +
            "JOIN FETCH n.subject " +
            "WHERE n.user.id = :userId " +
            "ORDER BY n.createdAt DESC")
    List<Note> findByUserIdWithUserAndSubjectEager(@Param("userId") UUID userId);

    /**
     * Find all notes for a subject with eager loading of User relationship.
     * Uses JOIN FETCH to avoid lazy loading and N+1 queries.
     *
     * @param subjectId The subject ID
     * @return List of notes with User relationship eagerly loaded
     */
    @Query("SELECT DISTINCT n FROM Note n " +
            "JOIN FETCH n.user " +
            "WHERE n.subject.id = :subjectId " +
            "ORDER BY n.createdAt DESC")
    List<Note> findBySubjectIdWithUserEager(@Param("subjectId") UUID subjectId);
}
