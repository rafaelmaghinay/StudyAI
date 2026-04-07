package com.example.studyai.service;

import com.example.studyai.dto.UserAnswerDTO;
import com.example.studyai.model.Question;
import com.example.studyai.model.QuizAttempt;
import com.example.studyai.model.UserAnswer;
import com.example.studyai.repository.UserAnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserAnswerService {

    @Autowired
    private UserAnswerRepository userAnswerRepository;

    public UserAnswer createUserAnswer(QuizAttempt attempt, Question question, String answerText, boolean isCorrect) {
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setQuizAttempt(attempt);
        userAnswer.setQuestion(question);
        userAnswer.setUserAnswer(answerText);
        userAnswer.setCorrect(isCorrect);
        return userAnswerRepository.save(userAnswer);
    }

    public UserAnswer createUserAnswer(QuizAttempt attempt, Question question, String answerText, boolean isCorrect,
            Integer timeSpentSeconds) {
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setQuizAttempt(attempt);
        userAnswer.setQuestion(question);
        userAnswer.setUserAnswer(answerText);
        userAnswer.setCorrect(isCorrect);
        if (timeSpentSeconds != null) {
            userAnswer.setTimeSpentSeconds(timeSpentSeconds);
        }
        return userAnswerRepository.save(userAnswer);
    }

    public Optional<UserAnswer> getUserAnswerById(UUID id) {
        return userAnswerRepository.findById(id);
    }

    public List<UserAnswer> getAttemptAnswers(UUID attemptId) {
        return userAnswerRepository.findByAttemptId(attemptId);
    }

    public UserAnswerDTO convertToDTO(UserAnswer userAnswer) {
        return new UserAnswerDTO(userAnswer.getId(), userAnswer.getQuizAttempt().getId(),
                userAnswer.getQuestion().getId(), userAnswer.getUserAnswer(), userAnswer.isCorrect());
    }

    public List<UserAnswerDTO> convertToDTOList(List<UserAnswer> userAnswers) {
        return userAnswers.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public void deleteByAttemptId(UUID attemptId) {
        List<UserAnswer> answers = userAnswerRepository.findByAttemptId(attemptId);
        if (!answers.isEmpty()) {
            userAnswerRepository.deleteAll(answers);
        }
    }
}
