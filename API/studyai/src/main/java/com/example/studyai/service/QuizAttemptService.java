package com.example.studyai.service;

import com.example.studyai.dto.QuizAttemptDTO;
import com.example.studyai.dto.UserAnswerDTO;
import com.example.studyai.model.Quiz;
import com.example.studyai.model.QuizAttempt;
import com.example.studyai.model.User;
import com.example.studyai.repository.QuizAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuizAttemptService {

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private UserAnswerService userAnswerService;

    public QuizAttempt createQuizAttempt(Quiz quiz, User user, int totalQuestions) {
        // Check if an active (non-submitted) attempt already exists for this quiz+user
        // This prevents duplicate attempts if the frontend calls this twice
        QuizAttempt existingAttempt = quizAttemptRepository.findByQuizIdAndUserIdAndScoreIsNull(quiz.getId(),
                user.getId());
        if (existingAttempt != null) {
            return existingAttempt;
        }

        // No active attempt exists, create a new one
        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setUser(user);
        attempt.setTotalQuestions(totalQuestions);
        return quizAttemptRepository.save(attempt);
    }

    public Optional<QuizAttempt> getQuizAttemptById(UUID id) {
        return quizAttemptRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public QuizAttemptDTO getQuizAttemptByIdDTO(UUID id) {
        Optional<QuizAttempt> attempt = quizAttemptRepository.findById(id);
        if (attempt.isPresent()) {
            // Force load collections within transaction to avoid lazy loading issues
            if (attempt.get().getUserAnswers() != null) {
                attempt.get().getUserAnswers().size();
            }
            return convertToDTO(attempt.get());
        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<QuizAttempt> getUserAttempts(UUID userId) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByUserId(userId);
        // Force load collections within transaction to avoid lazy loading issues
        attempts.forEach(attempt -> {
            if (attempt.getUserAnswers() != null) {
                attempt.getUserAnswers().size();
            }
        });
        return attempts;
    }

    @Transactional(readOnly = true)
    public List<QuizAttempt> getQuizAttempts(UUID quizId) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizId(quizId);
        // Force load collections within transaction to avoid lazy loading issues
        attempts.forEach(attempt -> {
            if (attempt.getUserAnswers() != null) {
                attempt.getUserAnswers().size();
            }
        });
        return attempts;
    }

    public QuizAttempt submitQuizAttempt(UUID id, Integer score, Integer correctAnswers) {
        Optional<QuizAttempt> attempt = quizAttemptRepository.findById(id);
        if (attempt.isPresent()) {
            QuizAttempt qa = attempt.get();
            qa.setScore(score);
            qa.setCorrectAnswers(correctAnswers);
            return quizAttemptRepository.save(qa);
        }
        throw new RuntimeException("Quiz attempt not found");
    }

    public QuizAttemptDTO convertToDTO(QuizAttempt attempt) {
        List<UserAnswerDTO> answers = userAnswerService.convertToDTOList(attempt.getUserAnswers());

        // If correctAnswers is 0 but we have userAnswers, calculate from them
        int correctAnswers = attempt.getCorrectAnswers();
        if (correctAnswers == 0 && answers != null && !answers.isEmpty()) {
            correctAnswers = (int) answers.stream().filter(UserAnswerDTO::isCorrect).count();
        }

        return new QuizAttemptDTO(attempt.getId(), attempt.getQuiz().getId(), attempt.getUser().getId(),
                attempt.getScore(), attempt.getTotalQuestions(), correctAnswers,
                attempt.getStartedAt(), attempt.getCompletedAt(), attempt.getTimeSpentSeconds(), answers);
    }

    public List<QuizAttemptDTO> convertToDTOList(List<QuizAttempt> attempts) {
        return attempts.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void deleteQuizAttempt(UUID id) {
        Optional<QuizAttempt> attempt = quizAttemptRepository.findById(id);
        if (attempt.isPresent()) {
            // Delete related user answers first (due to CASCADE)
            userAnswerService.deleteByAttemptId(id);
            // Then delete the attempt
            quizAttemptRepository.deleteById(id);
        } else {
            throw new RuntimeException("Quiz attempt not found");
        }
    }
}
