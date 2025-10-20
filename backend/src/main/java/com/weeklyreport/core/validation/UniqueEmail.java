package com.weeklyreport.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation to check email uniqueness
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmailValidator.class)
@Documented
public @interface UniqueEmail {
    
    String message() default "Email already exists";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}