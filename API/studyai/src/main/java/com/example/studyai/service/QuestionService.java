package com.example.studyai.service;

import com.example.studyai.dto.QuestionDTO;
import com.example.studyai.model.Question;
import com.example.studyai.model.Quiz;
import com.example.studyai.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    /**
     * Create a multiple choice question with 4 options (A, B, C, D)
     *
     * @param quiz Quiz this question belongs to
     * @param questionText The question text
     * @param options List of 4 options (must be exactly 4)
     * @param correctAnswer One of: A, B, C, or D
     * @param explanation Explanation for the answer
     * @param orderIndex Order of question in quiz
     */
    public Question createQuestion(Quiz quiz, String questionText,
                                   List<String> options, String correctAnswer, String explanation, int orderIndex) {
        // Validate exactly 4 options
        if (options == null || options.size() != 4) {
            throw new IllegalArgumentException("Questions must have exactly 4 options (A, B, C, D)");
        }

        // Validate correctAnswer is one of A, B, C, D
        if (!isValidAnswer(correctAnswer)) {
            throw new IllegalArgumentException("Correct answer must be one of: A, B, C, or D");
        }

        Question question = new Question();
        question.setQuiz(quiz);
        question.setQuestionText(questionText);
        question.setOptions(options);
        question.setCorrectAnswer(correctAnswer);
        question.setExplanation(explanation);
        question.setOrderIndex(orderIndex);
        return questionRepository.save(question);
    }

    public Optional<Question> getQuestionById(UUID id) {
        return questionRepository.findById(id);
    }

    public List<Question> getQuestionsByQuizId(UUID quizId) {
        return questionRepository.findByQuizId(quizId);
    }

    public void deleteQuestion(UUID id) {
        questionRepository.deleteById(id);
    }

    public QuestionDTO convertToDTO(Question question) {
        return new QuestionDTO(question.getId(), question.getQuiz().getId(), question.getQuestionText(),
                question.getQuestionType(), question.getOptions(), question.getCorrectAnswer(),
                question.getExplanation(), question.getOrderIndex());
    }

    public List<QuestionDTO> convertToDTOList(List<Question> questions) {
        return questions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Validate if answer is one of A, B, C, or D
     */
    private static boolean isValidAnswer(String answer) {
        return answer != null && (answer.equalsIgnoreCase("A") ||
                                  answer.equalsIgnoreCase("B") ||
                                  answer.equalsIgnoreCase("C") ||
                                  answer.equalsIgnoreCase("D"));
    }
}
