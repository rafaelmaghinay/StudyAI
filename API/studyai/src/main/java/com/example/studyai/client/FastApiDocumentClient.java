package com.example.studyai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.core.io.ByteArrayResource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Client for communicating with FastAPI document service
 * Handles document uploads, downloads, and S3 operations
 */
@Component
public class FastApiDocumentClient {

    private static final Logger logger = LoggerFactory.getLogger(FastApiDocumentClient.class);

    @Value("${fastapi.url:http://localhost:8000}")
    private String fastApiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public FastApiDocumentClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Upload a document to FastAPI for processing and S3 storage
     *
     * @param file The file to upload
     * @param userId User ID
     * @param subjectId Subject ID
     * @param noteId Note ID
     * @return Map containing file_id, s3_key, extracted_text, and other metadata
     * @throws RestClientException if upload fails
     * @throws IOException if file operations fail
     */
    public Map<String, Object> uploadDocument(
            MultipartFile file,
            String userId,
            String subjectId,
            String noteId) throws RestClientException, IOException {
        try {
            logger.info("Uploading document to FastAPI: userId={}, subjectId={}, noteId={}", userId, subjectId, noteId);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");

            // Build multipart request
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
            body.add("user_id", userId);
            body.add("subject_id", subjectId);
            body.add("note_id", noteId);

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            String url = fastApiUrl + "/api/documents/upload";
            logger.debug("Calling FastAPI upload endpoint: {}", url);

            JsonNode response = restTemplate.postForObject(url, request, JsonNode.class);

            if (response == null) {
                throw new RestClientException("Empty response from FastAPI upload endpoint");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("file_id", response.get("file_id").asText());
            result.put("s3_key", response.get("s3_key").asText());
            result.put("extracted_text", response.get("extracted_text").asText());
            result.put("file_type", response.get("file_type").asText());
            result.put("file_size", response.get("file_size").asInt());
            result.put("message", response.get("message").asText());

            logger.info("Document uploaded successfully. S3 Key: {}", result.get("s3_key"));
            return result;

        } catch (RestClientException e) {
            logger.error("Error calling FastAPI upload endpoint: {}", e.getMessage(), e);
            throw e;
        } catch (IOException e) {
            logger.error("IO error while uploading document: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error uploading document: {}", e.getMessage(), e);
            throw new RestClientException("Failed to upload document: " + e.getMessage(), e);
        }
    }

    /**
     * Get a presigned URL for downloading a document
     *
     * @param fileId File ID
     * @param s3Key S3 object key
     * @param expirationSeconds URL expiration time in seconds
     * @return Presigned URL string
     * @throws RestClientException if request fails
     */
    public String getPresignedUrl(String fileId, String s3Key, int expirationSeconds) throws RestClientException {
        try {
            logger.info("Getting presigned URL from FastAPI for file: {}", fileId);

            String url = fastApiUrl + "/api/documents/presigned-url/" + fileId + "?s3_key=" + s3Key + "&expiration=" + expirationSeconds;
            logger.debug("Calling FastAPI presigned-url endpoint: {}", url);

            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response == null || !response.has("presigned_url")) {
                throw new RestClientException("Invalid response from FastAPI presigned-url endpoint");
            }

            String presignedUrl = response.get("presigned_url").asText();
            logger.info("Presigned URL generated successfully for file: {}", fileId);
            return presignedUrl;

        } catch (RestClientException e) {
            logger.error("Error getting presigned URL from FastAPI: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error getting presigned URL: {}", e.getMessage(), e);
            throw new RestClientException("Failed to get presigned URL: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a document from S3
     *
     * @param fileId File ID
     * @param s3Key S3 object key
     * @return Success message
     * @throws RestClientException if deletion fails
     */
    public String deleteDocument(String fileId, String s3Key) throws RestClientException {
        try {
            logger.info("Deleting document from FastAPI: fileId={}, s3Key={}", fileId, s3Key);

            String url = fastApiUrl + "/api/documents/delete/" + fileId + "?s3_key=" + s3Key;
            logger.debug("Calling FastAPI delete endpoint: {}", url);

            restTemplate.delete(url);

            logger.info("Document deleted successfully from S3: {}", fileId);
            return "Document deleted successfully";

        } catch (RestClientException e) {
            logger.error("Error deleting document from FastAPI: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error deleting document: {}", e.getMessage(), e);
            throw new RestClientException("Failed to delete document: " + e.getMessage(), e);
        }
    }
}
