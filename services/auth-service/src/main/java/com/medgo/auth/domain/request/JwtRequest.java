package com.medgo.auth.domain.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;


public record JwtRequest(@NotBlank @JsonProperty("userId") String userId) {
}