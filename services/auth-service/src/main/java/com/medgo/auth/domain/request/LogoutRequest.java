package com.medgo.auth.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LogoutRequest(
        @NotBlank(message = "UserId is required")
        @Size(max = 100, message = "UserId must not exceed 100 characters")
        String userId
) {
}

