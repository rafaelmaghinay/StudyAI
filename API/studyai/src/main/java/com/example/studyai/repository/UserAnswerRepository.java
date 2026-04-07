package com.example.studyai.repository;

import com.example.studyai.model.UserAnswer;
import com.example.studyai.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, UUID> {
    List<UserAnswer> findByQuizAttempt(QuizAttempt quizAttempt);

    List<UserAnswer> findByQuizAttemptId(UUID quizAttemptId);

    @Deprecated(since = "1.1.0", forRemoval = true)
    default List<UserAnswer> findByAttempt(QuizAttempt attempt) {
        return findByQuizAttempt(attempt);
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    default List<UserAnswer> findByAttemptId(UUID attemptId) {
        return findByQuizAttemptId(attemptId);
    }
}
