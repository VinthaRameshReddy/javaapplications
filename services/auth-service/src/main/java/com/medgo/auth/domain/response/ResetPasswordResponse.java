package com.medgo.auth.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResetPasswordResponse(

        @JsonProperty("status")
        String status) {
}
