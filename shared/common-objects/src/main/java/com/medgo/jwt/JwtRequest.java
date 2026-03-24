package com.medgo.jwt;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;


public record JwtRequest(@NotBlank @JsonProperty("login_id") String loginId) {
}