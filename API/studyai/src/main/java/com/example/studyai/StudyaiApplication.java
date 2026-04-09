package com.example.studyai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application entrypoint for the StudyAI backend.
 * <p>
 * Boots the REST API, security configuration, and integration with the
 * FastAPI-based quiz generation service and persistence layer.
 */
@SpringBootApplication
public class StudyaiApplication {

	/**
	 * Start the StudyAI Spring Boot application.
	 *
	 * @param args optional command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(StudyaiApplication.class, args);
	}

}
