package com.medgo.auth.service;

import com.medgo.auth.domain.request.ResendOtpRequest;
import com.medgo.auth.domain.request.VerifyOtpRequest;
import com.medgo.commons.CommonResponse;

import java.util.Map;

public interface OtpService {
    CommonResponse generateOtp(String identifier, String email, String mobile, Map<String, Object> userData);

    CommonResponse generateLoginOtp(String identifier, String email, String mobile, Map<String, Object> userData);
    CommonResponse generatePasswordResetOtp(String identifier, String email, String mobile, Map<String, Object> userData);

    CommonResponse verifyOtp(VerifyOtpRequest request);

    CommonResponse resendOtp(ResendOtpRequest request);

    Map<String, Object> getUserDataByRefId(String otpRefId);
    void cleanupOtp(String otpRefId);

    CommonResponse ifUserLocked(String identifier);


    boolean isOtpValidated(String otpRefId);
    /**
     * Mark active OTP sessions for the given memberCode as having completed registration validation.
     * This allows registration-flow OTPs to be used for subsequent operations (like setPassword).
     */
    void markRegistrationValidatedForMember(String memberCode);
}