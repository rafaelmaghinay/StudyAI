package com.example.studyai.repository;

import com.example.studyai.model.Subject;
import com.example.studyai.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, UUID> {
    List<Subject> findByUser(User user);
    List<Subject> findByUserId(UUID userId);

    /**
     * Find all subjects for a user with eager loading of User relationship.
     * Uses JOIN FETCH to avoid lazy loading and N+1 queries, which cause
     * prepared statement conflicts with PgBouncer connection pooling.
     *
     * @param userId The user ID
     * @return List of subjects with User relationship eagerly loaded
     */
    @Query("SELECT DISTINCT s FROM Subject s " +
            "JOIN FETCH s.user " +
            "WHERE s.user.id = :userId " +
            "ORDER BY s.createdAt DESC")
    List<Subject> findByUserIdWithUserEager(@Param("userId") UUID userId);
}
