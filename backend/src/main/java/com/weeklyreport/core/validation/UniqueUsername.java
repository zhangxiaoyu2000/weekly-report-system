package com.weeklyreport.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation to check username uniqueness
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueUsernameValidator.class)
@Documented
public @interface UniqueUsername {
    
    String message() default "Username already exists";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}