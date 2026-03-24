package com.medgo.auth.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordRequest(

        @NotBlank(message = "Password is required")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&*()\\-+=\\[\\]/\\\\;,.?\":|_]).{9,25}$",
                message = "Password must be 9-25 characters, include lowercase, uppercase, digit, and special character"
        )

        String password,

        String otpRefId) {
}