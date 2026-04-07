package com.example.studyai.repository;

import com.example.studyai.model.QuizAttempt;
import com.example.studyai.model.Quiz;
import com.example.studyai.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {
    List<QuizAttempt> findByUser(User user);

    List<QuizAttempt> findByUserId(UUID userId);

    List<QuizAttempt> findByQuiz(Quiz quiz);

    List<QuizAttempt> findByQuizId(UUID quizId);

    // Find active (non-submitted) attempts for a user+quiz combination
    // An attempt is active if score is null (not yet submitted)
    QuizAttempt findByQuizIdAndUserIdAndScoreIsNull(UUID quizId, UUID userId);
}
