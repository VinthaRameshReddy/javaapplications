package com.medgo.auth.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserDetailsRequest(
        @NotBlank(message = "Email or username is required")
        @Size(max = 100, message = "Email or username must not exceed 100 characters")
        String userId
) {
}

