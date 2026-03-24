package com.medgo.auth.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;

import java.util.List;

public class EmailListValidator implements ConstraintValidator<ValidEmailList, List<String>> {

    private final EmailValidator emailValidator = new EmailValidator();

    @Override
    public boolean isValid(List<String> emails, ConstraintValidatorContext context) {
        if (emails == null) return true;

        for (String email : emails) {
            if (email == null || !emailValidator.isValid(email, context)) {
                return false;
            }
        }
        return true;
    }
}
