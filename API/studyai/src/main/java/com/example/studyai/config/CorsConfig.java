package com.example.studyai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Global REST client configuration
 * CORS is handled centrally in SecurityConfig.java
 */
@Configuration
public class CorsConfig {

    /**
     * REST template for making HTTP requests to external services
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}


