package com.medicard.integration.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EmailListValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEmailList {
    String message() default "Invalid email in the list";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
