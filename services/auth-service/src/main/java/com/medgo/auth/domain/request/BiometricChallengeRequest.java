package com.medgo.auth.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BiometricChallengeRequest(
        @NotBlank(message = "Email is required")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        String email
) {
}











