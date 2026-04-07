package com.example.studyai.annotation;

import java.lang.annotation.*;

/**
 * Custom annotation to inject the current authenticated user into controller
 * methods
 *
 * Usage:
 * 
 * @GetMapping
 *             public ResponseEntity<?> getQuiz(@CurrentUser User
 *             user, @PathVariable UUID quizId) {
 *             // user is automatically injected from Spring Security context
 *             }
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
