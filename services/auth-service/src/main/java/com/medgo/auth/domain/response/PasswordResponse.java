package com.medgo.auth.domain.response;

public record PasswordResponse(
        String email,
        String message) {
}