package com.medgo.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class EmailValidator implements ConstraintValidator<ValidEmail, String> {


    private static final String EMAIL_REGEX = 
        "^[a-zA-Z0-9]([a-zA-Z0-9._+-]*[a-zA-Z0-9])?@([a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,63}$";

    private static final String EMAIL_REGEX_UNICODE = 
        "^[\\p{L}\\p{N}]([\\p{L}\\p{N}._+-]*[\\p{L}\\p{N}])?@([\\p{L}\\p{N}]([\\p{L}\\p{N}-]*[\\p{L}\\p{N}])?\\.)+[\\p{L}]{2,63}$";
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private static final Pattern EMAIL_PATTERN_UNICODE = Pattern.compile(EMAIL_REGEX_UNICODE);
    
    // Maximum lengths per RFC 5321
    private static final int MAX_LOCAL_PART_LENGTH = 64;
    private static final int MAX_DOMAIN_LENGTH = 255;
    private static final int MAX_EMAIL_LENGTH = 320; // 64 + 1 (@) + 255

    @Override
    public void initialize(ValidEmail constraintAnnotation) {
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isBlank()) {
            return true;
        }

        String trimmedEmail = email.trim();
        // Check total length
        if (trimmedEmail.length() > MAX_EMAIL_LENGTH) {
            return false;
        }

        int atIndex = trimmedEmail.indexOf('@');
        if (atIndex <= 0 || atIndex >= trimmedEmail.length() - 1) {
            return false;
        }
        
        String localPart = trimmedEmail.substring(0, atIndex);
        String domainPart = trimmedEmail.substring(atIndex + 1);
        
        // Check local part length
        if (localPart.length() > MAX_LOCAL_PART_LENGTH || localPart.isEmpty()) {
            return false;
        }
        
        // Check domain part length
        if (domainPart.length() > MAX_DOMAIN_LENGTH || domainPart.isEmpty()) {
            return false;
        }
        
        // Check for consecutive dots in local part
        if (localPart.contains("..")) {
            return false;
        }
        
        // Check for consecutive dots or hyphens at start/end of domain
        if (domainPart.startsWith(".") || domainPart.startsWith("-") ||
            domainPart.endsWith(".") || domainPart.endsWith("-")) {
            return false;
        }
        
        // Check for consecutive dots in domain
        if (domainPart.contains("..")) {
            return false;
        }
        
        // Try standard pattern first (ASCII)
        if (EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            return true;
        }
        
        return EMAIL_PATTERN_UNICODE.matcher(trimmedEmail).matches();
    }
}

