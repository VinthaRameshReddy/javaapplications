package com.medgo.auth.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponse(
        @JsonProperty("status") String status,
        @JsonProperty("message") String message
) {
}
