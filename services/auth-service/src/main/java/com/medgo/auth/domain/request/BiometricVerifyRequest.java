package com.medgo.auth.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BiometricVerifyRequest(
        @NotBlank(message = "User identifier (email or mobile) is required")
        @Size(max = 100, message = "User identifier must not exceed 100 characters")
        String userId,

        @NotBlank(message = "Passkey is required")
        String passkey
) {
}


