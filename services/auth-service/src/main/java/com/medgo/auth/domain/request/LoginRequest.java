package com.medgo.auth.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginRequest(
        @JsonProperty("emailId") String emailId,
        @JsonProperty("mobileNumber") String mobileNumber,
        @JsonProperty("password") String password,
        @JsonProperty("consentFlag") String consentFlag
) {
}
