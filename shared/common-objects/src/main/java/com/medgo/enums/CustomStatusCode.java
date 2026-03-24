package com.medgo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CustomStatusCode {
    SUCCESS(000, "SUCCESS"),



    // OTP Errors

    INVALID_OTP(1001, "The OTP you entered is invalid. Please try again."),
    EXPIRED_OTP(1002, "Your OTP has expired. Please request a new one."),
    OTP_ATTEMPT_EXCEEDED(1003, "Maximum OTP attempts reached. Please try again after 15 minutes."),
    OTP_NOT_FOUND(1004, "No OTP found for this request. Please generate a new one."),
    OTP_ALREADY_USED(1005, "This OTP has already been used. Please request a new one."),
    OTP_RESEND_LIMIT(1006, "You have reached the maximum resend attempts. Try again later."),
    OTP_GENERATION_FAILED(1007, "Unable to generate OTP at this time. Please try again."),
    OTP_BLOCKED(1008, "Your account has been temporarily blocked due to repeated invalid attempts."),
    SESSION_EXPIRED(1009, "Your session has expired. Please log in again."),
    AUTHENTICATION_FAILED(1010, "Authentication failed"),
    AUTHORIZATION_FAILED(1011, "Access denied"),
    RESOURCE_NOT_FOUND(1012, "Requested resource not found"),
    DUPLICATE_REQUEST(1013, "Duplicate request detected"),
    RATE_LIMIT_EXCEEDED(1014, "Too many requests"),
    SERVICE_UNAVAILABLE(1015, "Service temporarily unavailable"),
    DATABASE_ERROR(1016, "Database operation failed"),
    WAIT_BEFORE_NEXT_OTP(1017, "Please wait before requesting another OTP."),
    INVALID_CREDENTIALS(1018, "Incorrect email/number or password.Please try again."),
    USER_NOT_FOUND(1019, "User not found."),
    MEMBER_NOT_FOUND(1020, "Member not found."),
    INACTIVE_USER(1021, "Account is inactive. Please contact support."),
    INVALID_TOKEN(1022, "Invalid Token."),
    TOKEN_EXPIRED(1023, "Token has expired."),
    NO_REIMBURSEMENT_HISTORY_FOUND(1024, "No reimbursement history found."),


    // Fallback
    UNKNOWN_ERROR(1099, "Unexpected error occurred");

    private final int code;
    private final String message;
}
