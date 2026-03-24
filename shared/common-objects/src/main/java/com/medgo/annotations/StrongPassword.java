package com.medgo.annotations;

import com.medgo.annotations.validators.StrongPasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {

    String message() default "Password must be strong. It should contain at least 8 characters, one uppercase letter,"
            + " one lowercase letter, one digit, and one special character.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
