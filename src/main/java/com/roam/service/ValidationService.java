package com.roam.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Centralized validation service using Jakarta Bean Validation.
 * Validates entities before persistence operations.
 * 
 * @author muntasiractive
 * @since 1.1.0
 */
public class ValidationService {

    private static ValidationService instance;
    private final Validator validator;

    private ValidationService() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    /**
     * Get singleton instance of ValidationService.
     */
    public static synchronized ValidationService getInstance() {
        if (instance == null) {
            instance = new ValidationService();
        }
        return instance;
    }

    /**
     * Validate an entity and throw exception if invalid.
     * 
     * @param entity The entity to validate
     * @param <T>    The type of entity
     * @throws ValidationException if validation fails
     */
    public <T> void validate(T entity) {
        Set<ConstraintViolation<T>> violations = validator.validate(entity);

        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));

            throw new ValidationException("Validation failed: " + errors);
        }
    }

    /**
     * Validate an entity and return violations without throwing exception.
     * 
     * @param entity The entity to validate
     * @param <T>    The type of entity
     * @return Set of constraint violations (empty if valid)
     */
    public <T> Set<ConstraintViolation<T>> validateAndGetViolations(T entity) {
        return validator.validate(entity);
    }

    /**
     * Check if an entity is valid.
     * 
     * @param entity The entity to check
     * @param <T>    The type of entity
     * @return true if valid, false otherwise
     */
    public <T> boolean isValid(T entity) {
        return validator.validate(entity).isEmpty();
    }

    /**
     * Validate a specific property of an entity.
     * 
     * @param entity       The entity containing the property
     * @param propertyName The name of the property to validate
     * @param <T>          The type of entity
     * @throws ValidationException if validation fails
     */
    public <T> void validateProperty(T entity, String propertyName) {
        Set<ConstraintViolation<T>> violations = validator.validateProperty(entity, propertyName);

        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));

            throw new ValidationException("Property '" + propertyName + "' validation failed: " + errors);
        }
    }

    /**
     * Custom exception for validation failures.
     */
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }

        public ValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
