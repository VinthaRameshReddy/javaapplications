package com.medgo.auth.service;

import com.medgo.auth.domain.request.*;
import com.medgo.commons.CommonResponse;
import jakarta.validation.Valid;

public interface RegistrationService {


    CommonResponse setPassword(PasswordRequest request);

    CommonResponse requestMemberOtp(MemberOtpRequest request);

    CommonResponse requestNonMemberOtp(NonMemberOtpRequest request);
    CommonResponse requestPasswordResetMember(MemberOtpRequest request);
    CommonResponse requestPasswordResetNonMember(NonMemberOtpRequest request);

    CommonResponse loginMember(@Valid LoginMemberRequest request);

    CommonResponse validateMemberRegistration(MemberValidationRequest memRegister);

    CommonResponse loginNonMember(@Valid LoginMemberRequest request);

    CommonResponse registerNonMember(NonMemberRegistrationRequest request);

    CommonResponse registerMember(MemberRegistrationRequest request);

    CommonResponse setNonMemberPassword(@Valid PasswordRequest request);

    CommonResponse resetMemberPassword(@Valid ResetPasswordRequest request);

    CommonResponse verifyLoginOtpAndTrustDevice(VerifyOtpRequest verifyRequest);

    CommonResponse resetNonMemberPassword(@Valid ResetPasswordRequest request);

    CommonResponse checkBiometric(@Valid BiometricCheckRequest request);

    CommonResponse registerBio(@Valid RegisterBioRequest request);

    CommonResponse verifyBiometric(@Valid BiometricVerifyRequest request);

    CommonResponse generateBiometricChallenge(@Valid BiometricChallengeRequest request);

    CommonResponse biometricLogin(@Valid BiometricLoginRequest request);

    CommonResponse storePasskeyHash(@Valid StorePasskeyHashRequest request);

    CommonResponse getUserDetails(@Valid UserDetailsRequest request);

}