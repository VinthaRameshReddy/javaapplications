package com.medgo.annotations.validators;

import com.medgo.annotations.StrongPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])" +           // at least one digit
                    "(?=.*[a-z])" +           // at least one lowercase
                    "(?=.*[A-Z])" +           // at least one uppercase
                    "(?=.*[@#$%^&+=!])" +     // at least one special character
                    "(?=\\S+$)" +             // no whitespace
                    ".{8,}$";                 // at least 8 characters

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) return false;
        return password.matches(PASSWORD_PATTERN);
    }
}
