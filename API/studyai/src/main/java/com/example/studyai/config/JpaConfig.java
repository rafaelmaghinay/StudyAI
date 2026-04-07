package com.example.studyai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA Configuration
 * Spring Boot 3.x auto-configures JPA/Hibernate
 * This configuration enables transaction management
 */
@Configuration
@EnableTransactionManagement
public class JpaConfig {
    // Spring Boot auto-configuration handles:
    // - DataSource creation and connection pooling (HikariCP)
    // - EntityManagerFactory creation
    // - Transaction management
    // - JPA repository scanning
    //
    // Configuration is in application.properties
}
