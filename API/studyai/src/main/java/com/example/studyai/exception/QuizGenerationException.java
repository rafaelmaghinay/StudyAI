package com.example.studyai.exception;

/**
 * Exception thrown when quiz generation fails in FastAPI service.
 * This exception causes transaction rollback when thrown from @Transactional methods.
 */
public class QuizGenerationException extends RuntimeException {
    
    public QuizGenerationException(String message) {
        super(message);
    }
    
    public QuizGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public QuizGenerationException(Throwable cause) {
        super(cause);
    }
}
