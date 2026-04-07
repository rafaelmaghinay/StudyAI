package com.example.studyai.client;

import com.example.studyai.dto.QuizGenerationRequest;
import com.example.studyai.dto.QuestionDocument;
import com.example.studyai.dto.QuizGenerationResponse;
import com.example.studyai.exception.QuizGenerationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Component
public class FastApiClient {

    private static final Logger logger = LoggerFactory.getLogger(FastApiClient.class);

    @Value("${fastapi.url:http://localhost:8000}")
    private String fastApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public QuizGenerationResponse generateQuiz(List<QuestionDocument> documents, int numQuestions, String quizTitle)
            throws QuizGenerationException {
        String url = fastApiUrl + "/api/quiz/generate";

        QuizGenerationRequest request = new QuizGenerationRequest(documents, numQuestions, quizTitle);

        try {
            logger.info("Calling FastAPI quiz generation: title={}, numQuestions={}, numDocuments={}",
                    quizTitle, numQuestions, documents.size());

            QuizGenerationResponse response = restTemplate.postForObject(url, request, QuizGenerationResponse.class);

            if (response != null && response.getQuestions() != null) {
                logger.info("FastAPI quiz generation succeeded: title={}, requestedQuestions={}, generatedQuestions={}",
                        quizTitle, numQuestions, response.getQuestions().size());
                return response;
            } else {
                logger.warn("FastAPI quiz generation returned null or empty response: title={}, requestedQuestions={}",
                        quizTitle, numQuestions);
                throw new QuizGenerationException(
                        "FastAPI returned null/empty response for quiz generation: " + quizTitle);
            }
        } catch (RestClientException e) {
            String errorMsg = String.format(
                    "FastAPI quiz generation failed: title=%s, numQuestions=%d, error=%s",
                    quizTitle, numQuestions, e.getMessage());
            logger.error(errorMsg, e);
            throw new QuizGenerationException(errorMsg, e);
        } catch (QuizGenerationException e) {
            throw e; // Re-throw QuizGenerationException as-is
        } catch (Exception e) {
            String errorMsg = String.format(
                    "Unexpected error during quiz generation: title=%s, numQuestions=%d, error=%s",
                    quizTitle, numQuestions, e.getMessage());
            logger.error(errorMsg, e);
            throw new QuizGenerationException(errorMsg, e);
        }
    }
}
