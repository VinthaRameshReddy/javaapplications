package com.medgo.auth.controller;

import com.medgo.auth.commonutilitys.ResponseHeaderUtil;
import com.medgo.auth.commonutilitys.SecurityValidationUtil;
import com.medgo.auth.domain.request.*;
import com.medgo.auth.service.ConsentService;
import com.medgo.auth.service.OtpService;
import com.medgo.auth.service.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import com.medgo.commons.CommonResponse;
import com.medgo.auth.domain.request.LogoutRequest;
import com.medgo.crypto.annotation.DecryptBody;
import com.medgo.crypto.annotation.EncryptResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.medgo.auth.service.LogoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private ConsentService consentService;

    @Autowired
    private LogoutService logoutService;


    @PostMapping("/validateMemberRegDetails")
    @EncryptResponse
    public CommonResponse memberRegistration(
            @DecryptBody(MemberValidationRequest.class) MemberValidationRequest memRegister
    ) {
        return registrationService.validateMemberRegistration(memRegister);

    }


    @PostMapping("/validateMemberDetails")
    @EncryptResponse
    public CommonResponse registerMember(
            @Valid @DecryptBody(MemberRegistrationRequest.class) MemberRegistrationRequest request
    ) {
        return registrationService.registerMember(request);
    }


    @PostMapping("/validateNonMemberDetails")
    @EncryptResponse
    public CommonResponse registerNonMember(
            @Valid @DecryptBody(NonMemberRegistrationRequest.class) NonMemberRegistrationRequest request
    ) {
        return registrationService.registerNonMember(request);
    }


    @PostMapping("/verifyOtp")
    @EncryptResponse
    public ResponseEntity<CommonResponse> verifyOtp(@Valid @DecryptBody(VerifyOtpRequest.class) VerifyOtpRequest request
    ) {
        logger.info("VerifyOtpRequest: {}", request);
        CommonResponse response = registrationService.verifyLoginOtpAndTrustDevice(request);
        logger.info("VerifyOtpResponse: {}", response);
        return ResponseHeaderUtil.processTokenResponse(response, true);
    }

    @PostMapping("/resendOtp")
    @EncryptResponse
    public CommonResponse resendOtp(@DecryptBody(ResendOtpRequest.class) ResendOtpRequest request
    ) {
        return otpService.resendOtp(request);
    }

    @PostMapping("/setPasswordMember")
    @EncryptResponse
    public CommonResponse setPassword(@Valid @DecryptBody(PasswordRequest.class) PasswordRequest request
    ) {
        return registrationService.setPassword(request);
    }

    @PostMapping("/setPasswordNonMember")
    @EncryptResponse
    public CommonResponse setNonMemberPassword(@Valid @DecryptBody(PasswordRequest.class) PasswordRequest request
    ) {
        return registrationService.setNonMemberPassword(request);
    }


    @PostMapping("/login/member")
    @EncryptResponse
    public ResponseEntity<CommonResponse> loginMember(
            @Valid @DecryptBody(LoginMemberRequest.class) LoginMemberRequest request
    ) {
        logger.info("LoginMemberRequest: {}", request);
        ResponseEntity<CommonResponse> validationError = SecurityValidationUtil.validateDeviceId();
        if (validationError != null) {
            return validationError;
        }
        CommonResponse response = registrationService.loginMember(request);
        logger.info("LoginMemberResponse: {}", response);
        return ResponseHeaderUtil.processTokenResponse(response, true);
    }


    @PostMapping("/login/nonmember")
    @EncryptResponse
    public ResponseEntity<CommonResponse> loginNonMember(
            @Valid @DecryptBody(LoginMemberRequest.class) LoginMemberRequest request
    ) {
        logger.info("LoginMemberRequest: {}", request);
        ResponseEntity<CommonResponse> validationError = SecurityValidationUtil.validateDeviceId();
        if (validationError != null) {
            return validationError;
        }
        CommonResponse response = registrationService.loginNonMember(request);
        logger.info("LoginMemberResponse: {}", response);
        return ResponseHeaderUtil.processTokenResponse(response, true);
    }


    @EncryptResponse
    @PostMapping("/member/requestOtp")
    public CommonResponse requestMemberOtp(@DecryptBody(MemberOtpRequest.class) MemberOtpRequest request
    ) {
        return registrationService.requestMemberOtp(request);
    }


    @PostMapping("/nonmember/requestOtp")
    @EncryptResponse
    public CommonResponse requestNonMemberOtp(@DecryptBody(NonMemberOtpRequest.class) NonMemberOtpRequest request
    ) {
        return registrationService.requestNonMemberOtp(request);
    }

    @PostMapping("/member/requestPasswordResetOtp")
    @EncryptResponse
    public CommonResponse requestMemberPasswordResetOtp(@DecryptBody(MemberOtpRequest.class) MemberOtpRequest request
    ) {
        return registrationService.requestPasswordResetMember(request);
    }

    @PostMapping("/nonmember/requestPasswordResetOtp")
    @EncryptResponse
    public CommonResponse requestNonMemberPasswordResetOtp(@DecryptBody(NonMemberOtpRequest.class) NonMemberOtpRequest request
    ) {
        return registrationService.requestPasswordResetNonMember(request);
    }

    @PostMapping("/member/resetPassword")
    @EncryptResponse
    public CommonResponse resetMemberPassword(
            @Valid @DecryptBody(ResetPasswordRequest.class) ResetPasswordRequest request

    ) {
        return registrationService.resetMemberPassword(request);
    }

    @PostMapping("/nonmember/resetPassword")
    @EncryptResponse
    public CommonResponse resetNonMemberPassword(
            @Valid @DecryptBody(ResetPasswordRequest.class) ResetPasswordRequest request
    ) {
        return registrationService.resetNonMemberPassword(request);
    }

    @PostMapping("/biometric")
    @EncryptResponse
    public ResponseEntity<CommonResponse> checkBiometric(
            @Valid @DecryptBody(BiometricCheckRequest.class) BiometricCheckRequest request
    ) {
        logger.info("BiometricCheckRequest: userId={}", request.userId());
        ResponseEntity<CommonResponse> validationError = SecurityValidationUtil.validateDeviceId();
        if (validationError != null) {
            return validationError;
        }
        CommonResponse response = registrationService.checkBiometric(request);
        logger.info("BiometricCheckResponse: {}", response);
        return ResponseHeaderUtil.processTokenResponse(response);
    }

    @PostMapping("/registerbio")
    @EncryptResponse
    public CommonResponse registerBio(
            @Valid @DecryptBody(RegisterBioRequest.class) RegisterBioRequest request
    ) {
        logger.info("RegisterBioRequest: {}", request);
        CommonResponse response = registrationService.registerBio(request);
        logger.info("RegisterBioResponse: {}", response);
        return response;
    }

    @PostMapping("/biometric/challenge")
    @EncryptResponse
    public CommonResponse generateBiometricChallenge(
            @Valid @DecryptBody(BiometricChallengeRequest.class) BiometricChallengeRequest request
    ) {
        logger.info("BiometricChallengeRequest: email={}", request.email());
        CommonResponse response = registrationService.generateBiometricChallenge(request);
        logger.info("BiometricChallengeResponse: {}", response);
        return response;
    }

    @PostMapping("/biometric/login")
    @EncryptResponse
    public ResponseEntity<CommonResponse> biometricLogin(
            @Valid @DecryptBody(BiometricLoginRequest.class) BiometricLoginRequest request
    ) {
        logger.info("BiometricLoginRequest: email={}", request.email());
        ResponseEntity<CommonResponse> validationError = SecurityValidationUtil.validateDeviceId();
        if (validationError != null) {
            return validationError;
        }
        CommonResponse response = registrationService.biometricLogin(request);
        logger.info("BiometricLoginResponse: {}", response);
        return ResponseHeaderUtil.processTokenResponse(response, true);
    }

    @PostMapping("/biometric/storePasskeyHash")
    @EncryptResponse
    public CommonResponse storePasskeyHash(
            @Valid @DecryptBody(StorePasskeyHashRequest.class) StorePasskeyHashRequest request
    ) {
        logger.info("StorePasskeyHashRequest: email={}", request.email());
        CommonResponse response = registrationService.storePasskeyHash(request);
        logger.info("StorePasskeyHashResponse: {}", response);
        return response;
    }

    @PostMapping("/consent/list")
    @EncryptResponse
    public CommonResponse listConsents(
            @DecryptBody(ListConsentsRequest.class) ListConsentsRequest request
    ) {
        logger.info("ListConsentsRequest received");
        CommonResponse response = consentService.listConsents(request);
        logger.info("ListConsentsResponse: {}", response);
        return response;
    }

    @PostMapping("/consent/store")
    @EncryptResponse
    public CommonResponse storeUserConsent(
            @Valid @DecryptBody(StoreUserConsentRequest.class) StoreUserConsentRequest request
    ) {
        logger.info("StoreUserConsentRequest: userId={}, consentIds={}, agreed={}", 
                request.userId(), request.consentIds(), request.agreed());
        CommonResponse response = consentService.storeUserConsent(request);
        logger.info("StoreUserConsentResponse: {}", response);
        return response;
    }

    @PostMapping("/userDetails")
    @EncryptResponse
    public ResponseEntity<CommonResponse> getUserDetails(
            @Valid @DecryptBody(UserDetailsRequest.class) UserDetailsRequest request
    ) {
        logger.info("UserDetailsRequest: {}", request.userId());
        ResponseEntity<CommonResponse> validationError = SecurityValidationUtil.validateDeviceId();
        if (validationError != null) {
            return validationError;
        }
        CommonResponse response = registrationService.getUserDetails(request);
        logger.info("UserDetailsResponse: {}", response);
        return ResponseHeaderUtil.processTokenResponse(response);
    }

    @PostMapping("/logout")
    @EncryptResponse
    public CommonResponse logout(@Valid @DecryptBody(LogoutRequest.class) LogoutRequest request, HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        // Delegate all logout logic to LogoutService which will validate token presence/ownership
        return logoutService.logout(token, request.userId());
    }


}
