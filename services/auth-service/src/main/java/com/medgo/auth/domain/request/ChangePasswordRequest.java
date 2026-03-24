package com.medgo.auth.domain.request;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
    @NotBlank(message = "Current password is required")
    String currentPassword,
    @NotBlank(message = "New password is required")
    String newPassword,
    @NotBlank(message = "OTP is required")
    String otp
) {}

