package com.medgo.auth.domain.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record StoreUserConsentRequest(
        @NotNull(message = "User email is required")
        @NotEmpty(message = "User email cannot be empty")
        String userId,

        @NotNull(message = "Consent IDs are required")
        @NotEmpty(message = "At least one consent ID is required")
        List<Integer> consentIds,

        @NotNull(message = "Agreed status is required")
        Integer agreed
) {
}

