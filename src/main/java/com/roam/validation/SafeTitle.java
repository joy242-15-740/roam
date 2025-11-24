package com.roam.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates that a string is a safe title (no control characters, length within
 * bounds).
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SafeTitleValidator.class)
@Documented
public @interface SafeTitle {

    String message() default "Title contains invalid characters or exceeds maximum length";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int max() default 255;
}
