package com.example.studyai.resolver;

import com.example.studyai.annotation.CurrentUser;
import com.example.studyai.model.User;
import com.example.studyai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Resolver for @CurrentUser annotation
 * Extracts the authenticated user from Spring Security context and injects it
 * into controller methods
 */
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private static final Logger logger = LoggerFactory.getLogger(CurrentUserArgumentResolver.class);

    @Autowired
    private UserService userService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(CurrentUser.class) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Attempting to resolve @CurrentUser with no authentication");
            return null;
        }

        // Get the userId from the authentication principal
        // This is set by our authentication filter
        Object principal = authentication.getPrincipal();

        if (principal instanceof String) {
            try {
                UUID userId = UUID.fromString((String) principal);
                User user = userService.getUserById(userId);
                logger.debug("Resolved current user: {}", user.getEmail());
                return user;
            } catch (IllegalArgumentException e) {
                logger.error("Invalid UUID format in authentication: {}", e.getMessage());
                return null;
            } catch (Exception e) {
                logger.error("Failed to resolve current user: {}", e.getMessage());
                return null;
            }
        }

        logger.warn("Principal is not a string: {}", principal.getClass().getName());
        return null;
    }
}
