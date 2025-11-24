package com.roam.validation;

import com.roam.util.InputSanitizer;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for SafeSearchQuery annotation.
 */
public class SafeSearchQueryValidator implements ConstraintValidator<SafeSearchQuery, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true; // Empty queries are valid (will return no results)
        }

        try {
            InputSanitizer.sanitizeSearchQuery(value);
            return true;
        } catch (IllegalArgumentException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage())
                    .addConstraintViolation();
            return false;
        }
    }
}
