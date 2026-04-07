package com.example.studyai.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for object mapping and deserialization
 * RestTemplate bean is provided by CorsConfig
 */
@Configuration
public class RestClientConfig {

    /**
     * ObjectMapper bean for JSON processing
     * Configured with JavaTimeModule for LocalDateTime serialization
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Register JavaTimeModule for Java 8 date/time types (LocalDateTime, LocalDate, etc.)
        mapper.registerModule(new JavaTimeModule());
        
        // Don't serialize dates as timestamps - use ISO-8601 format
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Don't serialize null values
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        return mapper;
    }
}
