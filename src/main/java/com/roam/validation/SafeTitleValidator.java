package com.roam.validation;

import com.roam.util.InputSanitizer;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for SafeTitle annotation.
 */
public class SafeTitleValidator implements ConstraintValidator<SafeTitle, String> {

    private int maxLength;

    @Override
    public void initialize(SafeTitle constraintAnnotation) {
        this.maxLength = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Use @NotNull for null checks
        }

        try {
            String sanitized = InputSanitizer.sanitizeTitle(value);
            return sanitized.length() <= maxLength;
        } catch (IllegalArgumentException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage())
                    .addConstraintViolation();
            return false;
        }
    }
}
