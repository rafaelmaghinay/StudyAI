package com.example.studyai.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Load .env file earliest possible in Spring Boot lifecycle
 * This EnvironmentPostProcessor runs BEFORE any bean definitions
 */
@Component
public class DotEnvConfig implements EnvironmentPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DotEnvConfig.class);

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            // Load .env file
            Dotenv dotenv = Dotenv.configure()
                    .directory(".")
                    .ignoreIfMissing()
                    .load();

            // Add all .env properties to Spring's environment
            Map<String, Object> properties = new HashMap<>();
            dotenv.entries().forEach(entry -> {
                properties.put(entry.getKey(), entry.getValue());
                logger.debug("Loaded environment variable from .env: {}", entry.getKey());
            });

            // Register as highest priority property source
            environment.getPropertySources().addFirst(
                    new MapPropertySource("dotenv", properties));

            logger.info("Successfully loaded {} environment variables from .env file", properties.size());

        } catch (Exception e) {
            logger.warn("Could not load .env file: {}. Continuing with system environment variables.",
                    e.getMessage());
        }
    }
}
