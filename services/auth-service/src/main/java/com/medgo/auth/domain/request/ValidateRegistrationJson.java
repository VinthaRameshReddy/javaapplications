package com.medgo.auth.domain.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public record ValidateRegistrationJson(
        @NotBlank(message = com.medgo.auth.constants.Constants.EMAIL_NOT_BLANK)
        @Email(message = com.medgo.auth.constants.Constants.INVALID_EMAIL)
        @Size(max = 100, message = com.medgo.auth.constants.Constants.EMAIL_MAX_LENGTH)
        String email,

        @Size(max = 11, message = com.medgo.auth.constants.Constants.PHONE_MAX_LENGTH)
        @Pattern(regexp = "^[0-9]*$", message = com.medgo.auth.constants.Constants.PHONE_NUMERIC_ONLY)
        String phoneNumber,

        @NotBlank(message = "Member code cannot be blank")
        @Size(max = 50, message = "Member code must not exceed 50 characters")
        String memberCode) {
}