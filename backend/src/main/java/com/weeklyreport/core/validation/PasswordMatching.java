package com.weeklyreport.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation to check password matching
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordMatchingValidator.class)
@Documented
public @interface PasswordMatching {
    
    String message() default "Password and confirmation do not match";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    String passwordField() default "password";
    
    String confirmPasswordField() default "confirmPassword";
}