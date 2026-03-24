package com.medgo.auth.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DeleteAccountRequest(
        @NotBlank(message = "User ID (email or memberCode) is required")
        @Size(max = 100, message = "User ID must not exceed 100 characters")
        String userId,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 60, message = "Password must be between 8 and 60 characters")
        String password,

        @NotNull(message = "Confirmation is required")
        Boolean confirmed
) {
}

