package com.example.studyai.repository;

import com.example.studyai.model.Question;
import com.example.studyai.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    List<Question> findByQuiz(Quiz quiz);
    List<Question> findByQuizId(UUID quizId);
}
