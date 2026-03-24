package com.medgo.auth.domain.response;

public record OtpResponse(
        String message,
        String otpRefId) {
}
