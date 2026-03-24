Task: Harden password reset flow with OTP verification and 90-second expiry

Context:
- Service: auth-service
- OTP handling is implemented in OtpServiceImpl/UserOTPModel, password reset in AuthServiceImpl.
- Current flows: request OTP → verify OTP → reset password (member and non-member).

Requirements:

1. OTP verification flag
- Use the existing `otpValidated` field on `UserOTPModel` to track whether an OTP session has been successfully verified.
- In `OtpServiceImpl.verifyOtp(VerifyOtpRequest request)`:
  - On successful OTP verification (correct OTP, within expiry, below max attempts):
    - Set `session.setOtpValidated(true)` before saving the new OTP session under the new `otpRefId`.
  - Keep all existing behavior (attempt tracking, expiry check, bypass member logic, logging, redis keys) unchanged.

2. OTP validation check API
- In `OtpService` interface, add:
  - `boolean isOtpValidated(String otpRefId);`
- Implement this in `OtpServiceImpl`:
  - Given an `otpRefId`, load the corresponding `UserOTPModel` from Redis.
  - Return `false` if:
    - `otpRefId` is null/blank, or
    - No session is found, or
    - `otpValidated` is false or null.
  - Return `true` only if a session exists and `otpValidated` is true.
  - Add appropriate logging; do not change any existing methods.

3. Enforce OTP verification before password reset
- In `AuthServiceImpl.resetMemberPassword(ResetPasswordRequest request)` and `resetNonMemberPassword(ResetPasswordRequest request)`:
  - Before using `getUserDataByRefId` or updating the password:
    - Call `otpService.isOtpValidated(request.otpRefId())`.
    - If it returns `false`, return an error `CommonResponse` with:
      - HTTP status: 401 UNAUTHORIZED
      - Message (example): `"OTP not verified or session expired. Please verify OTP before resetting your password."`
  - If it returns `true`, proceed with the existing reset logic exactly as-is:
    - Look up user from OTP data, encode and save new password, reset attempts, cleanup OTP, and return current success messages.
- Do not modify any other flows (login, registration, changePassword, biometric flows, etc.).

4. OTP expiry configuration
- In `OtpServiceImpl`, keep:
  - `private static final int RESEND_WAIT_SECONDS = 90;   // Min 90s wait between resend`
  - `private static final int OTP_EXPIRY_SECONDS = 90;    // OTP valid for 90s`
- Ensure that `verifyOtp` uses `OTP_EXPIRY_SECONDS` to determine whether an OTP has expired.
- Do not change any other timing logic (SESSION_TTL_MINUTES, ACCOUNT_LOCK_MINUTES, etc.).

5. Flow behavior (post-change)
- Expected standard reset flow:
  1) Client calls `requestMemberOtp` / `requestNonMemberOtp` → receives `otpRefId`.
  2) Client calls `/api/v1/auth/verifyOtp` with `{ otpRefId, otp, flowType: "PASSWORD_RESET" }` within 90 seconds.
     - On success, backend returns new `otpRefId` (verified token) and marks `otpValidated=true` in that session.
  3) Client calls `/member/resetPassword` or `/nonmember/resetPassword` with:
     `{ otpRefId: <verified-otpRefId>, newPassword: "..." }`.
     - If OTP is verified and not expired, password resets successfully.
     - If OTP is not verified / expired / missing, returns 401 with “OTP not verified or session expired…” message.

Constraints:
- Do not break or alter any existing business logic, side effects, or response formats beyond what’s needed to enforce OTP verification and maintain 90-second expiry.
- Do not change how OTP emails/SMS are sent or to which identifiers they are sent.
- Keep all public endpoints and request/response shapes backward compatible, except for the new 401 error case when reset is attempted without a verified OTP.

