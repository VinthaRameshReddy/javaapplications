package com.medgo.auth.domain.request;

public record ResetPasswordRequest(
        String otpRefId,
        String newPassword) {
}