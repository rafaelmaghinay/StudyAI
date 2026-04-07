package com.example.studyai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Health Check Controller
 * Provides endpoints for monitoring application health
 */
@RestController
@RequestMapping("/api/health")
public class HealthCheckController implements HealthIndicator {

    @Autowired
    private DataSource dataSource;

    /**
     * GET /api/health/ping - Simple ping test
     */
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    /**
     * GET /api/health/db - Database connectivity test
     */
    @GetMapping("/db")
    public String checkDatabase() {
        try {
            Connection conn = dataSource.getConnection();
            boolean connected = !conn.isClosed();
            conn.close();
            return connected ? "Database connected" : "Database connection failed";
        } catch (Exception e) {
            return "Database error: " + e.getMessage();
        }
    }

    /**
     * Health indicator for Spring Boot actuator
     */
    @Override
    public Health health() {
        try {
            Connection conn = dataSource.getConnection();
            boolean connected = !conn.isClosed();
            conn.close();
            return connected ? Health.up().build() : Health.down().build();
        } catch (Exception e) {
            return Health.down().withException(e).build();
        }
    }
}
