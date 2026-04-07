package com.example.studyai.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application Startup Listener
 * Logs application startup information and performs initialization checks
 */
@Component
public class ApplicationStartupListener {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupListener.class);

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("=====================================");
        logger.info("StudyAI Application Started Successfully");
        logger.info("=====================================");
        logger.info("Application is now running on:");
        logger.info("  - Main API: http://localhost:8080");
        logger.info("  - Health Check: http://localhost:8080/api/health/ping");
        logger.info("  - Database Health: http://localhost:8080/api/health/db");
        logger.info("=====================================");
    }
}
