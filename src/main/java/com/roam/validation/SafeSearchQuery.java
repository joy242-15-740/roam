package com.roam.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates that a string is a safe search query (no injection attempts,
 * reasonable complexity).
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SafeSearchQueryValidator.class)
@Documented
public @interface SafeSearchQuery {

    String message() default "Search query contains invalid characters or is too complex";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
