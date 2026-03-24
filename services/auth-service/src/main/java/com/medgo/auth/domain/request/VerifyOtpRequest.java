package com.medgo.auth.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VerifyOtpRequest(@NotBlank(message = "otpRefId is required") @NotNull(message = "otpRefId cannot be null")
                               String otpRefId,

                               @NotBlank(message = "OTP is required") @NotNull(message = "OTP cannot be null") String otp,

                               @NotBlank(message = "Flow type is required") @NotNull(message = "Flow type cannot be null") String flowType
) {

}