package com.example.studyai.repository;

import com.example.studyai.model.Quiz;
import com.example.studyai.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, UUID> {
    List<Quiz> findByUser(User user);
    List<Quiz> findByUserId(UUID userId);

    /**
     * Find all quizzes for a user with eager loading of User relationship.
     * Uses JOIN FETCH to avoid lazy loading and N+1 queries, which cause
     * prepared statement conflicts with PgBouncer connection pooling.
     *
     * @param userId The user ID
     * @return List of quizzes with User relationship eagerly loaded
     */
    @Query("SELECT DISTINCT q FROM Quiz q " +
            "JOIN FETCH q.user " +
            "WHERE q.user.id = :userId " +
            "ORDER BY q.createdAt DESC")
    List<Quiz> findByUserIdWithUserEager(@Param("userId") UUID userId);
}
