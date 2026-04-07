package com.example.studyai.service;

import com.example.studyai.client.FastApiClient;
import com.example.studyai.dto.QuizDTO;
import com.example.studyai.dto.QuestionDTO;
import com.example.studyai.dto.QuestionDocument;
import com.example.studyai.dto.QuizGenerationResponse;
import com.example.studyai.exception.QuizGenerationException;
import com.example.studyai.model.Note;
import com.example.studyai.model.Quiz;
import com.example.studyai.model.Subject;
import com.example.studyai.model.User;
import com.example.studyai.repository.QuizRepository;
import com.example.studyai.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private static final Logger logger = LoggerFactory.getLogger(QuizService.class);

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private FastApiClient fastApiClient;

    @Autowired
    private QuestionService questionService;

    @Transactional
    public Quiz createQuiz(User user, String title, List<Note> notes) {
        logger.debug("Creating quiz - user: {}, title: {}, notes count: {}", user.getId(), title, notes.size());
        try {
            Quiz quiz = new Quiz();
            quiz.setUser(user);
            quiz.setTitle(title);
            quiz.setTotalQuestions(0); // Explicitly set required field
            quiz.setNotes(notes != null ? notes : new ArrayList<>());
            logger.debug("Quiz object initialized with required fields: user={}, title={}, totalQuestions=0",
                    user.getId(), title);

            Quiz saved = quizRepository.save(quiz);
            logger.debug("Quiz saved to repository: id={}", saved.getId());

            // Flush to detect constraint violations early
            entityManager.flush();
            logger.debug("EntityManager flushed successfully");

            logger.info("Quiz created - id: {}, notes: {}", saved.getId(),
                    saved.getNotes().stream().map(Note::getId).collect(Collectors.toList()));
            return saved;
        } catch (Exception e) {
            logger.error("Error during quiz creation: {}", e.getMessage());
            logger.error("Error details: ", e);
            throw e;
        }
    }

    @Transactional(rollbackFor = { QuizGenerationException.class, Exception.class }, timeout = 150)
    public Quiz createQuizWithQuestions(User user, String title, String description, String difficulty,
            int questionCount, List<Note> notes, UUID subjectId) {
        logger.debug("Creating quiz with questions - user: {}, title: {}, questions: {}, notes: {}, subjectId: {}",
                user.getId(), title, questionCount, notes.size(), subjectId);

        // Create the base quiz
        Quiz quiz = new Quiz();
        quiz.setUser(user);
        quiz.setTitle(title);
        quiz.setDescription(description);
        quiz.setDifficultyLevel(difficulty);
        quiz.setTotalQuestions(0); // Will update after generating questions
        quiz.setNotes(notes != null ? notes : new ArrayList<>());

        // Set subject if provided
        if (subjectId != null) {
            Subject subject = subjectRepository.findById(subjectId).orElse(null);
            if (subject != null) {
                quiz.setSubject(subject);
                logger.debug("Subject set for quiz: {}", subjectId);
            } else {
                logger.warn("Subject not found: {}", subjectId);
            }
        }
        logger.debug("Quiz object initialized: user={}, title={}, difficulty={}", user.getId(), title, difficulty);

        Quiz savedQuiz = quizRepository.save(quiz);
        entityManager.flush();
        logger.debug("Quiz saved to repository: id={}", savedQuiz.getId());

        // Generate questions from notes if question count > 0
        if (questionCount > 0) {
            // Check that we have notes to generate from
            if (notes == null || notes.isEmpty()) {
                logger.warn("Cannot generate questions: no notes provided for quiz: {}", title);
                // Still allow quiz creation without questions if no notes available
            } else {
                // Collect document metadata (s3Key and fileType) to send to FastAPI
                List<QuestionDocument> documents = notes.stream()
                        .map(note -> new QuestionDocument(note.getS3Key(), note.getFileType().toString()))
                        .collect(Collectors.toList());

                logger.info("Calling FastAPI to generate {} questions from {} documents for quiz: {}",
                        questionCount, documents.size(), title);
                try {
                    // Call FastAPI with document metadata
                    // FastAPI will download files from S3, extract text, and generate quiz
                    QuizGenerationResponse generatedQuestions = fastApiClient.generateQuiz(
                            documents,
                            questionCount,
                            title);

                    logger.info("FastAPI returned {} questions", generatedQuestions.getQuestions().size());

                    // Save generated questions
                    int savedCount = 0;
                    for (QuizGenerationResponse.GeneratedQuestion genQuestion : generatedQuestions.getQuestions()) {
                        questionService.createQuestion(
                                savedQuiz,
                                genQuestion.getQuestionText(),
                                genQuestion.getOptions(),
                                genQuestion.getCorrectAnswer(),
                                genQuestion.getExplanation(),
                                genQuestion.getOrderIndex());
                        savedCount++;
                    }

                    logger.info("Saved {} questions to quiz {}", savedCount, savedQuiz.getId());
                    savedQuiz.setTotalQuestions(savedCount);
                } catch (QuizGenerationException e) {
                    // Log the error and rollback the entire transaction
                    logger.error("FastAPI quiz generation failed for quiz: {}. Rolling back transaction. Error: {}",
                            title, e.getMessage(), e);
                    throw e; // Re-throw to trigger @Transactional rollback
                }
            }
        } else {
            logger.debug("Question count is 0, skipping question generation");
        }

        quizRepository.save(savedQuiz);
        entityManager.flush();
        logger.info("Quiz finalized - id: {}, total questions: {}", savedQuiz.getId(), savedQuiz.getTotalQuestions());
        return savedQuiz;
    }

    @Transactional(readOnly = true)
    public Optional<Quiz> getQuizById(UUID id) {
        return quizRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Quiz> getUserQuizzes(UUID userId) {
        return quizRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<QuizDTO> getUserQuizzesDTO(UUID userId) {
        // Convert within the transaction to avoid LazyInitializationException
        return convertToDTOList(quizRepository.findByUserId(userId));
    }

    @Transactional(readOnly = true)
    public QuizDTO getQuizByIdDTO(UUID id) {
        // Convert within the transaction to avoid LazyInitializationException
        Optional<Quiz> quiz = quizRepository.findById(id);
        if (quiz.isPresent()) {
            Quiz q = quiz.get();
            // Force load questions within transaction
            if (q.getQuestions() != null) {
                q.getQuestions().size(); // Trigger lazy loading
            }
            return convertToDTO(q);
        }
        return null;
    }

    public Quiz updateQuiz(UUID id, String title) {
        Optional<Quiz> quiz = quizRepository.findById(id);
        if (quiz.isPresent()) {
            Quiz q = quiz.get();
            q.setTitle(title);
            return quizRepository.save(q);
        }
        throw new RuntimeException("Quiz not found");
    }

    public void deleteQuiz(UUID id) {
        quizRepository.deleteById(id);
    }

    public QuizDTO convertToDTO(Quiz quiz) {
        // Ensure notes are loaded within transaction
        List<UUID> noteIds = new ArrayList<>();
        if (quiz.getNotes() != null && !quiz.getNotes().isEmpty()) {
            noteIds = quiz.getNotes().stream().map(Note::getId).collect(Collectors.toList());
        }

        // Convert questions to DTOs
        List<QuestionDTO> questionDTOs = new ArrayList<>();
        if (quiz.getQuestions() != null && !quiz.getQuestions().isEmpty()) {
            questionDTOs = quiz.getQuestions().stream()
                    .map(questionService::convertToDTO)
                    .collect(Collectors.toList());
        }

        logger.debug("Converting quiz to DTO - id: {}, questions: {}, noteIds: {}",
                quiz.getId(), questionDTOs.size(), noteIds.size());

        return new QuizDTO(
                quiz.getId(),
                quiz.getUser().getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getTotalQuestions(),
                quiz.getDifficultyLevel(),
                noteIds,
                questionDTOs,
                quiz.getCreatedAt(),
                quiz.getUpdatedAt());
    }

    public List<QuizDTO> convertToDTOList(List<Quiz> quizzes) {
        return quizzes.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
}
