package com.medgo.auth.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginMemberRequest(

        @NotBlank(message = "Credential (email or mobile) is required")
        @Size(max = 100, message = "Credential must not exceed 100 characters")
        String userId,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 60, message = "Password must be between 8 and 60 characters")
        String password,

        Boolean biometricEnabled
) {
}